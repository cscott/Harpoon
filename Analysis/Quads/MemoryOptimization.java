// MemoryOptimization.java, created Sun Jun 17 19:10:49 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.cscott.jutil.DisjointSet;
import net.cscott.jutil.GenericMultiMap;
import net.cscott.jutil.MultiMap;
import net.cscott.jutil.SnapshotIterator;
import net.cscott.jutil.WorkSet;
import net.cscott.jutil.Default;
import net.cscott.jutil.Default.PairList;

/**
 * <code>MemoryOptimization</code> reduces the number of memory operations
 * by combining multiple loads/stores to the same field/array element.
 * It should be safe with respect to the revised Java memory model.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MemoryOptimization.java,v 1.6 2004-02-08 01:53:14 cananian Exp $
 */
public final class MemoryOptimization
    extends harpoon.Analysis.Transformation.MethodMutator<Quad> {
    final CallGraph cg; final FieldSyncOracle fso;
    
    /** Creates a <code>MemoryOptimization</code>. */
    MemoryOptimization(HCodeFactory parent,
		       CallGraph cg, FieldSyncOracle fso) {
	// we take in SSA, and output SSA.
        super(harpoon.IR.Quads.QuadSSA.codeFactory(parent));
	this.cg = cg; this.fso = fso;
    }
    public MemoryOptimization(HCodeFactory parent,
			      ClassHierarchy ch, CallGraph cg) {
	this(parent, cg, new FieldSyncOracle(parent, ch, cg));
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> hc = input.hcode();
	/*
	System.out.println("BEFORE: ");
	hc.print(new java.io.PrintWriter(System.out));
	*/
	// do the analysis.
	Analysis a = new Analysis(hc);
	// okay, rename quads and eliminate useless stuff.
	Iterator<Quad> qit = new SnapshotIterator<Quad>(hc.getElementsI());
	// add moves to edges.
	for (Iterator<Edge> it=a.moves.keySet().iterator(); it.hasNext(); ) {
	    Edge e = it.next(); Quad q = e.to();
	    for (Iterator<Temp[]> it2=a.moves.getValues(e).iterator(); it2.hasNext();){
		Temp[] pair = it2.next();
		e = addAt(e, new MOVE(q.getFactory(), q,
				      a.ds.find(pair[0]),
				      a.ds.find(pair[1])));
	    }
	}
	// note that we created the 'qit' snapshot before adding the MOVEs.
	while (qit.hasNext()) {
	    Quad q = qit.next();
	    // delete useless.
	    if (a.useless.contains(q))
		q.remove();
	    else if (!(q instanceof HEADER)) // rename
		Quad.replace(q, q.rename(a.tempMap, a.tempMap));
	}
	//DeadCode.optimize((harpoon.IR.Quads.Code)hc, null);
	/*
	System.out.println("AFTER: ");
	hc.print(new java.io.PrintWriter(System.out));
	*/
	// done!
	return hc;
    }
    protected HCodeAndMaps<Quad> cloneHCode(HCode<Quad> hc,HMethod newmethod) {
	// make SSA into RSSx.
	assert hc.getName().equals(QuadSSA.codename);
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps<Quad> cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
    }
    protected String mutateCodeName(String codeName) {
	assert codeName.equals(QuadSSA.codename);
	return MyRSSx.codename;
    }
	/** helper routine to add a quad on an edge. */
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = e.from(); int frm_succ = e.which_succ();
	Quad to  = e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }


    class Analysis {
	/** <BasicBlock,temp,field> -> temp */
	final Map<BasicBlock<Quad>,Map<Value,Temp>> out =
	    new HashMap<BasicBlock<Quad>,Map<Value,Temp>>();
	/** Discovered Temp identities. */
	final DisjointSet<Temp> ds = new DisjointSet<Temp>();
	/** Set of useless stores */
	final Set<Quad> useless = new HashSet<Quad>();
	/** tempmap view of ds. */
	final TempMap tempMap = new TempMap() {
	    public Temp tempMap(Temp t) { return ds.find(t); }
	};
	/** <BasicBlock,Value>->Temp */
	private final Map<PairList<BasicBlock<Quad>,Value>,Temp> tempgen =
	    new HashMap<PairList<BasicBlock<Quad>,Value>,Temp>();
	Temp tempgen(BasicBlock<Quad> bb, Value v, Temp template) {
	    PairList<BasicBlock<Quad>,Value> key = Default.pair(bb, v);
	    if (!tempgen.containsKey(key)) {
		tempgen.put(key, new Temp(template));
	    }
	    return tempgen.get(key);
	}
	/** Edge -> set of moves */
	final MultiMap<Edge,Temp[]> moves = new GenericMultiMap<Edge,Temp[]>();
	/* set of phis we're adding moves for */
	final Set<PairList<BasicBlock<Quad>,Value>> phiadded =
	    new HashSet<PairList<BasicBlock<Quad>,Value>>();

	Analysis(HCode<Quad> hc) {
	    BasicBlock.Factory<Quad> bbF = new BasicBlock.Factory<Quad>(hc);
	    WorkSet<BasicBlock<Quad>> w =
		new WorkSet<BasicBlock<Quad>>(bbF.blockSet());
	    // forward dataflow for READ propagation.
	    while (!w.isEmpty()) {
		BasicBlock<Quad> bb = w.pop();
		if (doBlockGET(bbF, bb))  // if block out has changed...
		    w.addAll(bb.nextSet()); // ... add successors to workset.
	    }
	    // backwards dataflow for WRITE propagation.
	    /*
	    w.addAll(bbF.blockSet());
	    while (!w.isEmpty()) {
		BasicBlock<Quad> bb = w.pop();
		if (doBlockSET(bbF, bb))  // if block out has changed...
		    w.addAll(bb.prevSet()); // ... add successors to workset.
	    }
	    */
	    // done.
	}
	Map<Value,List<Temp>> valuemaptempmap(Map<Value,Temp> m,
					  Quad q, int which_pred) {
	    if (m==null) return null;
	    HashMap<Value,List<Temp>> result = new HashMap<Value,List<Temp>>();
	    for (Iterator<Map.Entry<Value,Temp>> it = m.entrySet().iterator();
		 it.hasNext(); ) {
		Map.Entry<Value,Temp> me = it.next();
		Value v = me.getKey().map(ds, q, which_pred);
		Temp t = ds.find(me.getValue());// not phi-mapped
		Temp tt = Value.map(t, ds, q, which_pred);// phi-mapped.
		result.put(v, Arrays.asList(new Temp[] { tt, t }));
	    }
	    return result;
	}

	boolean doBlockGET(BasicBlock.Factory<Quad> bbF, BasicBlock<Quad> bb) {
	    List<Quad> quads = bb.statements();
	    // compute in set by merging outs.
	    // map <temp,field>->temp; only present if all inputs have it.
	    // do renaming as per PHI.
	    Map<Value,Temp> in = new HashMap<Value,Temp>();
	    Quad first = quads.get(0);
	    Quad[] pred = first.prev();
	    // collect maps from each pred.
	    List<Map<Value,List<Temp>>> prin = new ArrayList<Map<Value,List<Temp>>>(pred.length);
	    for (int i=0; i<pred.length; i++) {
		BasicBlock<Quad> prbb = bbF.getBlock(pred[i]);
 		prin.add(valuemaptempmap(out.get(prbb), first, i));
	    }
	    // merge key sets
	    Set<Value> merged=null; boolean allknown=true;
	    for (int i=0; i<pred.length; i++) {
		if (prin.get(i)==null) { allknown=false; continue; }// no info yet.
		if (merged==null) merged = prin.get(i).keySet();
		else merged.retainAll(prin.get(i).keySet());
		moves.remove(first.prevEdge(i));
	    }
	    // handle merged inputs.
	    if (merged!=null && allknown)
		for (Iterator<Value> it=merged.iterator(); it.hasNext(); ) {
		    Value v = it.next();
		    Temp t=null; boolean same=true;
 		    for (int i=0; i<pred.length; i++) {
 			if (prin.get(i)==null) continue; // no info.
			// tt is the phi-mapped version of the temp.
			Temp tt = prin.get(i).get(v).get(0);
			if (t==null) t=tt;
			else if (!t.equals(tt)) same=false;
		    }
		    if (t==null) continue; // skip this.
		    if (same && !phiadded.contains(Default.pair(bb, v)))
			in.put(v, t);
		    else {
			Temp nt = ds.find(tempgen(bb, v, t));
			for (int i=0; i<pred.length; i++) {
			    if (prin.get(i)==null) continue;
			    // note that we use the un-phi-mapped temp here.
			    Temp tt = prin.get(i).get(v).get(1);
			    moves.add(first.prevEdge(i), new Temp[] {nt,tt});
			}
			in.put(v, nt);
			phiadded.add(Default.pair(bb, v));
		    }
		}
		    

	    ReadVisitor rv = new ReadVisitor(in);
	    for (Iterator<Quad> it=quads.iterator(); it.hasNext(); ) {
		Quad q = it.next();
		if (!useless.contains(q))
		    q.accept(rv);
	    }
	    // okay, now we have out map.
	    Map<Value,Temp> oldout = out.put(bb, in);
	    return (oldout==null || !oldout.equals(in));
	}
	class ReadVisitor extends QuadVisitor {
	    final Map<Value,Temp> map;
	    ReadVisitor(Map<Value,Temp> map) { this.map = map; }
	    public void visit(Quad q) { /* do nothing */ }
	    public void visit(GET q) {
		Value v = new FieldValue
		    (q.field(), q.isStatic()?null:ds.find(q.objectref()));
		if (map.containsKey(v)) {
		    ds.union(q.dst(), map.get(v));
		    useless.add(q);
		} else {
		    map.put(v, q.dst());
		}
	    }
	    public void visit(SET q) {
		Value v = new FieldValue
		    (q.field(), q.isStatic()?null:ds.find(q.objectref()));
		map.put(v, ds.find(q.src()));
	    }
	    public void visit(MOVE q) {
		ds.union(q.dst(), q.src());
		useless.add(q);
	    }
	    public void visit(AGET q) {
		Value v = new ArrayValue(ds.find(q.objectref()),
					 ds.find(q.index()));
		if (map.containsKey(v)) {
		    ds.union(q.dst(), map.get(v));
		    useless.add(q);
		} else {
		    map.put(v, q.dst());
		}
	    }
	    public void visit(ASET q) {
		Value v = new ArrayValue(ds.find(q.objectref()),
					 ds.find(q.index()));
		map.put(v, ds.find(q.src()));
	    }
	    public void visit(MONITORENTER q) { visitMONITOR(q); }
	    public void visit(MONITOREXIT q) { visitMONITOR(q); }
	    public void visitMONITOR(Quad q) {
		assert q instanceof MONITORENTER ||
			    q instanceof MONITOREXIT;
		for (Iterator<Value> it=map.keySet().iterator(); it.hasNext(); ) {
		    Value v = it.next();
		    if (v.isArray()) continue;
		    if (!Modifier.isFinal(v.field().getModifiers()))
			it.remove();
		}
	    }
	    
	    public void visit(CALL q) {
		HMethod calls[] = cg.calls(q.getFactory().getMethod(), q);
		for (Iterator<Value> it=map.keySet().iterator();
		     it.hasNext(); ) {
		    Value v = it.next();
		    if (v.isArray()) it.remove();
		    else if (Modifier.isFinal(v.field().getModifiers()))
			continue; // final fields can't be written during call
		    else for (int i=0; i<calls.length; i++)
			if (fso.isSync(calls[i]) ||
			    fso.isWritten(calls[i], v.field())) {
			    it.remove();
			    break;
			}
		}
	    }
	}
    }
    abstract static class Value { 
	boolean isArray() { return false; }
	HField field() { throw new Error(); }
	abstract Value map(DisjointSet<Temp> ds, Quad q, int which_pred);
	static final Temp map(Temp t,
			      DisjointSet<Temp> ds, Quad q, int which_pred) {
	    t = ds.find(t);
	    if (q instanceof PHI) {
		PHI phi = (PHI) q;
		for (int i=0; i<phi.numPhis(); i++)
		    if (phi.src(i, which_pred).equals(t))
			return ds.find(phi.dst(i));
	    }
	    return t;
	}
    }
    static class FieldValue extends Value {
	final HField hf;
	final Temp receiver;
	FieldValue(HField hf, Temp receiver) {
	    this.hf = hf; this.receiver=receiver;
	}
	HField field() { return hf; }
	Value map(DisjointSet<Temp> ds, Quad q, int which_pred) {
	    if (receiver==null) return this;
	    Temp r = map(receiver, ds, q, which_pred);
	    if (receiver==r) return this;
	    return new FieldValue(hf, r);
	}
	public int hashCode() {
	    return hf.hashCode()+7*(receiver==null?0:receiver.hashCode());
	}
	public boolean equals(Object o) {
	    if (!(o instanceof FieldValue)) return false;
	    FieldValue fv = (FieldValue) o;
	    return hf.equals(fv.hf) &&
		(receiver==null ? fv.receiver==null:
		 fv.receiver!=null && receiver.equals(fv.receiver));
	}
    }
    static class ArrayValue extends Value {
	final Temp receiver, length;
	ArrayValue(Temp receiver, Temp length) {
	    this.receiver=receiver; this.length=length;
	}
	boolean isArray() { return true; }
	Value map(DisjointSet<Temp> ds, Quad q, int which_pred) {
	    Temp r = map(receiver, ds, q, which_pred);
	    Temp l = map(length, ds, q, which_pred);
	    if (receiver==r && length==l) return this;
	    return new ArrayValue(r, l);
	}
	public int hashCode() {
	    return receiver.hashCode()+length.hashCode();
	}
	public boolean equals(Object o) {
	    if (!(o instanceof ArrayValue)) return false;
	    ArrayValue av = (ArrayValue) o;
	    return receiver.equals(av.receiver)&& length.equals(av.length);
	}
    }
}
