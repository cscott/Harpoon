// MRA.java, created Mon Oct  1 16:42:17 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
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
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>MRA</code> is a forward data-flow analysis
 * for Quad form that answers the question "which 
 * <code>Temp<code>s contain the address of the most 
 * recently allocated object at this program point?"
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: MRA.java,v 1.1.2.2 2001-10-13 19:24:43 kkz Exp $
 */
public class MRA {

    private final Code code;
    private final BasicBlock.Factory bbf;
    private final CFGrapher cfger;
    private final UseDefer ud;
    private final Map bb2pre;
    private Map bb2post;

    /** Creates a new <code>MRA</code>. Performs analysis. 
     *  If the code is modified, then a new MRA needs to be
     *  created.
     */
    public MRA(Code code) {
	this.code = code;
	this.cfger = CFGrapher.DEFAULT;
	this.ud = UseDefer.DEFAULT;
	this.bbf = new BasicBlock.Factory(code, cfger);
	this.bb2pre = new HashMap();
	this.bb2post = new HashMap();
	analyze();
	// after analysis, we can get rid of the post map
	this.bb2post = null;
    }

    /** Returns the Set of <code>Temp</code>s that
     *  contain the address of the most recently
     *  allocated object at the given program point,
     *  before the given <code>Quad</code> is
     *  executed.  This function is undefined for
     *  <code>PHI<code>s.
     */
    public Set mra_before(Quad q) {
	BasicBlock bb = bbf.getBlock(q);
	Util.assert(bb != null);
	Set pre = (Set) bb2pre.get(bb);
	Util.assert(pre != null);
	Set post = new HashSet(pre);
	for(Iterator it = bb.statements().iterator(); it.hasNext(); ) {
	    Quad curr = (Quad) it.next();
	    if (q.equals(curr))
		break;
	    // PHIs and LABELs (which are also PHIs) are handled specially
	    // so are CJMPs, SWITCHs and TYPESWITCHs, which are actually SIGMAs
	    if (curr.kind() != QuadKind.PHI && 
		curr.kind() != QuadKind.LABEL &&
		curr.kind() != QuadKind.CJMP &&	
		curr.kind() != QuadKind.SWITCH &&	
		curr.kind() != QuadKind.TYPESWITCH)
		transfer(curr, post);
	}
	return Collections.unmodifiableSet(post);
    }


    private void analyze() {
	// create universe
	Set universe = new HashSet();
	for(Iterator it = code.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    universe.addAll(ud.useC(q));
	    universe.addAll(ud.defC(q));
	}
	universe = Collections.unmodifiableSet(universe);
	Set empty = Collections.unmodifiableSet(Collections.EMPTY_SET);
	// initialize the maps
	for(Iterator it = bbf.blocksIterator(); it.hasNext(); )
	    bb2post.put((BasicBlock)it.next(), universe);
	// initialize worklist
	Worklist toprocess = new WorkSet();
	toprocess.push((BasicBlock)bbf.getRoot());
	// keep going until we reached a fixed point
	while(!toprocess.isEmpty()) {
	    BasicBlock bb = (BasicBlock)toprocess.pull();
	    // construct the pre-Set
	    Set pre = preSet(bb);
	    // add the unmodifiable original to the map
	    bb2pre.put(bb, pre);
	    // make a copy
	    Set mra = new HashSet(pre);
	    for(Iterator it = bb.statements().iterator() ; it.hasNext(); ) {
		Quad q = (Quad) it.next();
		// PHIs and LABELs (which are also PHIs) are handled 
		// specially. so are CJMPs, SWITCHs and TYPESWITCHs, 
		// which are actually SIGMAs
		if (q.kind() != QuadKind.PHI && 
		    q.kind() != QuadKind.LABEL &&
		    q.kind() != QuadKind.CJMP &&	
		    q.kind() != QuadKind.SWITCH &&	
		    q.kind() != QuadKind.TYPESWITCH)
		    transfer(q, mra);
	    }
	    // if the post-Set has changed, then update
	    // the map and add successors to worklist
	    if (mra.size() != ((Set)bb2post.get(bb)).size()) {
		bb2post.put(bb, Collections.unmodifiableSet(mra));
		for(Iterator it = bb.nextSet().iterator(); it.hasNext(); )
		    toprocess.push((BasicBlock)it.next());
	    }
	}
    }

    
    // handles the complicated pre-Sets that
    // occur when we have SIGMAs and PHIs
    // returns an unmodifiable set
    private Set preSet(BasicBlock bb) {
	// start with the empty set
	Set mra = new HashSet();
	Quad q = (Quad) bb.statements().get(0);
	// PHIs are special
	if (q.kind() == QuadKind.LABEL ||
	    q.kind() == QuadKind.PHI) {
	    PHI phi = (PHI) q;
	    Quad[] prev = phi.prev();
	    // need to cycle through predecessors
	    for (int i = 0; i < phi.arity(); i++) {
		Set pmra = new HashSet(postSet(bbf.getBlock(prev[i]), bb));
		for (int j = 0; j < phi.numPhis(); j++) {
		    Temp src = phi.src(j, i);
		    // substitute srcs for dsts
		    if (pmra.remove(src))
			pmra.add(phi.dst(j));
		}
		// perform intersection
		if (i == 0)
		    mra.addAll(pmra);
		else
		    mra.retainAll(pmra);
	    }
	} else {
	    // perform straight intersection
	    Iterator it = bb.prevSet().iterator();
	    if (it.hasNext())
		mra.addAll(postSet((BasicBlock)it.next(), bb));
	    while (it.hasNext())
		mra.retainAll(postSet((BasicBlock)it.next(), bb));
	}
	return Collections.unmodifiableSet(mra);
    }


