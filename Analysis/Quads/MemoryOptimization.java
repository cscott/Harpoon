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
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.DisjointSet;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

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
 * @version $Id: MemoryOptimization.java,v 1.1.2.1 2001-06-18 04:14:43 cananian Exp $
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
	for (int i=0; i<quads.length; i++) {
	    // delete useless.
	    if (a.useless.contains(quads[i]))
		quads[i].remove();
	    else if (false && quads[i] instanceof PHI) {
		// new parts for some PHIs.
	    } else if (!(quads[i] instanceof HEADER)) // rename
		Quad.replace(quads[i],
			     quads[i].rename(a.tempMap, a.tempMap));
	}
	/*
	System.out.println("AFTER: ");
	hc.print(new java.io.PrintWriter(System.out));
	*/
	// done!
	return hc;
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
	boolean doBlockGET(BasicBlock.Factory bbF, BasicBlock bb) {
	    List quads = bb.statements();
	    // compute in set by merging outs.
	    // map <temp,field>->temp; only present if all inputs have it.
	    // do renaming as per PHI.
	    Map in = new HashMap();
	    /*
	    Quad first = (Quad) quads.get(0);
	    Quad[] pred = first.prev();
	    for (int i=0; i<pred.length; i++) {
		BasicBlock prbb = bbF.getBlock(pred[i]);
		Map pin = (Map) out.get(prbb);
		if (pin==null) continue; // no info yet.
		for (Iterator it=pin.entrySet().iterator(); it.hasNext(); ) {
		    Map.Entry me = (Map.Entry) it.next();
		    Value v = (Value) me.getKey();
		    Temp  t = (Temp) me.getValue();
		    v = v.map(ds, first, i); t = Value.map(t, ds, first, i);
		    if (i==0) in.put(v, t);
		    else if (!in.containsKey(v
		
		for out.getValues(prbb)
		if (i==0) in.putAll((Map)out.get(prbb));
		else in.
	    }
	    */
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
		    Util.assert(ds.find(q.dst())==q.dst());
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
		    Util.assert(ds.find(q.dst())==q.dst());
		    map.put(v, q.dst());
		}
	    }
	    public void visit(ASET q) {
		Value v = new ArrayValue((Temp)ds.find(q.objectref()),
					 (Temp)ds.find(q.index()));
		map.put(v, ds.find(q.src()));
	    }
	    public void visit(MONITORENTER q) {
		map.clear();
	    }
	    public void visit(MONITOREXIT q) {
		map.clear();
	    }
	    public void visit(CALL q) {
		HMethod calls[] = cg.calls(q.getFactory().getMethod(), q);
		for (Iterator it=map.keySet().iterator(); it.hasNext(); ) {
		    Value v = (Value) it.next();
		    if (v.isArray()) it.remove();
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
