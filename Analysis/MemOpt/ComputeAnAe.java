// ComputeAnAe.java, created Mon Apr  1 20:17:07 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import java.lang.reflect.Modifier;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Util.Constraints.ConstraintSolver;
import harpoon.Util.Constraints.Var;
//import harpoon.Util.Constraints.Unfeasible;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.DiGraph;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.FOOTER;

/**
 * <code>ComputeAnAe</code> is an example of using the set constraint solver.
 * Otherwise, it is unnececessary and it will be removed at some point).
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ComputeAnAe.java,v 1.6 2004-02-08 03:19:53 cananian Exp $
 */
public class ComputeAnAe {
    
    /** Creates a <code>ComputeAnAe</code>. */
    public ComputeAnAe(CachingCodeFactory hcf, CallGraph cg, boolean anew) {
	this.hcf = hcf;
	this.cg  = cg;
	this.anew = anew;
	hm2vn = new HashMap();
	hm2ve = new HashMap();

	compute();

	// enable some GC
	this.hcf = null;
	this.cg  = null;
    }

    public ComputeAnAe(CachingCodeFactory hcf, CallGraph cg) {
	this(hcf, cg, false);
    }

    private boolean anew;

    // statistics related things
    /** Enable some statistics on variables and constraints. */
    public static final boolean STATS = true;
    private int nb_var = 0;
    private int nb_vv_cons = 0;
    private int nb_av_cons = 0;

    // work variables
    private CachingCodeFactory hcf;
    private CallGraph cg;

    private Map hm2vn;
    private Map hm2ve;
    private Map sol;
    private ConstraintSolver cs;


    private void create_vars() {
	for(Object hmO : cg.callableMethods()) {
	    HMethod hm = (HMethod) hmO;
	    if(!isAnalyzable(hm)) continue;
	    hm2vn.put(hm, new Var());
	    hm2ve.put(hm, new Var());
	    if(STATS) nb_var++;
	}
    }

    private void add_constraints() {
	for(Object hmO : cg.callableMethods()) {
	    HMethod hm = (HMethod) hmO;
	    if(!isAnalyzable(hm)) continue;

	    System.out.println("Constraints for " + hm);

	    add_constraints(hm, getVn(hm), 0);
	    add_constraints(hm, getVe(hm), 1);
	}
    }


    private void add_constraints(HMethod hm, Var v, int type) {
	Collection allocations = new LinkedList();
	Set seen = new HashSet();
	LinkedList wlist = new LinkedList();

	HCode hcode = hcf.convert(hm);
	FOOTER footer = ((Code)hcode).getRootElement().footer();
	// examine FOOTER's predecessors and select the RETURN or
	// the THROW instructions as roots (function of type)
	int prev_nb = footer.prevLength();
	for(int i = 0; i < prev_nb; i++) {
	    Quad prev = footer.prev(i);
	    int kind = prev.kind();
	    if(((type == 0) && (kind == QuadKind.RETURN)) ||
	       ((type == 1) && (kind == QuadKind.THROW))) {
		seen.add(prev);
		wlist.addLast(prev);
	    }
	}

	// worklist algorithm for reachability
	while(!wlist.isEmpty()) {
	    Quad curr = (Quad) wlist.removeFirst();

	    prev_nb = curr.prevLength();
	    for(int i = 0; i < prev_nb; i++) {
		Quad prev = curr.prev(i);
		int kind = prev.kind();

		if(kind == QuadKind.CALL)
		    deal_with_call(hm, v, curr, (CALL) prev);

		if(seen.add(prev)) {
		    wlist.addLast(prev);
		    if((kind == QuadKind.NEW) ||
		       (anew && (kind == QuadKind.ANEW)))
			allocations.add(prev);
		}
	    }
	}

	cs.addBasicConstraint(allocations, v);
	if(STATS) nb_av_cons++;
    }


    private void deal_with_call(HMethod hm, Var v, Quad curr, CALL call) {
	// true if call->curr is a normal edge, false if it's
	// an exceptional edge; this way we know whether to use getVn or
	// getVe
	boolean on_return = (call.next(0) == curr);

	HMethod[] callees = cg.calls(hm, call);
	for(int i = 0; i < callees.length; i++) {
	    HMethod callee = callees[i];
	    if(!isAnalyzable(callee)) continue;

	    Var v_callee = on_return ? getVn(callee) : getVe(callee);
	    cs.addInclusionConstraint(v_callee, v);
	    if(STATS) nb_vv_cons++;
	}
    }
	

    private void compute() {
	cs = new ConstraintSolver();
	create_vars();
	add_constraints();

	if(STATS)
	    System.out.println("ComputeAnAe " +
			       nb_var + " variable(s) " +
			       nb_vv_cons + " v->v constraint(s) " +
			       nb_av_cons + " a->v constraint(s) ");

	sol = cs.solve();
    }

    // returns the variable corresponding to A_n(hm)
    private Var getVn(HMethod hm) {
	return (Var) hm2vn.get(hm);
    }

    // returns the variable corresponding to A_e(hm)
    private Var getVe(HMethod hm) {
	return (Var) hm2ve.get(hm);
    }

    /** Checks whether we can look at the code of <code>hm</code>. */
    public static boolean isAnalyzable(HMethod hm) {
	int mod = hm.getModifiers();
	return !(Modifier.isAbstract(mod) ||
		 Modifier.isNative(mod));
    }

    /** Returns the set of nodes allocated in <code>hm</code> (or one
        of the transitively called methods), along some path that ends
        in the normal exit of <code>hm</code>. */
    public Set getAn(HMethod hm) {
	Set result = (Set) sol.get(getVn(hm));
	if(result == null)
	    result = Collections.EMPTY_SET;
	return result;
    }

    /** Returns the set of nodes allocated in <code>hm</code> (or one
        of the transitively called methods), along some path that ends
        in the exceptional exit of <code>hm</code>. */
    public Set getAe(HMethod hm) {
	Set result = (Set) sol.get(getVe(hm));
	if(result == null)
	    result = Collections.EMPTY_SET;
	return result;
    }

}
