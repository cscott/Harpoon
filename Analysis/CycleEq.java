// CycleEq.java, created Wed Oct 14 08:15:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.Util.CombineIterator;
import harpoon.Util.FilterIterator;
import harpoon.Util.Util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * <code>CycleEq</code> computes cycle equivalence classes for nodes in
 * a control flow graph, in O(E) time.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CycleEq.java,v 1.4.2.16 1999-06-18 01:47:56 cananian Exp $
 */

public class CycleEq  {
    List elements = new ArrayList();
    Map equiv = new HashMap();
    public CycleEq(HCode hc, boolean edgegraph) {
	Graph g = (edgegraph) ?
	    (Graph) new EdgeGraph(hc) :
	    (Graph) new ElementGraph(hc);
	compute_cyeq(g);
	for (Iterator e = g.primes(); e.hasNext(); ) { /* dfs order */
	    Node n = (Node) e.next();
	    List l = (List) equiv.get(n.cd_class);
	    if (l==null) { l = new LinkedList(); equiv.put(n.cd_class, l); }
	    l.add(0, n.source());
	    elements.add(n.source());
	}
	// make equiv and elements unmodifiable.
	equiv = Collections.unmodifiableMap(equiv);
	elements = Collections.unmodifiableList(elements);
	// throw away all temp info now.
    }
    /** Return <code>Collection</code> of cycle-equivalency
     *	<code>List</code>s. */
    public Collection cdClasses() { return equiv.values(); }
    /** Return <code>List</code> of edges/nodes, in DFS order. */
    public List elements() { return elements; }

    //
    private static void compute_cyeq(Graph g) {
	Map recentSize = new HashMap();
	Map recentClass= new HashMap();
	// Compute CD equivalence classes.
	for (Iterator e = g.elements(); e.hasNext(); ) {
	    Node n = (Node) e.next();
	    /* Compute Hi(n) */
	    int hi0 = g.size(); // how high using backedges only.
	    for (NodeList nlp=n.backedges; nlp!=null; nlp=nlp.next) {
		// looking for (t,n)
		Node t = nlp.n;
		// compute min.
		if (t.dfs_num <= hi0) hi0 = t.dfs_num;
	    }

	    int hi1 = g.size(); // how high through children.
	    for (NodeList nlp=n.childtree; nlp!=null; nlp=nlp.next) {
		Node c = nlp.n;
		if (c.hi < hi1) {
		    hi1 = c.hi;
		}
	    }

	    // find min(hi) through children.
	    int hi2 = 0; // lowest hi through children
	    for (NodeList nlp=n.childtree; nlp!=null; nlp=nlp.next) {
		Node c = nlp.n;
		if (c.hi > hi2) {
		    hi2 = c.hi;
		}
	    }

	    // set Hi(n)
	    n.hi = Math.min(hi0, hi1);

	    // Compute BList(n)
	    n.blist = new BracketList();

	    //  for each child c of n do
	    for (NodeList nlp=n.childtree; nlp!=null; nlp=nlp.next) {
		Node c = nlp.n;
		n.blist.append(c.blist);
	    }
	    //  for each backedge e from a descendant of n to n, do
	    for (NodeList nlp = n.backedges; nlp!=null; nlp=nlp.next) {
		Node d = nlp.n;
		// "to n" is taken care of; make sure source is a descendant
		if (d.dfs_num < n.dfs_num) continue; // not a descendant.
		n.blist.delete(d, n);
	    }
	    //  Also capping backedges:
	    for (NodeList nlp = n.capping; nlp!=null; nlp=nlp.next) {
		Node d = nlp.n;
		n.blist.delete(d, n);
	    }
	    //  for each backedge e from n to an ancestor of n, do
	    for (NodeList nlp = n.backedges; nlp!=null; nlp=nlp.next) {
		Node a = nlp.n;
		// "from n" is taken care of; make sure target is ancestor.
		if (a.dfs_num > n.dfs_num) continue; // not an ancestor.
		Bracket b = new Bracket(n, a);
		n.blist.push(b);
		recentSize.put(b, new Integer(-1)); // n is not a rep. node.
	    }
	    //  if n has more than one child, then
	    if (n.childtree!=null && n.childtree.next!=null) {
		Bracket b = new Bracket(n,g.byNum(hi2)); //capping backedge
		n.blist.push(b);
		recentSize.put(b, new Integer(-1));
		// add edge to node.
		b.ancestor.capping = new NodeList(n, b.ancestor.capping);
	    }

	    // Compute Class(n)
	    //  if n is a representative node
	    if (n.isPrime()) {
		Bracket tbn = n.blist.top();
		int  sbn = n.blist.size();
		if (sbn != ((Integer)recentSize.get(tbn)).intValue()) {
		    // start a new equivalence class.
		    recentSize.put(tbn, new Integer(sbn));
		    // use an anonymous object as the unique 'class name'
		    recentClass.put(tbn, new Object());
		}
		n.cd_class = recentClass.get(tbn);
	    }
	} // for each node
    } // procedure
    
