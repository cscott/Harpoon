// ConstraintSolver.java, created Sun Apr  7 15:16:28 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collection;

import harpoon.Analysis.PointerAnalysis.PAWorkList;
import harpoon.Util.Graphs.DiGraph;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.TopSortedCompDiGraph;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationEntryVisitor;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.PredicateWrapper;

import harpoon.Util.Util;


/**
 * <code>ConstraintSolver</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ConstraintSolver.java,v 1.3 2004-03-04 22:32:24 salcianu Exp $
 */
public class ConstraintSolver {

    // all variables appearing in this set of constraints
    private Set all_vars = new HashSet();

    // <v2, v2> in vv_subset  iff  v1 \subseteq v2
    private Relation vv_subset = new RelationImpl();

    // list of general constraints
    private LinkedList cl = new LinkedList();

    public void addBasicConstraint(Collection atoms, Var v) {
	addConstraint(new BasicConstraint(atoms, v));
    }

    // adds the constraint "v1 \subseteq v2"
    public void addInclusionConstraint(Var v1, Var v2) {
	vv_subset.add(v1, v2);
	all_vars.add(v1);
	all_vars.add(v2);
    }

    // add general constraint
    public void addConstraint(Constraint c) {
	cl.add(c);
	Var[] in_dep = c.in_dep();
	for(int i = 0; i < in_dep.length; i++)
	    all_vars.add(in_dep[i]);
	Var[] out_dep = c.out_dep();
	for(int i = 0; i < out_dep.length; i++)
	    all_vars.add(out_dep[i]);
    }

    // find the values of all the variables
    public Map solve() {
	return solve(all_vars);
    }

    // solve the system of constraints
    public Map solve(Set relevant_vars) {
	// initialize v->solution and v->delta(solution) maps
	mv2sol = new HashMap();
	mv2delta = new HashMap();
	master_vars = new HashSet();

	// 1. use simple inclusion constraints to compute sets of
	// equal variables; for each such set, select a master
	// variable; from now on we work only with constraints on the
	// master variables (+ v2master map).
	build_equivalence_classes(relevant_vars);

	Set key_vars = project_set(relevant_vars, v2master);
	construct_data_structs();

	// 2. compute SCCs of master vars according to the dependencies from
	// all the constraints (simple + general)
	TopSortedCompDiGraph/*<Var>*/ ts_scc_vars = construct_sccs(key_vars);

	// 3. process the SCC in topological order
	// 3. 0 create data structs used by the fixed point
	w_intra_scc = new PAWorkList();
	// 3. 1 fixed point over each SCC
	for(Object scc0 : ts_scc_vars.incrOrder())
	    solve_scc((SCComponent) scc0);

	// 4. compute final solution
	Map retval = compute_final_solution(relevant_vars);

	// enable some GC
	mv2sol      = null;
	mv2delta    = null;
	w_intra_scc = null;
	new_delta   = null;
	master_vars = null;
	return retval;
    }

    // Is there any loop dependency?  Ie, is there any constraint c
    // such that c both reads and modifies v?  This is important,
    // because in this case, after we propagate delta(v) along all the
    // constraints going out of v, we cannot set delta(v) to emptyset:
    // some new values might have been added to delta(v); see details
    // in solve_scc.
    private boolean loops;

    // <a, b> in vv_superset iff  b \subseteq a
    // vv_superset is the reverse of vv_subset; we don't maintain it
    // all the time, we just compute it when we need it (in solve())
    private Relation vv_superset;

    // map variable v -> master variable for v's equivalence class
    private Map v2master = new HashMap();

    // v -> constraints that may propagate values from v
    private Relation vv_const_out = new RelationImpl();

    // v -> variables that depend on v
    private Relation forward_dep;
    // v -> variables that v depends on
    private Relation backward_dep;

    // same as vv_subset but operates only with master variables
    // <v1, v2> in w_vv_subset  iff  v1 \subseteq v2
    private Relation w_vv_subset;
    
    private Map mv2sol;

