// InclusionConstraints.java, created Sun Mar 31 21:55:50 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

/** <code>InclusionConstraints</code> is a simple solver for inclusion
    constraints.  A system of inclusion constraints is a set of
    constraints of the form &quot;<code>T1</code> included in
    <code>T2</code>&quot; where each {@link #Term term} <code>Ti</code>
    is either a variable or a set of atoms.

    <p>
    Here is a simple example:
    <ol>
    <li><code>V0</code> included in <code>V1</code>
    <li><code>{1, 2}<code> included in <code>V0</code>
    <li><code>{3}</code> included in <code>V1</code>
    </ol>

    <p>
    Solving a system of inclusion constraints means finding a mapping
    variable -> set of atoms such that when we replace each variable
    with the corresponding set of atoms, all constraints are satisfied.

    A possible solution for the previous constraint system is
    <code>V0 = {1, 2}, V1 = {1, 2, 3}</code>.
    Here's another one
    <code>V0 = {1, 2, 100}, V1 = {1, 2, 3, 100, 1000}</code>.

    <p>
    We'll always study the least possible solution. Note that a
    system of inclusion constraints can be unfeasible: just add the
    constraint <code>V1 included in {}</code> to the previous example.

    @author  Alexandru SALCIANU <salcianu@MIT.EDU>
    @version $Id: InclusionConstraints.java,v 1.1 2002-04-02 23:54:15 salcianu Exp $ */
public class InclusionConstraints {

    /** Root of the term class hierarchy. A term is either a variable
	<code>Var</code> or a set of atoms <code>AtomSet</code>. */
    public static abstract class Term {
	public static final int VAR  = 0;
	public static final int ATOMSET  = 1;
	/** Returns the kind of <code>this</code> object. The result
            has to be <code>VAR</code> or <code>ATOMSET</code>. */
	public abstract int kind();
	/** "Accepts" a <code>TermVisitor</code>. The visitor patterm
            allows us to avoid <code>instanceof</code> tests and
            typecast. */
	public abstract void accept(TermVisitor tv);
    }

    /** Visitor for <code>Term</code>. */
    public static abstract class TermVisitor {
	protected TermVisitor() {}
	public abstract void visit(Term t);
	public void visit(Var var)      { visit((Term) var); }
	public void visit(AtomSet aset) { visit((Term) aset); }
    }

    /** Variable for the inclusion constraints. */
    public static class Var extends Term {
	/** Returns <code>Term.VAR</code>. */
	public final int kind() { return Term.VAR; }

	/** {@inheritDoc} */
	public void accept(TermVisitor tv) { tv.visit(this); }

	// counter used to generate unique ids (for debug)
	static int counter;
	// thread-safe way of getting a unique id!
	private static synchronized int get_id() { return counter++; }

	private int id = get_id();

	/** String representation of <code>this</code> variable:
            \quot;V<i>id</i>&quot; where <i>id</i> is a unique integer
            id. */
	public String toString() { return "V" + id; }
    }

    /** <i>Set of atoms</i> term for the inclusion constraints. */
    public static class AtomSet extends Term {
	private Set set;

	private AtomSet() {}

	/** Construct a set of atoms.
	    @param <code>c</code> is the collection of atoms that are
	    attached to this <code>AtomSet</code>. The constructor
	    does a private copy of <code>c</code> (you can freely
	    modify <code>set<code> afterwards, it won't harm
	    <code>this</code> <code>AtomSet</code>. */
	public AtomSet(Collection c) { this.set = new HashSet(c); }

	/** Returns the undelying set of atoms. */
	Set getSet() { return set; }

	/** {@inheritDoc} */
	public void accept(TermVisitor tv) { tv.visit(this); }

	/** Returns <code>Term.ATOMSET</code>. */
	public final int kind() { return Term.ATOMSET; }

	/** String representation of <code>this</code> set of atoms:
	    simply the string representation of the underlying
	    string. */
	public String toString() { return set.toString(); }
    }

    private Relation smaller = new RelationImpl();
    private Relation bigger  = new RelationImpl();

    /** Adds the inclusion constraint <code>t1</code> included in
	<code>t2</code>. Each <code>t</code>i can be either a variable
	or a set of atoms.
	@see Var
	@see AtomSet */
    public void addConstraint(Term t1, Term t2) {
	smaller.add(t1, t2);
	bigger.add(t2, t1);
    }

    private SCComponent get_sorted_sccs() {
	final Map nm = new HashMap();
	final Map pm = new HashMap();

	SCComponent.Navigator nav = new SCComponent.Navigator() {
		public Object[] next(Object node) {
		    Object[] result = (Object[]) nm.get(node);
		    if(result == null) {
			Collection c = (Collection) smaller.getValues(node);
			result = c.toArray(new Object[c.size()]);
			nm.put(node, result);
		    }
		    return result;
		}
		
		public Object[] prev(Object node) {
		    Object[] result = (Object[]) pm.get(node);
		    if(result == null) {
			Collection c = (Collection) bigger.getValues(node);
			result = c.toArray(new Object[c.size()]);
			pm.put(node, result);
		    }
		    return result;
		}
	    };
	Set keys = smaller.keys();
	Object[] vars = keys.toArray(new Object[keys.size()]);
	return SCCTopSortedGraph.topSort
	    (SCComponent.buildSCC(vars, nav)).getFirst(); 
    }

    /** Solves <code>this</code> system of inclusion constraints.
	@return If the system is feasible, returns a map that assigns
	to each variable from the constraint system a set of atoms;
	this assignment is the least possible solution of the
	system.
	@exception <code>Unfeasible</code> if the system is
	unfeasible. */
    public Map solve() throws Unfeasible {
	// 1. compute SCCs and sort them topologically
	SCComponent first_scc = get_sorted_sccs();

	// 2. process the SCCs in increasing topological order
	Map map = new HashMap();
	for(SCComponent c = first_scc; c != null; c = c.nextTopSort())
	    solve_scc(c, map);

	return final_result(map);
    }

    private void solve_scc(SCComponent scc, Map map) {
	// solution for all variables from scc
	Set s = new HashSet();

	for(int i = 0; i < scc.prevLength(); i++)
	    s.addAll((Set) map.get(scc.prev(i)));

	Object[] nodes = scc.nodes();
	for(int i = 0; i < nodes.length; i++) {
	    Term t = (Term) nodes[i];
	    if(t instanceof AtomSet)
		s.addAll(((AtomSet) t).getSet());
	}

	map.put(scc, s);
    }

    // returns false if the set of atoms / atom t is not included in s.
    private boolean check(Term t, Set s) {
	return
	    !(((t instanceof AtomSet) &&
	      !(s.equals(((AtomSet) t).getSet()))));
    }
    
    private Map final_result(Map map) throws Unfeasible {
	final Map result = new HashMap();
	
	for(Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    SCComponent scc = (SCComponent) entry.getKey();
	    final Set solution = (Set) entry.getValue();
	    
	    Object nodes[] = scc.nodes();
	    for(int i = 0; i < nodes.length; i++) {
		Term t = (Term) nodes[i];
		if(t instanceof Var)
		    result.put(t, solution);
		else if(!check(t, solution))
		    throw
			new Unfeasible(t + " != " + solution);
	    }
	}
	return result;
    }
}
