// CycleEq.java, created Wed Oct 14 08:15:53 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.Properties.Edges;
import harpoon.Util.ArrayEnumerator;
import harpoon.Util.Set;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
/**
 * <code>CycleEq</code> computes cycle equivalence classes for nodes in
 * control flow graph, in O(E) time.
 * 
 * @author  C. Scott Ananian <>
 * @version $Id: CycleEq.java,v 1.2 1998-10-16 12:00:49 cananian Exp $
 */

public class CycleEq  {
    Hashtable equiv = new Hashtable();
    public CycleEq(HCode hc) {
	Graph g = new Graph(hc);
	compute_cyeq(g);
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement)e.nextElement();
	    Node n = g.code2node(hce);
	    Set s = (Set) equiv.get(n.cd_class);
	    if (s==null) { s = new Set(); equiv.put(n.cd_class, s); }
	    s.union(hce);
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
	    for (Enumeration ee=n.backedges(); ee.hasMoreElements(); ){
		// looking for (t,n)
		Node t = (Node) ee.nextElement();
		// compute min.
		if (t.dfs_num <= hi0) hi0 = t.dfs_num;
	    }

	    int hi1 = g.size(); // how high through children.
	    for (Enumeration ee=n.children(); ee.hasMoreElements(); ) {
		Node c = (Node) ee.nextElement();
		if (c.hi < hi1) {
		    hi1 = c.hi;
		}
	    }

	    // find min(hi) through children.
	    int hi2 = 0; // lowest hi through children
	    for (Enumeration ee=n.children(); ee.hasMoreElements(); ) {
		Node c = (Node) ee.nextElement();
		if (c.hi > hi2) {
		    hi2 = c.hi;
		}
	    }

	    // set Hi(n)
	    n.hi = Math.min(hi0, hi1);

	    // Compute BList(n)
	    n.blist = new BracketList();

	    //  for each child c of n do
	    int nchild = 0; // also count number of children.
	    for (Enumeration ee=n.children(); ee.hasMoreElements(); ) {
		Node c = (Node) ee.nextElement();
		n.blist.append(c.blist);
		nchild++;
	    }
	    //  for each backedge e from a descendant of n to n, do
	    for (Enumeration ee=n.backedges(); ee.hasMoreElements(); ){
		Node d = (Node) ee.nextElement();
		// "to n" is taken care of; make sure source is a descendant
		if (d.dfs_num < n.dfs_num) continue; // not a descendant.
		n.blist.delete(new Bracket(d, n));
	    }
	    //  Also capping backedges:
	    for (Enumeration ee=n.capping.elements(); ee.hasMoreElements(); ){
		Node d = (Node) ee.nextElement();
		n.blist.delete(new Bracket(d, n));
	    }
	    //  for each backedge e from n to an ancestor of n, do
	    for (Enumeration ee=n.backedges(); ee.hasMoreElements(); ){
		Node a = (Node) ee.nextElement();
		// "from n" is taken care of; make sure target is ancestor.
		if (a.dfs_num > n.dfs_num) continue; // not an ancestor.
		Bracket b = new Bracket(n, a);
		n.blist.push(b);
		recentSize.put(b, new Integer(-1)); // n is not a rep. node.
	    }
	    //  if n has more than one child, then
	    if (nchild > 1) {
		Bracket b = new Bracket(n,g.byNum(hi2)); //capping backedge
		n.blist.push(b);
		recentSize.put(b, new Integer(-1));
		// add edge to node.
		b.ancestor.capping.union(n);
	    }