    // computes the sets of variables that are equal (due to mutual
    // inclusions)
    private void build_equivalence_classes(Set relevant_vars) {
	vv_superset = vv_subset.revert(new RelationImpl());

	Navigator nav = new Navigator() {
		final Map next_map = new HashMap();
		final Map pred_map = new HashMap();
		
		public Object[] next(Object node) {
		    Object[] result = (Object[]) next_map.get(node);
		    if(result == null) {
			Set s = vv_superset.getValues(node);
			result = s.toArray(new Object[s.size()]);
			next_map.put(node, result);
		    }
		    return result;
		}
		
		public Object[] prev(Object node) {
		    Object[] result = (Object[]) pred_map.get(node);
		    if(result == null) {
			Set s = vv_subset.getValues(node);
			result = s.toArray(new Object[s.size()]);
			pred_map.put(node, result);
		    }
		    return result;
		}
	    };

	// 1. construct inclusion digraph (relation);
	DiGraph.diGraph(relevant_vars, nav).
	    // 2. find its equivalence classes;
	    getComponentDiGraph().
	    forAllVertices
	    (new DiGraph.VertexVisitor/*<SCComponent<Var>>*/() {
		public void visit(Object/*SCComponent<Var>*/ scc0) {
		    SCComponent scc = (SCComponent/*<Var>*/) scc0;
		    Object nodes[] = scc.nodes();
		    // 3. pick one "master" variable from each class
		    Var master_v = (Var) nodes[0];
		    master_vars.add(master_v);
		    // 4. map all vars from sccs to master_v
		    for(int i = 0; i < nodes.length; i++)
			v2master.put((Var) nodes[i], master_v);
		}
	    });
    }

    Set master_vars;

    private void construct_data_structs() {
	// initialize (partial) solution
	for(Object vO : master_vars) {
	    Var v = (Var) vO;

	    //System.out.println("Master var: " + v);

	    // initialize the solution for the master var. to empty
	    mv2sol.put(v, new HashSet());
	    // delta sol. is also empty
	    mv2delta.put(v, new HashSet());	    
	}

	forward_dep = new RelationImpl();

	PSolAccesser sacc = new PSolAccesser() {
		public Set getSet(Var v) {
		    assert false : "Shouldn't be called!";
		    return null;
		}
		public Set getDeltaSet(Var v) {
		    assert false : "Shouldn't be called!";
		    return null;
		}
		public void updateSet(Var v, Set delta) {

		  //System.out.println("updateSet(" + v + ", " + delta + ")");

		    ((Set) mv2sol.get(v)).addAll(delta);
		    ((Set) mv2delta.get(v)).addAll(delta);
		}
		public void updateSetWithOneElem(Var v, Object elem) {
		    assert false : "Shouldn't be called!";
		}
	    };

	loops = false;

	for(Iterator it = cl.iterator(); it.hasNext(); ) {
	    Constraint c = ((Constraint) it.next()).convert(v2master);
	    Var[] in_v  = c.in_dep();
	    Var[] out_v = c.out_dep();
	    if(in_v.length != 0) {
		for(int i = 0; i < in_v.length; i++) {
		    vv_const_out.add(in_v[i], c);
		    for(int j = 0; j < out_v.length; j++) {
			loops = loops || (in_v[i] == out_v[j]);
			forward_dep.add(in_v[i], out_v[j]);
		    }
		}
	    }
	    else {
		// constraints with 0 in-dependencies can be
		c.action(sacc); // fully applied now!
	    }
	}

	w_vv_subset = new RelationImpl();
	vv_subset.forAllEntries(new RelationEntryVisitor() {
		public void visit(Object value, Object key) {
		    Object valuep = v2master.get(value);
		    Object keyp = v2master.get(key);
		    // ignore self-inclusions
		    if(valuep != keyp) {
			w_vv_subset.add(valuep, keyp);
			forward_dep.add(valuep, keyp);
		    }
		}
	    });

	backward_dep = forward_dep.revert(new RelationImpl());
    }

