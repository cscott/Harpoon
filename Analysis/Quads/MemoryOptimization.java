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
import harpoon.Util.Default;
import harpoon.Util.Collections.DisjointSet;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>MemoryOptimization</code> reduces the number of memory operations
 * by combining multiple loads/stores to the same field/array element.
 * It should be safe with respect to the revised Java memory model.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MemoryOptimization.java,v 1.1.2.6 2001-11-08 00:22:52 cananian Exp $
 */
public final class MemoryOptimization
    extends harpoon.Analysis.Transformation.MethodMutator {
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
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	/*
	System.out.println("BEFORE: ");
	hc.print(new java.io.PrintWriter(System.out));
	*/
	// do the analysis.
	Analysis a = new Analysis(hc);
	// okay, rename quads and eliminate useless stuff.
	Quad[] quads = (Quad[]) hc.getElements();
	// add moves to edges.
	for (Iterator it=a.moves.keySet().iterator(); it.hasNext(); ) {
	    Edge e = (Edge) it.next(); Quad q = (Quad) e.to();
	    for (Iterator it2=a.moves.getValues(e).iterator(); it2.hasNext();){
		List pair = (List) it2.next();
		e = addAt(e, new MOVE(q.getFactory(), q,
				      (Temp)a.ds.find(pair.get(0)),
				      (Temp)a.ds.find(pair.get(1))));
	    }
	}
	for (int i=0; i<quads.length; i++) {
	    // delete useless.
	    if (a.useless.contains(quads[i]))
		quads[i].remove();
	    else if (!(quads[i] instanceof HEADER)) // rename
		Quad.replace(quads[i],
			     quads[i].rename(a.tempMap, a.tempMap));
	}
	//DeadCode.optimize((harpoon.IR.Quads.Code)hc, null);
	/*
	System.out.println("AFTER: ");
	hc.print(new java.io.PrintWriter(System.out));
	*/
	// done!
	return hc;
    }
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	// make SSA into RSSx.
	Util.assert(hc.getName().equals(QuadSSA.codename));
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
    }
    protected String mutateCodeName(String codeName) {
	Util.assert(codeName.equals(QuadSSA.codename));
	return MyRSSx.codename;
    }
	/** helper routine to add a quad on an edge. */
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }


    class Analysis {
	/** <BasicBlock,temp,field> -> temp */
	final Map out = new HashMap();
	/** Discovered Temp identities. */
	final DisjointSet ds = new DisjointSet();
	/** Set of useless stores */
	final Set useless = new HashSet();
	/** tempmap view of ds. */
	final TempMap tempMap = new TempMap() {
	    public Temp tempMap(Temp t) { return (Temp) ds.find(t); }
	};
	/** <BasicBlock,Value>->Temp */
	private final Map tempgen = new HashMap();
	Temp tempgen(BasicBlock bb, Value v, Temp template) {
	    List key = Default.pair(bb, v);
	    if (!tempgen.containsKey(key)) {
		tempgen.put(key, new Temp(template));
	    }
	    return (Temp) tempgen.get(key);
	}
	/** Edge -> set of moves */
	final MultiMap moves = new GenericMultiMap();
	/* set of phis we're adding moves for */
	final Set phiadded = new HashSet();

	Analysis(HCode hc) {
	    BasicBlock.Factory bbF = new BasicBlock.Factory(hc);
	    WorkSet w = new WorkSet(bbF.blockSet());
	    // forward dataflow for READ propagation.
	    while (!w.isEmpty()) {
		BasicBlock bb = (BasicBlock) w.pop();
		if (doBlockGET(bbF, bb))  // if block out has changed...
		    w.addAll(bb.nextSet()); // ... add successors to workset.
	    }
	    // backwards dataflow for WRITE propagation.
	    /*
	    w.addAll(bbF.blockSet());
	    while (!w.isEmpty()) {
		BasicBlock bb = (BasicBlock) w.pop();
		if (doBlockSET(bbF, bb))  // if block out has changed...
		    w.addAll(bb.prevSet()); // ... add successors to workset.
	    }
	    */
	    // done.
	}
	Map valuemaptempmap(Map m, Quad q, int which_pred) {
	    if (m==null) return null;
	    HashMap result = new HashMap();
	    for (Iterator it=m.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry me = (Map.Entry) it.next();
		Value v = ((Value)me.getKey()).map(ds, q, which_pred);
		Temp t = (Temp) ds.find(me.getValue());// not phi-mapped
		Temp tt = Value.map(t, ds, q, which_pred);// phi-mapped.
		result.put(v, new Temp[] { tt, t });
	    }
	    return result;
	}

	boolean doBlockGET(BasicBlock.Factory bbF, BasicBlock bb) {
	    List quads = bb.statements();
	    // compute in set by merging outs.
	    // map <temp,field>->temp; only present if all inputs have it.
	    // do renaming as per PHI.
	    Map in = new HashMap();
	    Quad first = (Quad) quads.get(0);
	    Quad[] pred = first.prev();
	    // collect maps from each pred.
	    Map[] prin = new Map[pred.length];
	    for (int i=0; i<pred.length; i++) {
		BasicBlock prbb = bbF.getBlock(pred[i]);
		prin[i] = valuemaptempmap((Map) out.get(prbb), first, i);
	    }
	    // merge key sets
	    Set merged=null; boolean allknown=true;
	    for (int i=0; i<pred.length; i++) {
		if (prin[i]==null) { allknown=false; continue; }// no info yet.
		if (merged==null) merged = prin[i].keySet();
		else merged.retainAll(prin[i].keySet());
		moves.remove(first.prevEdge(i));
	    }
	    // handle merged inputs.
	    if (merged!=null && allknown)
		for (Iterator it=merged.iterator(); it.hasNext(); ) {
		    Value v = (Value) it.next();
		    Temp t=null; boolean same=true;
 		    for (int i=0; i<pred.length; i++) {
 			if (prin[i]==null) continue; // no info.
			// tt is the phi-mapped version of the temp.
			Temp tt = ((Temp[]) prin[i].get(v))[0];
			if (t==null) t=tt;
			else if (!t.equals(tt)) same=false;
		    }
		    if (t==null) continue; // skip this.
		    if (same && !phiadded.contains(Default.pair(bb, v)))
			in.put(v, t);
		    else {
			Temp nt = (Temp) ds.find(tempgen(bb, v, t));
			for (int i=0; i<pred.length; i++) {
			    if (prin[i]==null) continue;
			    // note that we use the un-phi-mapped temp here.
			    Temp tt = ((Temp[]) prin[i].get(v))[1];
			    moves.add(first.prevEdge(i), Default.pair(nt, tt));
			}
			in.put(v, nt);
			phiadded.add(Default.pair(bb, v));
		    }
		}
		    

	    ReadVisitor rv = new ReadVisitor(in);
	    for (Iterator it=quads.iterator(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (!useless.contains(q))
		    q.accept(rv);
	    }
	    // okay, now we have out map.
	    Map oldout = (Map) out.put(bb, in);
	    return (oldout==null || !oldout.equals(in));
	}
	class ReadVisitor extends QuadVisitor {
	    final Map map;
	    ReadVisitor(Map map) { this.map = map; }
	    public void visit(Quad q) { /* do nothing */ }
	    public void visit(GET q) {
		Value v = new FieldValue
		    (q.field(), q.isStatic()?null:(Temp)ds.find(q.objectref()));
		if (map.containsKey(v)) {
		    ds.union(q.dst(), (Temp) map.get(v));
		    useless.add(q);
		} else {
		    map.put(v, q.dst());
		}
	    }
	    public void visit(SET q) {
		Value v = new FieldValue
		    (q.field(), q.isStatic()?null:(Temp)ds.find(q.objectref()));
		map.put(v, ds.find(q.src()));
	    }
	    public void visit(MOVE q) {
		ds.union(q.dst(), q.src());
		useless.add(q);
	    }
	    public void visit(AGET q) {
		Value v = new ArrayValue((Temp)ds.find(q.objectref()),
					 (Temp)ds.find(q.index()));
		if (map.containsKey(v)) {
		    ds.union(q.dst(), (Temp) map.get(v));
		    useless.add(q);
		} else {
		    map.put(v, q.dst());
		}
	    }
	    public void visit(ASET q) {
		Value v = new ArrayValue((Temp)ds.find(q.objectref()),
					 (Temp)ds.find(q.index()));
		map.put(v, ds.find(q.src()));
	    }
	    public void visit(MONITORENTER q) { visitMONITOR(q); }
	    public void visit(MONITOREXIT q) { visitMONITOR(q); }
	    public void visitMONITOR(Quad q) {
		Util.assert(q instanceof MONITORENTER ||
			    q instanceof MONITOREXIT);
		for (Iterator it=map.keySet().iterator(); it.hasNext(); ) {
		    Value v=(Value) it.next();
		    if (v.isArray()) continue;
		    if (!Modifier.isFinal(v.field().getModifiers()))
			it.remove();
		}
	    }
	    
	    public void visit(CALL q) {
		HMethod calls[] = cg.calls(q.getFactory().getMethod(), q);
		for (Iterator it=map.keySet().iterator(); it.hasNext(); ) {
		    Value v = (Value) it.next();
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
	abstract Value map(DisjointSet ds, Quad q, int which_pred);
	static final Temp map(Temp t,
			      DisjointSet ds, Quad q, int which_pred) {
	    t = (Temp) ds.find(t);
	    if (q instanceof PHI) {
		PHI phi = (PHI) q;
		for (int i=0; i<phi.numPhis(); i++)
		    if (phi.src(i, which_pred).equals(t))
			return (Temp) ds.find(phi.dst(i));
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
	Value map(DisjointSet ds, Quad q, int which_pred) {
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
	Value map(DisjointSet ds, Quad q, int which_pred) {
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