	    // Compute Class(n)
	    //  if n is a representative node
	    if (n instanceof NodePrime) {
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
    
    // representation of node-split graph, with dedicated END->START edge.
    static class Graph {
	final StartNode start = new StartNode(this);
	final EndNode end = new EndNode(this);

	Node newNode(HCodeElement hce) {
	    Node n = new NodePrime(this, hce);
	    code2node.put(hce, n);
	    return n;
	}
	HCodeElement node2code(Node n) {
	    return n.source;
	}
	Node code2node(HCodeElement hce) {
	    Node n = (Node) code2node.get(hce);
	    return (n!=null)?n:newNode(hce);
	}
	final Hashtable code2node = new Hashtable();
	final Set start_code = new Set();
	final Set end_code = new Set();
	Graph(HCode hc) {
	    start_code.union(hc.getRootElement());
	    HCodeElement[] leaves = hc.getLeafElements();
	    for (int i=0; i<leaves.length; i++)
		end_code.union(leaves[i]);
	    dfs_number(); // initialize dfs_number.
	}

	// DFS ORDERING.
	private void dfs_number() {
	    dfs_number(this.start, new Set());
	}
	private void dfs_number(Node n, Set visited) {
	    Util.assert(!visited.contains(n));
	    visited.union(n); // kilroy was here.
	    n.dfs_num = dfs_order.size();
	    dfs_order.addElement(n);
	    for (Enumeration e = n.adjE(); e.hasMoreElements(); ) {
		Node m = (Node) e.nextElement();
		if (!visited.contains(m)) {
		    n.treeedges.union(m);
		    m.treeedges.union(n);
		    dfs_number(m, visited);
		}
	    }
	}
	private final Vector dfs_order = new Vector();

	// Enumerating graph elements in reverse depth-first order.
	Enumeration elements() {
	    return new Enumeration() {
		int i=dfs_order.size();
		public boolean hasMoreElements() { return (i>0); }
		public Object nextElement() {return dfs_order.elementAt(--i);}
	    };
	}
	int size() { return dfs_order.size(); }
	Node byNum(int n) { return (Node) dfs_order.elementAt(n); }
    }
    // NODE TYPES
    static abstract class Node { // abstract node types.
	Graph g;
	HCodeElement source;
	public int dfs_num;
	public Set treeedges = new Set();
	public Object cd_class;
	public BracketList blist;
	public int hi;
	public Set capping = new Set();
	Node(Graph g, HCodeElement source) {
	    this.g = g; this.source = source;
	}
	public abstract Node[] adj();
	public Enumeration adjE() { return new ArrayEnumerator(adj()); }
	public Enumeration children() {
	    final Node[] adj = adj();
	    return new Enumeration() {
		int i=0;
		private void adv() {
		    while (i<adj.length)
			if (treeedges.contains(adj[i]) && // a tree edge
			    adj[i].dfs_num >= dfs_num) // and a child.
			    break;
			else i++;
		}
		public boolean hasMoreElements() {adv();return(i<adj.length);}
		public Object nextElement() {adv(); return adj[i++]; }
	    };
	}
	public Enumeration backedges() {
	    final Node[] adj = adj();
	    return new Enumeration() {
		int i=0;
		private void adv() {
		    while(i<adj.length)
			if (!treeedges.contains(adj[i])) // not a tree edge
			    break;
			else
			    i++;
		}
		public boolean hasMoreElements(){adv();return (i<adj.length);}
		public Object  nextElement() { adv(); return adj[i++]; }
	    };
	}
		

	// ugly hack to keep track of list cell corresponding to a back
	// edge.  The algorithm guys made me do this, honest.  I had no
	// choice.
	public Hashtable be2lc = new Hashtable();
	public String toString() { return "#"+dfs_num+": "+source; }
    }
    static class StartNode extends Node {
	StartNode(Graph g) { super(g, null); }
	public Node[] adj() {
	    int i=0;
	    Node[] r = new Node[g.start_code.size() + 1];
	    for (Enumeration e=g.start_code.elements(); e.hasMoreElements();){
		HCodeElement hce = (HCodeElement)e.nextElement();
		r[i++]=((NodePrime)g.code2node(hce)).ni;
	    }
	    r[i++] = g.end;
	    return r;
	}
	public String toString() { return "#"+dfs_num+": start_node"; }
    }
    static class EndNode extends Node {
	EndNode(Graph g) { super(g, null); }
	public Node[] adj() {
	    int i=0;
	    Node[] r = new Node[g.end_code.size() + 1];
	    r[i++] = g.start;
	    for (Enumeration e = g.end_code.elements(); e.hasMoreElements();){
		HCodeElement hce = (HCodeElement)e.nextElement();
		r[i++] = ((NodePrime)g.code2node(hce)).no;
	    }
	    return r;
	}
	public String toString() { return "#"+dfs_num+": end_node"; }
    }
    static class NodePrime extends Node { // represents a'
	NodeIn  ni;
	NodeOut no;
	public Node[] adj() { return new Node[] { no, ni }; }
	NodePrime(Graph g, HCodeElement source) {
	    super(g, source);
	    this.ni = new NodeIn(g, source);
	    this.no = new NodeOut(g, source);
	}
	public String toString() { return super.toString()+"'"; }
    }
    static class NodeIn extends Node { // represents a_i
	public Node[] adj() {
	    // link to start node, if necessary.
	    boolean uplink = g.start_code.contains(source);
	    // otherwise compute edges based on underlying HCodeElement.
	    HCodeEdge[] pred = ((Edges)source).pred();
	    Node[] r = new Node[pred.length+1+(uplink?1:0)];
	    r[0] = g.code2node(source); // a' first.
	    for (int i=0; i<pred.length; i++) // then x_o where x in pred(a)
		r[i+1] = ((NodePrime)g.code2node(pred[i].from())).no;
	    if (uplink) r[r.length-1] = g.start;
	    return r;
	}
	NodeIn(Graph g, HCodeElement source) { super(g, source); }
	public String toString() { return super.toString()+"_i"; }
    }
    static class NodeOut extends Node { // represents a_o
	public Node[] adj() {
	    // link to end node, if necessary.
	    boolean downlink = g.end_code.contains(source);
	    // otherwise compute edges based on underlying HCodeElement.
	    HCodeEdge[] succ = ((Edges)source).succ();
	    Node[] r = new Node[succ.length+1+(downlink?1:0)];
	    r[0] = g.code2node(source); // a' first.
	    for (int i=0; i<succ.length; i++) // then x_i where x in succ(a)
		r[i+1] = ((NodePrime)g.code2node(succ[i].to())).ni;
	    if (downlink) r[r.length-1] = g.end;
	    return r;
	}
	NodeOut(Graph g, HCodeElement source) { super(g, source); }
	public String toString() { return super.toString()+"_o"; }
    }
    // ----------------
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
	public void delete(Bracket e) {
	    ListCell lc = (ListCell) e.descendant.be2lc.get(e.ancestor);
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
}