    // ABSTRACT representation of Graphs and Nodes.
    static abstract class Graph {
	/** Entry node. */
	public final Node start = _init_start();
	/** Exit node. */
	public final Node end = _init_end();
	/** Constructor. */
	public Graph() { }
	/** Functions to initialize start and end. */
	abstract Node _init_start();
	abstract Node _init_end();
	/** return the number of nodes in the graph. */
	public int size() { return dfs_order.size(); }
	/** Get a particular node by its dfs order. */
	public Node byNum(int n) { return (Node) dfs_order.get(n); }
	/** Enumerate nodes in reverse depth-first search order. */
	public Iterator elements() {
	    return new Iterator() {
		int i=Graph.this.size();
		public boolean hasNext() { return (i>0); }
		public Object next() {return byNum(--i);}
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    };
	}
	/** Enumerate the primed nodes in depth-first search order. */
	public Iterator primes() {
	    FilterIterator.Filter f = new FilterIterator.Filter() {
		public boolean isElement(Object o) {
		    return ((Node)o).isPrime();
		}
	    };
	    return new FilterIterator(dfs_order.iterator(), f);
	}
	/** Collection interface to nodes. */
	public List asList() {
	    return Collections.unmodifiableList(dfs_order);
	}

	// DFS ORDERING.
	protected void dfs_number() {
	    Set visited = new HashSet();
	    Stack s = new Stack(); // closure stack.
	    s.push(new DFSClosure(start, null/*no parent*/, null));

	    while (!s.isEmpty()) {
		DFSClosure c = (DFSClosure) s.peek();
		Node n = c.n; Node parent = c.p; Iterator e = c.i;
		if (e==null) { // start of visit to node n.
		    Util.assert(!visited.contains(n));
		    visited.add(n); // kilroy was here.
		    n.dfs_num = dfs_order.size();
		    dfs_order.add(n);
		    c.i = e = n._adj_();
		}
		// hacked-apart iteration over n._adj_(),
		// rewritten to allow 'recursion' using the closure stack.
		if (!e.hasNext()) { s.pop(); continue; } // done with iterator
		Node m = (Node) e.next();
		if (!visited.contains(m)) { // a tree edge (child)
		    n.childtree = new NodeList(m, n.childtree);
		    // recurse: dfs_number(m, visited, n);
		    s.push(new DFSClosure(m, n, null));
		} else if (m==parent) { // a tree edge (parent)
		    n.parenttree = new NodeList(m, n.parenttree);
		} else { // a backedge
		    n.backedges = new NodeList(m, n.backedges);
		}
	    }
	}
	private class DFSClosure { // helper class.
	    final Node n, p; Iterator i;
	    DFSClosure(Node n, Node p, Iterator i)
	    { this.n = n; this.p = p; this.i = i; }
	}
	/* OLD VERSION OF dfs_number() -- using real recursion.
	   // kept around because it's *much* easier to understand.
	private void dfs_number(Node n, Set visited, Node parent) {
	    Util.assert(!visited.contains(n));
	    visited.add(n); // kilroy was here.
	    n.dfs_num = dfs_order.size();
	    dfs_order.add(n);
	    for (Iterator e = n._adj_(); e.hasNext(); ) {
		Node m = (Node) e.next();
		if (!visited.contains(m)) { // a tree edge (child)
		    n.childtree = new NodeList(m, n.childtree);
		    dfs_number(m, visited, n);
		} else if (m==parent) { // a tree edge (parent)
		    n.parenttree = new NodeList(m, n.parenttree);
		} else { // a backedge
		    n.backedges = new NodeList(m, n.backedges);
		}
	    }
	}
	*/
	private final List dfs_order = new ArrayList();
    }

