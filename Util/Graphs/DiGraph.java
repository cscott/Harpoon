// DiGraph.java, created Tue May  6 10:53:15 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.Collections;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

/**
 * <code>DiGraph</code> models a directed graph.  A directed graph is
 * defined by a set of <i>root</i> vertices and a <i>navigator</i>.
 * The navigator is an iterator over the graph: given a vertex
 * <code>v</code>, it gives <code>v</code>'s direct successors (i.e.,
 * vertices pointed to by arcs that start in <code>v</code>), and
 * (optionally), the <code>v</code>'s direct predecessors.  The
 * digraph contains all (transitive and reflexive) successors of the
 * root vertices.
 *
 * <p>There are two kinds of navigators:
 * <code>ForwardNavigator</code>s (gives only successors) and
 * bi-directional <code>Navigator</code> (both successors and
 * predecessors).  For a given graph you should define at least one of
 * them, i.e., you should override at least one of the methods
 * <code>getNavigator</code> and <code>getForwardNavigator</code> from
 * <code>DiGraph</code>.  The standard implementation of these methods
 * in this class is able to construct the other navigator: a
 * bi-directional navigator is trivially a forward one too.  Also, if
 * we know the root vertices and the successor relation, we can
 * produce the predecessor relation (although it's quite costly, and
 * may not terminate for digraphs with an infinite set of vertices).
 *
 * If you decide to implement both (eg, for efficiency) you should
 * make sure the two navigators are consistent.  To see what this
 * means, let <code>fnav</code> and <code>nav</code> be the forward,
 * and the full navigator associated with a digraph.  For any vertex
 * <code>v</code>, <code>fnav.next(v)</code> should returns the same
 * nodes as <code>nav.next(v)</code>.  In addition, <code>nav</code>
 * itself should be consistent, i.e., for any vertices
 * <code>v1</code>, <code>v2</code> from the digraph, if
 * <code>v1</code> appears in <code>nav.next(v2)</code>, then
 * <code>v2</code> should appear in <code>nav.prev(v1)</code>.
 *
 * @see ForwardNavigator
 * @see Navigator
 *
 * Created: Sun May  4 20:56:57 2003
 *
 * @author Alexandru Salcianu <salcianu@mit.edu>
 * @version $Id: DiGraph.java,v 1.10 2004-03-05 22:18:36 salcianu Exp $ */
public abstract class DiGraph<Vertex extends Object> {
    
    /** Returns a set that includes (but is not necessarily limited
        to) the roots of <code>this</code> directed graph.  By
        &quot;roots of a digraph&quot; we mean any set of vertices such
        that one can explore the entire graph by (transitively)
        navigating on their outgoing arcs (using the
        <code>next</code> method of the navigator).  It is OK to
        return ALL the vertices from the digraph. */
    public abstract Set<Vertex> getRoots();

    /** Returns the (bi-directional) navigator for <code>this</code>
        digraph.  The default implementation gets the forward
        navigator by calling <code>getForwardNavigator</code>,
        explores the entire digraph and constructs the predecessor
        relation.  Clearly, this is quite costly, and does not
        terminate for infinite digraphs.

	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getNavigator</code> and
	<code>getForwardNavigator</code>. */
    public Navigator<Vertex> getNavigator() {
	final Relation/*<Vertex,Vertex>*/ prevRel = 
	    new RelationImpl/*<Vertex,Vertex>*/();
	final ForwardNavigator<Vertex> fnav = getForwardNavigator();
	
	for(Vertex vertex : allVertices())
	    for (Vertex next : fnav.next(vertex))
		prevRel.add(next, vertex);

	return new Navigator<Vertex>() {
	    public Vertex[] next(Vertex vertex) { return fnav.next(vertex); }
	    public Vertex[] prev(Vertex vertex) {
		Set prev = prevRel.getValues(vertex);
		return (Vertex[]) prev.toArray(new Object[prev.size()]);
	    }
	};
    }
    
    /** Returns the forward navigator for <code>this</code> digraph.
	The default implementations returns the bi-directional
	navigator (obtained by calling <code>getNavigator</code>).
	
	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getNavigator</code> and
	<code>getForwardNavigator</code>. */
    public ForwardNavigator<Vertex> getForwardNavigator() {
	return getNavigator();
    }