    // handles the complicated post-Sets that
    // occur when we have SIGMAs and PHIs
    // the successor is needed when bb has
    // multiple sucessors, because its post-Set
    // is different for each successor
    // returns an unmodifiable set
    private Set postSet(BasicBlock bb, BasicBlock succ) {
	List stms = bb.statements();
	Quad q = (Quad) stms.get(stms.size()-1);
	// SIGMAs are special, but abstract
	if (q.kind() == QuadKind.CJMP || 
	    q.kind() == QuadKind.SWITCH || 
	    q.kind() == QuadKind.TYPESWITCH) {
	    // find our arity
	    SIGMA sigma = (SIGMA) q;
	    int arity = 0;
	    Quad match = (Quad) succ.statements().get(0);
	    Quad[] next = sigma.next();
	    for ( ; arity < sigma.arity(); arity++) {
		if (match.equals(next[arity])) {
		    // found arity
		    Set post = new HashSet((Set)bb2post.get(bb));
		    for (int i = 0; i < sigma.numSigmas(); i++) {
			// add only dst corresponding to
			// arity and only if src is present
			if (post.remove(sigma.src(i)))
			    post.add(sigma.dst(i, arity));
		    }
		    // make unmodifiable
		    return Collections.unmodifiableSet(post);
		}
	    }
	    // should never get here
	    throw new Error("Cannot find arity of "+succ+" w.r.t. "+bb);
	}
	// otherwise...
	return (Set) bb2post.get(bb);
    }


    // transfer function takes a Quad, the Set of
    // Temps containing the most-recently allocated
    // objects prior to the Quad, and modifies the
    // Set accordingly
    private void transfer(Quad q, Set pre) {
	int kind = q.kind();
	if (kind == QuadKind.ANEW || kind == QuadKind.NEW) {
	    // an ANEW or NEW creates a newly-allocated
	    // object that supercedes all others
	    pre.clear();
	    pre.addAll(ud.defC(q));
	} else if (kind == QuadKind.CALL) {
	    // after a call, we can no longer be
	    // certain of the most recently allocated
	    // object
	    pre.clear();
	} else if (kind == QuadKind.MOVE) {
	    // if the src is an pre, then the dst becomes
	    // one, else the dst cannot be one
	    MOVE move = (MOVE)q;
	    if (pre.contains(move.src()))
		pre.add(move.dst());
	    else
		pre.remove(move.dst());
	} else if (kind == QuadKind.METHOD) {
	    // parameters should not have any effect
	    Util.assert(!pre.removeAll(ud.defC(q)));
	} else if (kind == QuadKind.AGET || 
		   kind == QuadKind.ALENGTH ||
		   kind == QuadKind.COMPONENTOF ||
		   kind == QuadKind.CONST ||
		   kind == QuadKind.GET ||
		   kind == QuadKind.HANDLER ||
		   kind == QuadKind.INSTANCEOF ||
		   kind == QuadKind.OPER) {
	    // when a Temp is re-defined, we can no 
	    // longer be certain it points to the most 
	    // recently-allocated object
	    Util.assert(ud.defC(q).size() == 1);
	    pre.removeAll(ud.defC(q));
	} else if (kind == QuadKind.ARRAYINIT ||
		   kind == QuadKind.ASET ||
		   kind == QuadKind.DEBUG ||
		   kind == QuadKind.FOOTER ||
		   kind == QuadKind.HEADER ||
		   kind == QuadKind.MONITORENTER ||
		   kind == QuadKind.MONITOREXIT ||
		   kind == QuadKind.NOP ||
		   kind == QuadKind.RETURN ||
		   kind == QuadKind.SET ||
		   kind == QuadKind.THROW ||
		   kind == QuadKind.TYPECAST) {
	    // Quads with no defs have no effect
	    Util.assert(ud.defC(q).size() == 0);
	} else {
	    // LABELs, PHIs, SIGMAs, SWITCHs, TYPESWITCHs and XIs 
	    // are not handled, but there should not be any
	    throw new Error("Unknown QuadKind: "+q.kind()+" for "+q);
	}
    }
}