    static abstract class Node {
	/** the depth-first search numbering of this node. */
	int dfs_num;
	/** list of brackets originating at this node. */
	BracketList blist;
	/** the lowest dfs_num reachable from this node. */
	int hi;
	/** the control-dependency class of this node. */
	Object cd_class;
	/** List of capping backedges from this node. */
	NodeList capping = null;
	/** List of child edges belonging to the depth-first tree. */
	NodeList childtree = null;
	/** List of edges belonging to the depth-first tree. */
	NodeList parenttree = null;
	/** List of backedges sprouting from this node. */
	NodeList backedges = null;
	/** Return the underlying object for this node. */
	public abstract Object source();
	/** Determine whether this node is prime. */
	public abstract boolean isPrime();
	/** Enumerate all adjacent nodes from scratch, in reverse order. */
	abstract Iterator _adj_();
	/** Enumerate all adjacent nodes from the cache. */
	public Iterator adj() {
	    return new CombineIterator(new Iterator[] {
		parents(), children(), backedges() });
	}
	/** Enumerate all parents of this node in depth-first traversal of
	 *  the graph. */
	public Iterator parents() { return NodeList.elements(parenttree); }
	/** Enumerate all children of this node in depth-first traversal of
	 *  the graph. */
	public Iterator children() { return NodeList.elements(childtree); }
	/** Enumerate all adjacent nodes which are not parents or children of
	 *  the node in a depth-first traversal (targets of backedges). */
	public Iterator backedges() { return NodeList.elements(backedges); }
	/** Pretty-print this node. */
	public String toString() { return "#"+dfs_num+": "+source(); }
	/** hashcode. */
	public int hashCode() {
	    return ((source()==null)?0:source().hashCode() ) + (isPrime()?5:7);
	}

	// ugly hack to keep track of list cell corresponding to a back
	// edge.  The algorithm guys made me do this, honest.  I had no
	// choice.
	public final Map be2lc = new HashMap(7);
    }
    static class NodeList {
	public final Node n;
	public final NodeList next;
	public NodeList(Node n, NodeList next) {
	    this.n = n; this.next = next;
	}
	public static Iterator elements(final NodeList nl) {
	    return new Iterator() {
		NodeList nlp = nl;
		public boolean hasNext() { return nlp!=null; }
		public Object next() {
		    Node n = nlp.n; nlp=nlp.next; return n;
		}
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    };
	}
	public static Set asSet(final NodeList nl) {
	    return new AbstractSet() {
		public boolean isEmpty() { return nl==null; }
		public int size() {
		    int s=0;
		    for (NodeList nlp=nl; nlp!=null; nlp=nlp.next)
			s++;
		    return s;
		}
		public Iterator iterator() { return elements(nl); }
	    };
	}
    }
    // Brackets.
    static class Bracket {
	Node descendant;
	Node ancestor;
	public Bracket(Node descendant, Node ancestor) {
	    Util.assert(descendant.dfs_num >= ancestor.dfs_num);
	    this.descendant = descendant;
	    this.ancestor = ancestor;
	}
	public int hashCode() {
	    return descendant.hashCode() ^ ancestor.hashCode();
	}
	public boolean equals(Object o) {
	    Bracket b;
	    if (o==null) return false;
	    if (o==this) return true; // help alias analysis.
	    try { b = (Bracket) o; }
	    catch (ClassCastException e) { return false; }
	    return (b.descendant.equals(descendant) &&
		    b.ancestor.equals(ancestor));
	}
	public String toString() { return "<"+descendant+","+ancestor+">"; }
    }
    // linked lists of brackets.
    static class BracketList {
	static class ListCell {
	    ListCell prev;
	    Bracket b;
	    ListCell next;
	    ListCell(ListCell prev, Bracket b, ListCell next) {
		this.prev = prev; this.b = b; this.next = next;
	    }
	}
	ListCell first = null;
	ListCell last =  null;
	int size = 0;

	public BracketList() { }
	public int size() { return size; }
	public void push(Bracket e) {
	    first = new ListCell(null, e, first);
	    if (first.next!=null)
		first.next.prev = first;
	    else
		last = first;
	    e.descendant.be2lc.put(e.ancestor, first);
	    size++;
	}
	public Bracket top() { return first.b; }
	public void delete(Node desc, Node ancs) {
	    ListCell lc = (ListCell) desc.be2lc.get(ancs);
	    delete(lc);
	}
	private void delete(ListCell lc) {
	    if (first==lc) first=lc.next;
	    else lc.prev.next = lc.next;
	    if (last==lc) last = lc.prev;
	    else lc.next.prev = lc.prev;
	    size--;
	}
	public void append(BracketList bl) {
	    if (bl.first!=null) {
		if (first==null) {
		    first=bl.first;
		} else { // this and that both length > 0.
		    last.next = bl.first;
		    bl.first.prev=last;
		}
		last = bl.last;
	    }
	    size += bl.size;
	    // invalidate the source bracket-list.
	    bl.first=bl.last=null; bl.size=-1;
	}
	public String toString() {
	    StringBuffer sb=new StringBuffer();
	    sb.append(size);
	    sb.append(": { ");
	    for (ListCell lc=first; lc!=null; lc=lc.next) {
		sb.append(lc.b);
		if (lc.next!=null)
		    sb.append(", ");
	    }
	    sb.append(" }");
	    return sb.toString();
	}
    }
    