    /** Constructs a <code>DiGraph</code> object.
	@param roots set of root vertices
	@param navigator bi-directional digraph navigator
     */
    public static <Vertex> DiGraph<Vertex> diGraph
	(final Set<Vertex> roots, final Navigator<Vertex> navigator) {
	return new DiGraph<Vertex>() {
	    public Set<Vertex> getRoots() { return roots; }
	    public Navigator<Vertex> getNavigator() { return navigator; }
	};
    }


    /** Constructs a <code>DiGraph</code> object.
	@param roots set of root vertices
	@param navigator forward digraph navigator
     */
    public static <Vertex> DiGraph<Vertex> diGraph
	(final Set<Vertex> roots, final ForwardNavigator<Vertex> fnavigator) {
	return new DiGraph<Vertex>() {
	    public Set<Vertex> getRoots() { return roots; }
	    public ForwardNavigator<Vertex> getForwardNavigator() { 
		return fnavigator;
	    }
	};
    }

    /** @return Set of all vertices from <code>this</code> digraph. */
    public Set<Vertex> vertices() {
	return transitiveSucc(getRoots());
    }

    /** @return Set of all transitive and reflexive successors of
        <code>vertex</code>. */
    public Set<Vertex> transitiveSucc(Vertex vertex) {
    	return transitiveSucc(Collections.singleton(vertex));
    }

    /** @return Set of all transitive and reflexive successors of
        vertices from <code>roots</code>. */
    public Set<Vertex> transitiveSucc(Collection<Vertex> roots) {
	return reachableVertices(roots, getForwardNavigator());
    }

    /** @return Set of all transitive and reflexive
        predecessors of <code>vertex</code> */
    public Set<Vertex> transitivePred(Vertex vertex) {
    	return transitivePred(Collections.singleton(vertex));
    }

    /** @return Set of all transitive and reflexive predecessors of
        the vertices from <code>roots</code>. */
    public Set<Vertex> transitivePred(Collection<Vertex> roots) {
	return reachableVertices
	    (roots, 
	     new ReverseNavigator<Vertex>(getNavigator()));
    }


    /** @return Set of all transitive and reflexive successors of the
        vertices from <code>roots</code>, where the successors of a
        vertex are given by method <code>next</code> of
        <code>fnavigator</code>. */
    public static <Vertex> Set<Vertex> reachableVertices
	(Collection<Vertex> roots,
	 ForwardNavigator<Vertex> fnavigator) {
	return 
	    (new ClosureDFS()).doIt(roots, fnavigator, null, null);
    }


    /** @return one shortest path of vertices from <code>source</code>
	to <code>dest</code>, along edges indicated by
	<code>navigator</code>; returns <code>null</code> if no such
	path exists.  */
    public static <Vertex> List<Vertex> findPath
	(Vertex source, Vertex dest, ForwardNavigator<Vertex> navigator) {

	Map<Vertex,Vertex> pred = new HashMap<Vertex,Vertex>();
	Set<Vertex> reachables = new HashSet<Vertex>();
	LinkedList<Vertex> w = new LinkedList<Vertex>();

	reachables.add(source);
	w.addLast(source);

	boolean found = false;
	while(!w.isEmpty() && !found) {
	    Vertex vertex = w.removeFirst();
	    for (Vertex succ : navigator.next(vertex) ) {
		if(!pred.containsKey(succ)) {
		    pred.put(succ, vertex);
		    if(succ.equals(dest)) {
			found = true;
			break;
		    }
		}
		if(reachables.add(succ))
		    w.addLast(succ);
	    }
	}

	if(!found) return null;

	LinkedList<Vertex> path = new LinkedList<Vertex>();
	path.addFirst(dest);
	Vertex curr = dest;
	while(true) {
	    Vertex vertex = pred.get(curr);
	    path.addFirst(vertex);
	    curr = vertex;
	    if(source.equals(curr))
		break;
	}
	return path;
    }

    /** @return one shortest path of vertices from <code>source</code>
	to <code>dest</code>, along edges from <code>this</code>
	graph; returns <code>null</code> if no such path exists.  */
    public List<Vertex> findPath(Vertex source, Vertex dest) {
	return findPath(source, dest, getForwardNavigator());
    }

    /** Interface for passing a method as parameter to a
     *  <code>forAllReachableVertices</code> object.
     *  @see forAllReachableVertices */
    public static interface VertexVisitor<Vertex> {
	public void visit(Vertex obj);
    }

