// CycleEq.java, created Wed Oct 14 08:15:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.Properties.Edges;
import harpoon.Util.ArrayEnumerator;
import harpoon.Util.CombineEnumerator;
import harpoon.Util.FilterEnumerator;
import harpoon.Util.SingletonEnumerator;
import harpoon.Util.Set;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
/**
 * <code>CycleEq</code> computes cycle equivalence classes for nodes in
 * a control flow graph, in O(E) time.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CycleEq.java,v 1.4.2.4 1999-01-12 08:47:18 cananian Exp $
 */

public class CycleEq  {
    Hashtable equiv = new Hashtable();
    public CycleEq(HCode hc, boolean edgegraph) {
	Graph g = (edgegraph) ?
	    (Graph) new EdgeGraph(hc) :
	    (Graph) new ElementGraph(hc);
	compute_cyeq(g);
	for (Enumeration e = g.primes(); e.hasMoreElements(); ) {
	    Node n = (Node) e.nextElement();
	    Set s = (Set) equiv.get(n.cd_class);
	    if (s==null) { s = new Set(); equiv.put(n.cd_class, s); }
	    s.union(n.source());
	}
	// throw away all temp info now.
    }
    public Enumeration cdClasses() { return equiv.elements(); }

    //
    private static void compute_cyeq(Graph g) {
	Hashtable recentSize = new Hashtable();
	Hashtable recentClass= new Hashtable();
	// Compute CD equivalence classes.
	for (Enumeration e = g.elements(); e.hasMoreElements(); ) {
	    Node n = (Node) e.nextElement();
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
	public Node byNum(int n) { return (Node) dfs_order.elementAt(n); }
	/** Enumerate nodes in reverse depth-first search order. */
	public Enumeration elements() {
	    return new Enumeration() {
		int i=size();
		public boolean hasMoreElements() { return (i>0); }
		public Object nextElement() {return byNum(--i);}
	    };
	}
	/** Enumerated the primed nodes in reverse depth-first search order. */
	public Enumeration primes() {
	    FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		public boolean isElement(Object o) {
		    return ((Node)o).isPrime();
		}
	    };
	    return new FilterEnumerator(elements(), f);
	}

	// DFS ORDERING.
	protected void dfs_number() {
	    dfs_number(start, new Set(), null);
	}
	private void dfs_number(Node n, Set visited, Node parent) {
	    Util.assert(!visited.contains(n));
	    visited.union(n); // kilroy was here.
	    n.dfs_num = dfs_order.size();
	    dfs_order.addElement(n);
	    for (Enumeration e = n._adj_(); e.hasMoreElements(); ) {
		Node m = (Node) e.nextElement();
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
	private final Vector dfs_order = new Vector();
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
	abstract Enumeration _adj_();
	/** Enumerate all adjacent nodes from the cache. */
	public Enumeration adj() {
	    return new CombineEnumerator(new Enumeration[] {
		NodeList.elements(parenttree), NodeList.elements(childtree),
		NodeList.elements(backedges) });
	}
	/** Enumerate all parents of this node in depth-first traversal of
	 *  the graph. */
	public Enumeration parents() { return NodeList.elements(parenttree); }
	/** Enumerate all children of this node in depth-first traversal of
	 *  the graph. */
	public Enumeration children() { return NodeList.elements(childtree); }
	/** Enumerate all adjacent nodes which are not parents or children of
	 *  the node in a depth-first traversal (targets of backedges). */
	public Enumeration backedges() { return NodeList.elements(backedges); }
	/** Pretty-print this node. */
	public String toString() { return "#"+dfs_num+": "+source(); }

	// ugly hack to keep track of list cell corresponding to a back
	// edge.  The algorithm guys made me do this, honest.  I had no
	// choice.
	public final Hashtable be2lc = new Hashtable();
    }
    static class NodeList {
	public final Node n;
	public final NodeList next;
	public NodeList(Node n, NodeList next) {
	    this.n = n; this.next = next;
	}
	public static Enumeration elements(final NodeList nl) {
	    return new Enumeration() {
		NodeList nlp = nl;
		public boolean hasMoreElements() { return nlp!=null; }
		public Object nextElement() {
		    Node n = nlp.n; nlp=nlp.next; return n;
		}
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
	    if (!(o instanceof Bracket)) return false;
	    Bracket b = (Bracket) o;
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
	final Hashtable code2node = new Hashtable();
	final Set start_code = new Set();
	final Set end_code = new Set();
	ElementGraph(HCode hc) {
	    start_code.union(hc.getRootElement());
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++)
		end_code.union(leaves[i]);
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
	    abstract Enumeration _adj_();
	}
	class EStartNode extends ENode {
	    EStartNode() { super(null); }
	    Enumeration _adj_() {
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeElement)o)).ni;
		    }
		};
		return new CombineEnumerator(new Enumeration[] {
		    new SingletonEnumerator(end),
		    new FilterEnumerator(start_code.elements(), f) });
	    }