    // Representations of node-split graphs, with dedicated END->START edge.

    /** graph based on source ELEMENTS */
    static final class ElementGraph extends Graph {
	Node _init_start() { return new EStartNode(); }
	Node _init_end() { return new EEndNode(); }

	ENode newNode(HCodeElement hce) {
	    ENode n = new ENodePrime(hce);
	    code2node.put(hce, n);
	    return n;
	}
	HCodeElement node2code(ENode n) {
	    return n.source;
	}
	ENode code2node(HCodeElement hce) {
	    ENode n = (ENode) code2node.get(hce);
	    return (n!=null)?n:newNode(hce);
	}
	final Map code2node = new HashMap();
	final Set start_code = new HashSet(7);
	final Set end_code = new HashSet(7);
	ElementGraph(HCode hc) {
	    start_code.add(hc.getRootElement());
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++)
		end_code.add(leaves[i]);
	    dfs_number(); // initialize dfs_number.
	}

	// NODE TYPES
	abstract class ENode extends Node { // abstract node type.
	    final HCodeElement source;

	    ENode(HCodeElement source) {
		this.source = source;
	    }
	    public Object source() { return source; }
	    public boolean isPrime() { return false; }
	    abstract Iterator _adj_();
	}
	class EStartNode extends ENode {
	    EStartNode() { super(null); }
	    Iterator _adj_() {
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeElement)o)).ni;
		    }
		};
		return new CombineIterator(new Iterator[] {
		    Collections.singleton(end).iterator(),
		    new FilterIterator(start_code.iterator(), f) });
	    }

	    public String toString() { return "#"+dfs_num+": start_node"; }
	}
	class EEndNode extends ENode {
	    EEndNode() { super(null); }
	    Iterator _adj_() {
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeElement)o)).no;
		    }
		};
		return new CombineIterator(new Iterator[] {
		    new FilterIterator(end_code.iterator(), f),
		    Collections.singleton(start).iterator() });
	    }
	    public String toString() { return "#"+dfs_num+": end_node"; }
	}
	class ENodePrime extends ENode { // represents a'
	    ENodeIn  ni;
	    ENodeOut no;
	    Iterator _adj_() {
		return Arrays.asList(new Node[] { ni, no }).iterator();
	    }
	    ENodePrime(HCodeElement source) {
		super(source);
		this.ni = new ENodeIn(source);
		this.no = new ENodeOut(source);
	    }
	    public boolean isPrime() { return true; }
	    public String toString() { return super.toString()+"'"; }
	}
	class ENodeIn extends ENode { // represents a_i
	    Iterator _adj_() {
		Iterator e = Arrays.asList(((HasEdges)source).pred()).iterator();
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce.from())).no;
		    }
		};
		e = new CombineIterator(new Iterator[] {
		    new FilterIterator(e, f), // then x_o where x in pred(a)
		    // a' first.
		    Collections.singleton(code2node(source)).iterator() });
		if (!start_code.contains(source)) return e;
		else // link to start node, too.
		    return new CombineIterator(new Iterator[] {
			Collections.singleton(start).iterator(), e });
	    }
	    ENodeIn(HCodeElement source) { super(source); }
	    public String toString() { return super.toString()+"_i"; }
	}
	class ENodeOut extends ENode { // represents a_o
	    Iterator _adj_() {
		Iterator e = Arrays.asList(((HasEdges)source).succ()).iterator();
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce.to())).ni;
		    }
		};
		e = new CombineIterator(new Iterator[] {
		    new FilterIterator(e, f), //then x_i where x in succ(a)
		    // a' first.
		    Collections.singleton(code2node(source)).iterator() });
		if (!end_code.contains(source)) return e;
		else // link to end node, if necessary.
		    return new CombineIterator(new Iterator[] {
			Collections.singleton(end).iterator(), e });
	    }
	    ENodeOut(HCodeElement source) { super(source); }
	    public String toString() { return super.toString()+"_o"; }
	}
    } // end ElementGraph

    /** graph based on source EDGES */
    static final class EdgeGraph extends Graph {
	Node _init_start() { return new EStartNode(); }
	Node _init_end() { return new EEndNode(); }

	ENode newNode(HCodeEdge hce) {
	    ENode n = new ENodePrime(hce);
	    code2node.put(hce, n);
	    return n;
	}
	HCodeEdge node2code(ENode n) {
	    return n.source;
	}
	ENode code2node(HCodeEdge hce) {
	    ENode n = (ENode) code2node.get(hce);
	    return (n!=null)?n:newNode(hce);
	}
	final Map code2node = new HashMap();
	final Set start_code = new HashSet(7);
	final Set end_code = new HashSet(7);
	EdgeGraph(HCode hc) {
	    HCodeElement root = hc.getRootElement();
	    HCodeEdge[] root_succ = ((HasEdges)root).succ();
	    for (int i=0; i<root_succ.length; i++)
		start_code.add(root_succ[i]);
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++) {
		HCodeEdge[] leaf_succ = ((HasEdges)leaves[i]).pred();
		for (int j=0; j<leaf_succ.length; j++)
		    end_code.add(leaf_succ[j]);
	    }
	    dfs_number(); // initialize dfs_number.
	}

	// NODE TYPES
	abstract class ENode extends Node { // abstract node type.
	    final HCodeEdge source;

	    ENode(HCodeEdge source) {
		this.source = source;
	    }
	    public Object source() { return source; }
	    public boolean isPrime() { return false; }
	    abstract Iterator _adj_();
	}
	class EStartNode extends ENode {
	    EStartNode() { super(null); }
	    Iterator _adj_() {
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeEdge)o)).ni;
		    }
		};
		return new CombineIterator(new Iterator[] {
		    Collections.singleton(end).iterator(),
		    new FilterIterator(start_code.iterator(), f) });
	    }

	    public String toString() { return "#"+dfs_num+": start_node"; }
	}
	class EEndNode extends ENode {
	    EEndNode() { super(null); }
	    Iterator _adj_() {
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeEdge)o)).no;
		    }
		};
		return new CombineIterator(new Iterator[] {
		    new FilterIterator(end_code.iterator(), f),
		    Collections.singleton(start).iterator() });
	    }
	    public String toString() { return "#"+dfs_num+": end_node"; }
	}
	class ENodePrime extends ENode { // represents a'
	    ENodeIn  ni;
	    ENodeOut no;
	    Iterator _adj_() {
		return Arrays.asList(new Node[] { ni, no }).iterator();
	    }
	    ENodePrime(HCodeEdge source) {
		super(source);
		this.ni = new ENodeIn(source);
		this.no = new ENodeOut(source);
	    }
	    public boolean isPrime() { return true; }
	    public String toString() { return super.toString()+"'"; }
	}
	class ENodeIn extends ENode { // represents a_i
	    Iterator _adj_() {
		HasEdges from_node = (HasEdges)source.from();
		Iterator e = Arrays.asList(from_node.pred()).iterator();
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce)).no;
		    }
		};
		e = new CombineIterator(new Iterator[] {
		    new FilterIterator(e, f), // then x_o where x in pred(a)
		    // a' first
		    Collections.singleton(code2node(source)).iterator() });
		if (!start_code.contains(source)) return e;
		else // link to start node, too.
		    return new CombineIterator(new Iterator[] {
			Collections.singleton(start).iterator(), e });
	    }
	    ENodeIn(HCodeEdge source) { super(source); }
	    public String toString() { return super.toString()+"_i"; }
	}
	class ENodeOut extends ENode { // represents a_o
	    Iterator _adj_() {
		HasEdges to_node = (HasEdges)source.to();
		Iterator e = Arrays.asList(to_node.succ()).iterator();
		FilterIterator.Filter f = new FilterIterator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce)).ni;
		    }
		};
		e = new CombineIterator(new Iterator[] {
		    new FilterIterator(e, f), //then x_i where x in succ(a)
		    // a' first.
		    Collections.singleton(code2node(source)).iterator() });
		if (!end_code.contains(source)) return e;
		else // link to end node, if necessary.
		    return new CombineIterator(new Iterator[] {
			Collections.singleton(end).iterator(), e });
	    }
	    ENodeOut(HCodeEdge source) { super(source); }
	    public String toString() { return super.toString()+"_o"; }
	}
    } // end EdgeGraph
}