    /** DFS traversal of <code>this</code> digraph.

	@param onEntry action executed when a node is first time
	visited by the DFS traversal

	@param onExit action executed after the DFS traversal of a
	node finished */
    public void dfs(VertexVisitor<Vertex> onEntry,
		    VertexVisitor<Vertex> onExit) {
	DiGraph.dfs(getRoots(), getForwardNavigator(),
		    onEntry, onExit);
    }

    /** DFS traversal of a (sub) digraph.

	@param roots set of root vertices, starting points for the traversal

	@param navigator forward navigator through the digraph 

	@param onEntry action executed when a node is first time
	visited by the DFS traversal

	@param onExit action executed after the DFS traversal of a
	node finished */
    public static <Vertex> void dfs(Collection<Vertex> roots,
				    ForwardNavigator<Vertex> navigator,
				    VertexVisitor<Vertex> onEntry,
				    VertexVisitor<Vertex> onExit) {
	(new ClosureDFS<Vertex>()).doIt(roots, navigator, onEntry, onExit);
    }

    private static class ClosureDFS<Vertex> {
	public  Set<Vertex> visited;
	private VertexVisitor<Vertex>    onEntry;
	private VertexVisitor<Vertex>    onExit;
	private ForwardNavigator<Vertex> fnav;
	
	public Set<Vertex> doIt(Collection<Vertex> roots,
				ForwardNavigator<Vertex> fnav,
				VertexVisitor<Vertex> onEntry,
				VertexVisitor<Vertex> onExit) {
	    this.fnav    = fnav;
	    this.onEntry = onEntry;
	    this.onExit  = onExit;
	    this.visited = new HashSet();

	    for(Vertex root : roots ) {
		dfs_visit(root);
	    }

	    return visited;
	}

	private void dfs_visit(Vertex v) {
	    // skip already visited nodes
	    if(!visited.add(v)) return;

	    if(onEntry != null)
		onEntry.visit(v);

	    for(Vertex v2 : fnav.next(v))
		dfs_visit(v2);

	    if(onExit != null)
		onExit.visit(v);
	}
    }


    /** Executes an action exactly once for each vertex from
        <code>this</code> directed graph. */
    public void forAllVertices(VertexVisitor<Vertex> action) {
	dfs(action, null);
    }
    

    /** Performs an action exactly once on each vertex reachable from
        the set of vertices <code>roots</code>, via the navigator
        <code>navigator</code>.  This method is supposed to be called
        if:

	<ol>

	<li>you are interested only in a subgraph or

	<li>you have a set of roots and a navigator but didn't package
	them in a digraph.

	</ol>

	If you want to execute an action for all vertices from a
	digraph, use the non-static version of this method. */
    public static <Vertex> void forAllReachableVertices
	(Collection<Vertex> roots,
	 ForwardNavigator<Vertex> navigator,
	 VertexVisitor<Vertex> action) {

	(new ClosureDFS()).doIt(roots, navigator, action, null);
    }


    /** Returns the component graph for <code>this</code> graph.  The
        &quot;component graph&quot; of a graph <code>G</code> is the
        directed, acyclic graph consisting of the strongly connected
        components of <code>G</code>.  */
    public DiGraph<SCComponent/*<Vertex>*/> getComponentDiGraph() {
	final Set<SCComponent/*<Vertex>*/> sccs = SCComponent.buildSCC(this);
	return new DiGraph<SCComponent/*<Vertex>*/>() {
	    public Set<SCComponent/*<Vertex>*/> getRoots() {
		return sccs;
	    }
	    public Navigator<SCComponent/*<Vertex>*/> getNavigator() {
		return SCComponent.SCC_NAVIGATOR;
	    }
	};
    }

    
    /** @return set of vertices from <code>this</code> directed graph. */
    public Set<Vertex> allVertices() {
	return transitiveSucc(getRoots());
    }

    
    /** @return reverse of <code>this</code> directed graph: a
        directed graph with the same set of vertices, that contains an
        arc from <code>v1</code> to <code>v2</code> iff
        <code>this</code> graph contains an arc from <code>v2</code>
        to <code>v1</code>. */
    public DiGraph<Vertex> reverseGraph() {
	final DiGraph<Vertex> origDiGraph = this;
	return new DiGraph<Vertex>() {
	    public Set<Vertex> getRoots() {
		return origDiGraph.allVertices();
	    }
	    public Navigator<Vertex> getNavigator() {
		return
		    new ReverseNavigator<Vertex>
		    (origDiGraph.getNavigator());
	    }
	};
    }

    public String toString() {
	return getRoots().toString();
    }
}
