// DeadCode2.java, created Thu Oct  8 03:11:37 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PSET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Default;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Worklist;
import harpoon.Util.Util;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
/**
 * <code>DeadCode</code> removes dead code 
 * (unused definitions/useless jmps/one-argument phi functions/all moves) from
 * a method.  The analysis is optimistic; that is, it assumes that all code is
 * unused and seeks to prove otherwise.  Also works on LowQuads.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DeadCode.java,v 1.4 2002-04-10 03:00:59 cananian Exp $
 */

public abstract class DeadCode  {

    public static void optimize(harpoon.IR.Quads.Code hc,
				AllocationInformationMap aim) {
	AllocationInformation oldaim = hc.getAllocationInformation();
	// Assume everything's useless.
	Set useful = new HashSet(); // empty set.
	// make a renaming table
	NameMap nm = new NameMap();
	// keep track of defs.
	Map defMap = new HashMap();
	// keep track of PHI/SIGMAs which use certain temps.
	MultiMap useMap = new GenericMultiMap();
	// we'll have a coupla visitors
	LowQuadVisitor v;
	
	// make a worklist (which everything's on, at the beginning)
	WorkSet W = new WorkSet();
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++) {
	    W.push(ql[i]);
	    // build our defMap while we're at it.
	    Temp d[] = ql[i].def();
	    for (int j=0; j<d.length; j++)
		defMap.put(d[j], ql[i]);
	    // and keep track of phis/sigmas which use certain temps.
	    if (ql[i] instanceof PHI || ql[i] instanceof SIGMA)
		for (Iterator it=ql[i].useC().iterator(); it.hasNext(); )
		    useMap.add((Temp)it.next(), ql[i]);
	}
	// ...and a visitor
	v = new UsefulVisitor(W, useful, defMap, nm);
	// look for useful stuff.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    q.accept(v);
	}

	// remove the useless stuff, including useless cjmps/phis
	for (int i=0; i<ql.length; i++)
		W.push(ql[i]);
	v = new EraserVisitor(W, useful, useMap, nm);
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    q.accept(v);
	}

	// Finally, do all the necessary renaming
	ql = (Quad[]) hc.getElements(); // put them all in an array.
	// evil: can't replace the header node. [ack]
	for (int i=0; i<ql.length; i++)
	    if (!(ql[i] instanceof HEADER))
		replace(ql[i], ql[i].rename(nm, nm), oldaim, aim, nm);

    } // end OPTIMIZE METHOD.
    static void replace(Quad oldquad, Quad newquad,
			AllocationInformation oldaim,
			AllocationInformationMap newaim, TempMap tm) {
	Quad.replace(oldquad, newquad);
	// update allocation properties, too.
	if (newaim!=null && (oldquad instanceof ANEW||oldquad instanceof NEW))
	    newaim.transfer(newquad, oldquad, tm, oldaim);
    }

    static class EraserVisitor extends LowQuadVisitor {
	WorkSet W;
	Set useful;
	MultiMap useMap;
	NameMap nm;
	EraserVisitor(WorkSet W, Set useful, MultiMap useMap, NameMap nm) {
	    super(false/*non-strict*/);
	    this.W= W; this.useful= useful; this.useMap= useMap; this.nm= nm;
	}
	void unlink(Quad q) {
	    Edge before = q.prevEdge(0);
	    Edge after  = q.nextEdge(0);
	    Quad.addEdge((Quad)before.from(), before.which_succ(),
			 (Quad)after.to(), after.which_pred() );
	}

	public void visit(Quad q) {
	    // generally, remove it if it's worthless.
	    if (useful.contains(q)) return; // safe.
	    // removing this statement could make its predecessor useless.
	    if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
	    // unlink with vigor.
	    assert q.next().length==1 && q.prev().length==1;
	    unlink(q);
	}
	public void visit(PHI q) {
	    // arity-1 phis are useless.
	    if (q.prev().length == 1) {
		// make a pseudo-MOVE for every useful function in this useless phi.
		for (int i=0; i<q.numPhis(); i++)
		    if (useful.contains(q.dst(i)))
			for (int j=0; j<q.arity(); j++)
			    nm.map(q.dst(i), q.src(i,j));
		// could make a CJMP useless.
		if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
		// unlink it. (fun for the whole family)
		unlink(q);
	    } else {
		// check for unused functions in the phi.
		for (int i=0; i < q.numPhis(); i++) {
		    if (useful.contains(q.dst(i))) continue;
		    // shrink the phi! (secret headhunter's ritual)
		    q.removePhi(i);
		    // decrement i so we're at the right place still;
		    i--;
		}
		// check for phis whose sources are identical
		// and coalesce them.
		phidup.clear();
		for (int i=0; i < q.numPhis(); i++) {
		    Temp[] src = q.src(i);
		    if (phidup.containsKey(src)) { // delete it!
			int prev = ((Integer) phidup.get(src)).intValue();
			nm.map(q.dst(i), q.dst(prev));
			q.removePhi(i--);
			// this could enable more merging in PHI/SIGMAs that
			// use q.dst(i) or q.dst(prev)
			W.addAll(useMap.getValues(q.dst(i)));
			W.addAll(useMap.getValues(q.dst(prev)));
		    } else phidup.put(src, new Integer(i));
		}
	    }
	}
	private final SortedMap phidup = new TreeMap(new Comparator() {
	    public int compare(Object o1, Object o2) {
		Temp[] ta1 = (Temp[])o1, ta2 = (Temp[])o2;
		if (ta1.length!=ta2.length) return ta1.length-ta2.length;
		for (int i=0; i<ta1.length; i++) {
		    int c = Default.comparator.compare(nm.tempMap(ta1[i]),
						       nm.tempMap(ta2[i]));
		    if (c!=0) return c;
		}
		return 0;
	    }
	});

	public void visit(SIGMA q) {
	    // check for unused function in the sigma
	L1:
	    for (int i=0; i < q.numSigmas(); i++) {
		// if any dst is used, skip.
		for (int j=0; j < q.arity(); j++)
		    if (useful.contains(q.dst(i,j))) continue L1;
		// safe to delete. ERASER MAN appears.
		// shrink the sigma function in our secret laboratory.
		q.removeSigma(i);
		// decrement index to keep us steady.
		i--;
	    }
	    // find SIGMAs whose sources are identical, and coalesce them.
	    sigdup.clear();
	    for (int i=0; i < q.numSigmas(); i++) {
		Temp src = nm.tempMap(q.src(i));
		if (sigdup.containsKey(src)) { // delete it!
		    int prev = ((Integer) sigdup.get(src)).intValue();
		    for (int j=0; j<q.arity(); j++) {
			nm.map(q.dst(i,j), q.dst(prev,j));
			// this could enable more merging in PHI/SIGMAs that
			// use q.dst(i,j) or q.dst(prev,j)
			W.addAll(useMap.getValues(q.dst(i,j)));
			W.addAll(useMap.getValues(q.dst(prev,j)));
		    }
		    q.removeSigma(i--);
		} else sigdup.put(src, new Integer(i));
	    }
	}
	private final SortedMap sigdup = new TreeMap(new Comparator() {
	    public int compare(Object o1, Object o2) {
		Temp t1 = (Temp)o1, t2 = (Temp)o2;
		return Default.comparator.compare(nm.tempMap(t1),
						  nm.tempMap(t2));
	    }
	});

	public void visit(CJMP q) {
	    if (q.next(0)==q.next(1) && !matchPS(q, (PHI)q.next(0))) {
		// Mu-ha-ha-ha! KILL THE CJMP!
		// make a pseudo-MOVE for every useful function in this useless sigma.
		for (int i=0; i<q.numSigmas(); i++)
		    for (int j=0; j<q.arity(); j++)
			if (useful.contains(q.dst(i,j)))
			    nm.map(q.dst(i,j), q.src(i));
		// removing this might make a preceding CJMP useless.
		if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
		// shrink the phi (and put it on the worklist)
		((PHI)q.next(0)).removePred(q.nextEdge(1).which_pred());
		W.push(q.next(0));
		// link out the CJMP
		Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(),
			     q.next(0), q.nextEdge(0).which_pred());
	    } else {
		// just shrink the functions.
		visit((SIGMA)q);
	    }
	}
	// Determine if (useful) cjmp and phi args match.
	boolean matchPS(CJMP cj, PHI ph) {
	    // a hashtable makes this easier.
	    PSdup.clear();
	    for (int i=0; i<cj.numSigmas(); i++)
		for (int j=0; j<cj.arity(); j++)
		    PSdup.put(nm.tempMap(cj.dst(i,j)),
			  nm.tempMap(cj.src(i)));

	    int which_pred0 = cj.nextEdge(0).which_pred();
	    int which_pred1 = cj.nextEdge(1).which_pred();
	    for (int i=0; i<ph.numPhis(); i++) {
		if (!useful.contains(nm.tempMap(ph.dst(i))))
		    continue; // not useful, skip.
		if (PSdup.get(nm.tempMap(ph.src(i,which_pred0))) !=
		    PSdup.get(nm.tempMap(ph.src(i,which_pred1))) )
		    return true; // cjmp matters, either in sig or phi.
	    }
	    return false; // not useful!
	}
	private final Map PSdup = new HashMap();
    }

    static class NameMap implements TempMap {
	Map h = new HashMap();
	public Temp tempMap(Temp t) {
	    while (h.containsKey(t))
		t = (Temp) h.get(t);
	    return t;
	}
	public void map(Temp Told, Temp Tnew) { h.put(Told, Tnew); }
	public String toString() { return h.toString(); }
    }

    static class UsefulVisitor extends LowQuadVisitor {
	Worklist W;
	Set useful;
	Map defMap;
	NameMap nm;
	// maps cjmp targets past useless cjmps/phis
	Map jmpMap = new HashMap();
	// maps phi sources past useless cjmps/phis
	Map phiMap = new HashMap();

	UsefulVisitor(Worklist W, Set useful, Map defMap, NameMap nm) {
	    super(false/*non-strict*/);
	    this.W = W;
	    this.useful = useful;
	    this.defMap = defMap;
	    this.nm = nm;
	}
	void markUseful(Quad q) {
	    if (useful.contains(q)) return; // no change.
	    useful.add(q);
	    // all variables used by a useful quad are useful.
	    Temp u[] = q.use();
	    for (int i=0; i<u.length; i++)
		markUseful(u[i]);
	}
	void markUseful(Temp t) {
	    if (useful.contains(t)) return; // no change.
	    useful.add(t);
	    // the quad defining this temp is now useful, too.
	    if (defMap.containsKey(t)) // undefined vars possible.
		W.push(defMap.get(t));
	}
	public void visit(Quad q) {
	    boolean usefound = false;
	    // by default, a quad is useful iff what it defines is useful.
	    Temp d[] = q.def();
	    for (int i=0; i<d.length; i++)
		if (useful.contains(d[i]))
		    usefound = true;
	    // statements that define no variables are safe, however.
	    if (d.length==0) usefound = true;
	    // if it's useful, mark it.
	    if (usefound)
		markUseful(q);
	}

	public void visit(ARRAYINIT q) { // always useful.
	    markUseful(q);
	}

	public void visit(PSET q) {
	    // Pointer writes may be useful
	    markUseful(q);
	}

	public void visit(PCALL q) {
	    // all PCALLs are useful (may have side-effects)
	    useful.add(q);
	    // any variables used are useful.
	    for (int i=0; i<q.paramsLength(); i++)
		markUseful(q.params(i));
	    markUseful(q.retex());
	    markUseful(q.ptr());
	    if (q.retval()!=null) markUseful(q.retval());
	    // process sigmas normally
	    visit((SIGMA)q);
	}
	public void visit(CALL q) {
	    // all CALLs are useful (may have side-effects)
	    useful.add(q);
	    // any variables used are useful.
	    for (int i=0; i<q.paramsLength(); i++)
		markUseful(q.params(i));
	    markUseful(q.retex());
	    if (q.retval()!=null) markUseful(q.retval());
	    // process sigmas normally
	    visit((SIGMA)q);
	}
	public void visit(CJMP q) {
	    // assume all CJMPs are useful (we'll remove useless ones later)
	    useful.add(q);
	    // if this is useful, the condition is useful
	    markUseful(q.test());
	    // process sigmas normally.
	    visit((SIGMA)q);
	}
	public void visit(FOOTER q) { // always useful.
	    markUseful(q);
	}
	public void visit(HANDLER q) { // ACK! never should find.
	    assert false : "DeadCode doesn't work with HANDLERs.";
	}
	public void visit(HEADER q) { // always useful.
	    markUseful(q);
	}
	public void visit(METHOD q) { // always useful.
	    markUseful(q);
	}
	public void visit(MONITORENTER q) { // always useful.
	    markUseful(q);
	}
	public void visit(MONITOREXIT q) { // always useful.
	    markUseful(q);
	}
	public void visit(MOVE q) { // moves are never useful (add to rename map)
	    if (useful.contains(q.dst())) {
		markUseful(q.src());
		nm.map(q.dst(), q.src());
	    }
	}
	public void visit(NOP q) { // never useful.
	}
	public void visit(PHI q) {
	    // Assume all phis are useful (will remove arity-1 phis later)
	    useful.add(q);
	    // check the individual phi functions for usefulness.
	    for (int i=0; i < q.numPhis(); i++)
		if (useful.contains(q.dst(i)))
		    for (int j=0; j<q.arity(); j++)
			markUseful(q.src(i,j));
	}
	public void visit(RETURN q) { // always useful.
	    markUseful(q);
	}
	public void visit(SET q) { // always useful.
	    markUseful(q);
	}
	public void visit(SIGMA q) {
	    // Sigmas are useful iff one of the definitions is useful.
	    for (int i=0; i<q.numSigmas(); i++) {
		if (useful.contains(q.src(i))) continue; //skip already useful sigs.
		for (int j=0; j<q.arity(); j++)
		    if (useful.contains(q.dst(i,j))) // this one's (newly) useful.
			markUseful(q.src(i));
	    }
	}
	public void visit(SWITCH q) {
	    // I'm too lazy to see if the switch actually does anything, so assume
	    // it's always useful.
	    useful.add(q);
	    markUseful(q.index());
	    visit((SIGMA)q);
	}
	public void visit(TYPESWITCH q) {
	    // I'm too lazy to see if the typeswitch actually does anything, so assume
	    // it's always useful.
	    useful.add(q);
	    markUseful(q.index());
	    visit((SIGMA)q);
	}
	public void visit(THROW q) { // always useful.
	    markUseful(q);
	}
    }
}