    // returns the projection of a set through a map
    private Set project_set(Set set, Map map) {
	Set result = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); )
	    result.add(map.get(it.next()));
	return result;
    }

    // compute the sets of mutually dependent variables
    private TopSortedCompDiGraph/*<Var>*/ construct_sccs(Set key_vars) {
	Navigator nav = new Navigator() {
		public Object[] next(Object node) {
		    Var v = (Var) node;
		    Set s = backward_dep.getValues(v);
		    return s.toArray(new Object[s.size()]);
		}

		public Object[] prev(Object node) {
		    Var v = (Var) node;
		    Set s = forward_dep.getValues(v);
		    return s.toArray(new Object[s.size()]);
		}
	    };

	return new TopSortedCompDiGraph/*<Var>*/(key_vars, nav);
    }


    // iterate (fix point) to compute the solution for the variables from scc
    private void solve_scc(SCComponent scc) {

	//System.out.println("solve_scc(" + scc.nodeSet() + ")");

	current_scc = scc;

	Object nodes[] = scc.nodes();
	for(int i = 0; i < nodes.length; i++)
	    w_intra_scc.add(nodes[i]);
	
	while(!w_intra_scc.isEmpty()) {
	    Var v = (Var) w_intra_scc.remove();

	    //System.out.println(" Processing Var " + v);

	    current_v = v;

	    if(loops) new_delta = new HashSet();

	    for(Object cO : vv_const_out.getValues(v)) {
		Constraint c = (Constraint) cO;
		
		//System.out.println("    " + c);

		c.action(sacc_scc);
	    }

	    for(Object v2O : subset_of(v)) {
		Var v2 = (Var) v2O;
		//System.out.println("    Apply " + v + " \\subseteq " + v2);
		sacc_scc.updateSet(v2, sacc_scc.getDeltaSet(v));
	    }

	    if(loops)
		mv2delta.put(v, new_delta);
	    else
		((Set) mv2delta.get(v)).clear();
	}
	
    }

    // returns the set of variables v2 such that v \subseteq v2
    private Set subset_of(Var v) {
	return w_vv_subset.getValues(v);
    }

    // working data for solve_scc
    private SCComponent current_scc;
    private Var current_v;
    private Set new_delta;
    private Map mv2delta;
    private PAWorkList w_intra_scc;

    private PSolAccesser sacc_scc = new PSolAccesser() {
	    public Set getSet(Var v) {
		Set retval = (Set) mv2sol.get(v);
		assert retval != null;
		return retval;
	    }

	    public Set getDeltaSet(Var v) {
		Set retval = (Set) mv2delta.get(v);
		assert retval != null;
		return retval;
	    }

	    public void updateSet(Var v, Set delta) {
		Set s = (Set) mv2sol.get(v);
		Set d =
		    (loops && (v == current_v)) ? 
		    new_delta : ((Set) mv2delta.get(v));

		for(Iterator it = delta.iterator(); it.hasNext(); ) {
		    Object elem = it.next();
		    if(s.add(elem))
			d.add(elem);
		}

		// if we really discovered new things about v, and
		// v is from the current scc, add it to the worklist.
		if(!d.isEmpty() && current_scc.contains(v))
		    w_intra_scc.add(v);		    
	    }

	    public void updateSetWithOneElem(Var v, Object elem) {
		Set s = (Set) mv2sol.get(v);
		Set d =
		    (loops && (v == current_v)) ? 
		    new_delta : ((Set) mv2delta.get(v));

		if(s.add(elem))
		    d.add(elem);

		// if we really discovered new things about v, and
		// v is from the current scc, add it to the worklist.
		if(!d.isEmpty() && current_scc.contains(v))
		    w_intra_scc.add(v);		    
	    }

	};

    // compute the mapping var v -> solution for v, \forall relevant v's
    private Map compute_final_solution(Set relevant_vars) {
	Map v2sol = new HashMap();
	for(Object vO : relevant_vars) {
	    Var v = (Var) vO;
	    Var vm = (Var) v2master.get(v);
	    Set  s = (Set) mv2sol.get(vm);
	    v2sol.put(v, s);
	}
	return v2sol;
    }

    /** ConstraintSolver example */
    public static void main(String[] args) {
	
	ConstraintSolver cs = new ConstraintSolver();
	Var v0 = new Var();
	Var v1 = new Var();
	Var v2 = new Var();
	Var v3 = new Var();
	Var v4 = new Var();
	Var v5 = new Var();

	Set set1 = new HashSet();
	set1.add(new Integer(1));
	set1.add(new Integer(2));

	Set set2 = new HashSet();
	set2.add(new Integer(3));
	set2.add(new Integer(4));
	set2.add(new Integer(5));

	PredicateWrapper no4 = new PredicateWrapper() {
		public boolean check(Object obj) {
		    try{
			Integer i = (Integer) obj;
			return (i.intValue() != 4);
		    }
		    catch(ClassCastException e) {
			return true;
		    }
		}
		public String toString() { return "no-4"; }
	    };

	cs.addInclusionConstraint(v0, v1);
	cs.addInclusionConstraint(v1, v2);	
	cs.addConstraint(new DiffInclConstraint(v3, no4, v1));
	cs.addInclusionConstraint(v2, v3);
	cs.addInclusionConstraint(v3, v4);

	cs.addInclusionConstraint(v5, v3);
	cs.addInclusionConstraint(v3, v5);

	cs.addBasicConstraint(set2, v2);
	cs.addBasicConstraint(set1, v0);

	System.out.println(cs.solve());
    }

}