	    public String toString() { return "#"+dfs_num+": start_node"; }
	}
	class EEndNode extends ENode {
	    EEndNode() { super(null); }
	    Enumeration _adj_() {
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeElement)o)).no;
		    }
		};
		return new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(end_code.elements(), f),
		    new SingletonEnumerator(start) });
	    }
	    public String toString() { return "#"+dfs_num+": end_node"; }
	}
	class ENodePrime extends ENode { // represents a'
	    ENodeIn  ni;
	    ENodeOut no;
	    Enumeration _adj_() { 
		return new ArrayEnumerator(new Node[] { ni, no });
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
	    Enumeration _adj_() {
		Enumeration e = new ArrayEnumerator(((Edges)source).pred());
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce.from())).no;
		    }
		};
		e = new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(e, f), // then x_o where x in pred(a)
		    new SingletonEnumerator(code2node(source)) }); // a' first.
		if (!start_code.contains(source)) return e;
		else // link to start node, too.
		    return new CombineEnumerator(new Enumeration[] {
			new SingletonEnumerator(start), e });
	    }
	    ENodeIn(HCodeElement source) { super(source); }
	    public String toString() { return super.toString()+"_i"; }
	}
	class ENodeOut extends ENode { // represents a_o
	    Enumeration _adj_() {
		Enumeration e = new ArrayEnumerator(((Edges)source).succ());
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce.to())).ni;
		    }
		};
		e = new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(e, f), //then x_i where x in succ(a)
		    new SingletonEnumerator(code2node(source)) }); // a' first.
		if (!end_code.contains(source)) return e;
		else // link to end node, if necessary.
		    return new CombineEnumerator(new Enumeration[] {
			new SingletonEnumerator(end), e });
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
	final Hashtable code2node = new Hashtable();
	final Set start_code = new Set();
	final Set end_code = new Set();
	EdgeGraph(HCode hc) {
	    HCodeElement root = hc.getRootElement();
	    HCodeEdge[] root_succ = ((Edges)root).succ();
	    for (int i=0; i<root_succ.length; i++)
		start_code.union(root_succ[i]);
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++) {
		HCodeEdge[] leaf_succ = ((Edges)leaves[i]).pred();
		for (int j=0; j<leaf_succ.length; j++)
		    end_code.union(leaf_succ[j]);
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
	    abstract Enumeration _adj_();
	}
	class EStartNode extends ENode {
	    EStartNode() { super(null); }
	    Enumeration _adj_() {
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeEdge)o)).ni;
		    }
		};
		return new CombineEnumerator(new Enumeration[] {
		    new SingletonEnumerator(end),
		    new FilterEnumerator(start_code.elements(), f) });
	    }

	    public String toString() { return "#"+dfs_num+": start_node"; }
	}
	class EEndNode extends ENode {
	    EEndNode() { super(null); }
	    Enumeration _adj_() {
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			return ((ENodePrime)code2node((HCodeEdge)o)).no;
		    }
		};
		return new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(end_code.elements(), f),
		    new SingletonEnumerator(start) });
	    }
	    public String toString() { return "#"+dfs_num+": end_node"; }
	}
	class ENodePrime extends ENode { // represents a'
	    ENodeIn  ni;
	    ENodeOut no;
	    Enumeration _adj_() { 
		return new ArrayEnumerator(new Node[] { ni, no });
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
	    Enumeration _adj_() {
		Edges from_node = (Edges)source.from();
		Enumeration e = new ArrayEnumerator(from_node.pred());
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce)).no;
		    }
		};
		e = new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(e, f), // then x_o where x in pred(a)
		    new SingletonEnumerator(code2node(source)) }); // a' first.
		if (!start_code.contains(source)) return e;
		else // link to start node, too.
		    return new CombineEnumerator(new Enumeration[] {
			new SingletonEnumerator(start), e });
	    }
	    ENodeIn(HCodeEdge source) { super(source); }
	    public String toString() { return super.toString()+"_i"; }
	}
	class ENodeOut extends ENode { // represents a_o
	    Enumeration _adj_() {
		Edges to_node = (Edges)source.to();
		Enumeration e = new ArrayEnumerator(to_node.succ());
		FilterEnumerator.Filter f = new FilterEnumerator.Filter() {
		    public Object map(Object o) {
			HCodeEdge hce = (HCodeEdge) o;
			return ((ENodePrime)code2node(hce)).ni;
		    }
		};
		e = new CombineEnumerator(new Enumeration[] {
		    new FilterEnumerator(e, f), //then x_i where x in succ(a)
		    new SingletonEnumerator(code2node(source)) }); // a' first.
		if (!end_code.contains(source)) return e;
		else // link to end node, if necessary.
		    return new CombineEnumerator(new Enumeration[] {
			new SingletonEnumerator(end), e });
	    }
	    ENodeOut(HCodeEdge source) { super(source); }
	    public String toString() { return super.toString()+"_o"; }
	}
    } // end EdgeGraph
}
