// CycleEq.java, created Wed Oct 14 08:15:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Util.Collections.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * <code>CycleEq</code> computes cycle equivalence classes for edges in
 * a control flow graph, in O(E) time.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CycleEq.java,v 1.8 2002-08-30 22:37:12 cananian Exp $
 */

public class CycleEq  {
    /* List of <code>HCodeEdge</code>s in DFS order */
    private List elements = new ArrayList();
    /* Mapping of cycle equivalency class object to <code>List</code>
     * of <code>HCodeEdge</code>s in that equivalency class. */
    private Map equiv = new HashMap();

    public CycleEq(HCode hc) {
	Graph g = new Graph(hc); // compute cycle equivalency
	// DFS search to order elements in cycle-equivalency classes.
	dfs(g, hc.getRootElement(), new HashSet());
	// make equiv and elements unmodifiable.
	equiv = Collections.unmodifiableMap(equiv);
	elements = Collections.unmodifiableList(elements);
	// throw away all temp info now.
    }
    private void dfs(Graph g, HCodeElement hce, Set mark) {
	Stack s = new Stack();
    newnode: // this is an obvious hack to allow a 'goto' statement
	do { // despite java's "prohibition".
	    mark.add(hce);
	    s.push(((CFGraphable)hce).succC().iterator());
	    
	    while (!s.isEmpty()) {
		for (Iterator i = (Iterator) s.pop(); i.hasNext(); ) {
		    HCodeEdge e = (HCodeEdge)i.next();
		    Object cq = g.edge(e).CQclass;
		    List l = (List) equiv.get(cq);
		    if (l==null) { l = new LinkedList(); equiv.put(cq, l); }
		    l.add(e); // append edge to end of list.
		    elements.add(e);
		    // recurse
		    if (!mark.contains(e.to())) {
			s.push(i);
			hce = e.to();
			continue newnode; // recurse to top of procedure.
		    }
		}
	    }
	    break;
	} while(true); // not really a loop.  Just a block.
    }
    /** Return <code>Collection</code> of cycle-equivalency
     *	<code>List</code>s. */
    public Collection cdClasses() { return equiv.values(); }
    /** Return <code>List</code> of edges in DFS order. */
    public List elements() { return elements; }

    /* ----------------------------------------------------- */
    
    /** Graph stored edge/node information for cycle-equivalency computation.
     *  Also adds synthetic END->START edge to make CFG an SCC. */
    static private class Graph {
	Map hce2node = new HashMap(); // map HCodeElements to nodes.
	Map hce2edge = new HashMap(); // map HCodeEdges to edges
	Map start_edges = new HashMap(); // from synthetic START to root.
	Map end_edges   = new HashMap(); // from synthetic END to leaves.
	Node START = new StartNode(); // synthetic START
	Node END = new EndNode();     // synthetic END
	List all_nodes = new ArrayList(); // all nodes, in dfs preorder.
	Graph(HCode c) {
	    /* initialize graph */
	    HCodeElement root = c.getRootElement();
	    start_edges.put(root, new FakeEdge(START, node(root)));
	    HCodeElement[] leaves = c.getLeafElements();
	    for (int i=0; i<leaves.length; i++)
		end_edges.put(leaves[i], new FakeEdge(node(leaves[i]), END));
	    Edge e = new FakeEdge(START, END); // synthetic END->START edge
	    start_edges.put(null, e);
	    end_edges.put(null, e);
	    /* construct depth first spanning tree */
	    dfs(START, new HashSet());
	    /* compute cycle equivalence */
	    for (int i=all_nodes.size()-1; i>=0; i--) // dfs postorder
		cycle((Node)all_nodes.get(i));
	}
	/** Map HCodeElement to Node, creating new RealNode if necessary. */
	Node node(HCodeElement hce) {
	    assert hce!=null;
	    Node n = (Node) hce2node.get(hce);
	    return (n!=null)?n:new RealNode(hce);
	}
	/** Map HCodeEdge to Edge, creating new RealEdge if necessary. */
	Edge edge(HCodeEdge hce) {
	    assert hce!=null;
	    Edge e = (Edge) hce2edge.get(hce);
	    return (e!=null)?e:new RealEdge(hce);
	}
	/** Construct depth-first spanning tree. */
	void dfs(Node n, Set mark) {
	    assert !mark.contains(n);
	    n.dfsnum = mark.size();
	    mark.add(n);
	    all_nodes.add(n);
	    for (Iterator i=n.adj(); i.hasNext(); ) {
		Edge e = (Edge) i.next();
		Node m = e.otherEnd(n); /* edge from n to m */
		if (!mark.contains(m)) { /* a tree edge (child) */
		    n.children = new EdgeSList(e, n.children);
		    m.parent = e;
		    /* recurse */
		    dfs(m, mark);
		} else if (!e.equals(n.parent)) { /* a backedge */
		    n.backedges = new EdgeSList(e, n.backedges);
		}
	    }
	}
	/** Compute cycle-equivalency */
	void cycle(Node n) {
	    /** Compute hi(n) */
	    Node hi0 = null;
	    for (EdgeSList el=n.backedges; el!=null; el=el.next)
		if (hi0==null || el.e.otherEnd(n).dfsnum < hi0.dfsnum)
		    hi0 = el.e.otherEnd(n);
	    Node hi1 = null, hi2 = null;
	    for (EdgeSList el=n.children; el!=null; el=el.next)
		if (hi1==null || el.e.otherEnd(n).hi.dfsnum < hi1.dfsnum) {
		    if (hi2==null || hi1.dfsnum < hi2.dfsnum) hi2 = hi1;  
		    hi1 = el.e.otherEnd(n).hi;
		} else if (hi2==null ||
			   el.e.otherEnd(n).hi.dfsnum < hi2.dfsnum) {
		    hi2 = el.e.otherEnd(n).hi; // second-highest.
		}

	    n.hi = (hi0==null) ? hi1 : (hi1==null) ? hi0 :
		(hi0.dfsnum < hi1.dfsnum) ? hi0 : hi1;

	    /* compute bracketlist */
	    n.blist = new BracketList();
	    for (EdgeSList el=n.children; el!=null; el=el.next)
		n.blist.append(el.e.otherEnd(n).blist);
	    for (EdgeSList el=n.capping; el!=null; el=el.next)
		n.blist.delete(el.e);
	    for (EdgeSList el=n.backedges; el!=null; el=el.next)
		if (el.e.otherEnd(n).dfsnum > n.dfsnum) { /* descendant of n */
		    n.blist.delete(el.e);
		    if (el.e.CQclass==null)
			el.e.CQclass = new Object(); /* new CQ class */
		}
	    for (EdgeSList el=n.backedges; el!=null; el=el.next)
		if (el.e.otherEnd(n).dfsnum <= n.dfsnum) /*ancestor or itself*/
		    n.blist.push(el.e);
	    if (hi2!=null &&
		(hi0==null || hi0.dfsnum > hi2.dfsnum)) {
		/* create capping backedge */
		Edge d = new FakeEdge(n, hi2);
		hi2.capping = new EdgeSList(d, hi2.capping);
		n.blist.push(d);
	    }

	    /* determine class for edge from parent(n) to n */
	    Edge e = n.parent;
	    if (e!=null) {
		Edge b = n.blist.top();
		if (b.recentSize != n.blist.size()) {
		    b.recentSize =  n.blist.size();
		    b.recentClass=  new Object(); /* new CQ class */
		}
		e.CQclass = b.recentClass;

		/* handle one tree one backedge case */
		if (b.recentSize==1)
		    b.CQclass = e.CQclass;
	    }
	}

	/** Graph node superclass */
	abstract class Node {
	    int dfsnum = -1;
	    BracketList blist;
	    Node hi; // highest reachable from backedge originating below this
	    //
	    Edge      parent = null;
	    EdgeSList children = null;
	    EdgeSList backedges= null;
	    EdgeSList capping = null;

	    abstract Iterator adj(); /* return all edges */
	    public abstract boolean equals(Object o);
	    public abstract int hashCode();
	    public String toString() { return "["+dfsnum+"]"; }
	}
	/** Synthetic START node */
	class StartNode extends Node {
	    Iterator adj() { return start_edges.values().iterator(); }
	    public boolean equals(Object o) { return o instanceof StartNode; }
	    public int hashCode() { return 458543; }
	    public String toString() { return "START"; }
	}
	/** Synthetic END node */
	class EndNode extends Node {
	    Iterator adj() { return end_edges.values().iterator(); }
	    public boolean equals(Object o) { return o instanceof EndNode; }
	    public int hashCode() { return 234913; }
	    public String toString() { return "END"; }
	}
	/** 'Real' node corresponding to an HCodeElement */
	class RealNode extends Node {
	    final HCodeElement hce;
	    //
	    RealNode(HCodeElement hce) {
		assert hce!=null;
		assert !hce2node.containsKey(hce);
		this.hce = hce; hce2node.put(hce, this);
	    }
	    Iterator adj() {
		return new UnmodifiableIterator() {
		    final HCodeEdge[] pred=((CFGraphable)hce).pred();
		    final HCodeEdge[] succ=((CFGraphable)hce).succ();
		    Edge se = (Edge) start_edges.get(hce);
		    Edge ee = (Edge) end_edges.get(hce);
		    int i=0, j=0;
		    public boolean hasNext() {
			return (i<pred.length) || (j<succ.length) ||
			    (se != null) || (ee != null);
		    }
		    public Object next() {
			if (i<pred.length)
			    return edge(pred[i++]);
			if (j<succ.length)
			    return edge(succ[j++]);
			if (se!=null) { Edge e=se; se=null; return e; }
			if (ee!=null) { Edge e=ee; ee=null; return e; }
			throw new NoSuchElementException();
		    }
		};
	    }
	    public int hashCode() { return hce.hashCode(); }
	    public boolean equals(Object o) {
		RealNode rn;
		if (this==o) return true;
		if (null==o) return false;
		try { rn = (RealNode) o; }
		catch (ClassCastException e) { return false; }
		return rn.hce.equals(hce);
	    }
	    public String toString()
		{ return super.toString()+" "+hce.toString(); }
	}

	/** Graph edge superclass */
	abstract class Edge {
	    Object CQclass = null;
	    int recentSize;
	    Object recentClass;
	    EdgeDList container;
	    //
	    public abstract Node otherEnd(Node n);
	    public int hashCode() { throw new Error("Unimplemented."); }
	    public abstract String toString();
	}
	/** 'Real' edge corresponding to HCodeEdge */
	class RealEdge extends Edge {
	    final HCodeEdge hce;
	    RealEdge(HCodeEdge hce) {
		assert hce!=null;
		assert !hce2edge.containsKey(hce);
		this.hce = hce; hce2edge.put(hce, this);
	    }
	    public Node otherEnd(Node n) {
		RealNode rn = (RealNode) n; /* RealEdge connects RealNodes */
		return node(hce.from().equals(rn.hce)?hce.to():hce.from());
	    }
	    public String toString() { return hce.toString(); }
	}
	/** Synthetic edge; either capping or connecting Synthetic 
	 *  START or END nodes to each other or to real root/leaves. */
	class FakeEdge extends Edge {
	    final Node from, to;
	    FakeEdge(Node from, Node to) {
		assert from!=null && to !=null;
		this.from = from; this.to = to;
	    }
	    public Node otherEnd(Node n) {
		return from.equals(n)?to:from;
	    }
	    public String toString() {
		return "Fake: "+from+" -> "+to;
	    }
	}

	/*--------------- Utility classes ----------------*/
	/** Singly-linked lists of edges. */
	static class EdgeSList {
	    final Edge e;
	    final EdgeSList next;
	    EdgeSList(Edge e, EdgeSList next) {
		this.e = e; this.next = next;
	    }
	}
	/** Doubly-linked lists of edges. */
	static class EdgeDList {
	    final Edge e;
	    EdgeDList prev, next;
	    EdgeDList(EdgeDList prev, Edge e, EdgeDList next) {
		this.prev = prev; this.e = e; this.next = next;
	    }
	}
	/** BracketList structure, based on doubly-linked edge list. */
	static class BracketList {
	    EdgeDList first = null;
	    EdgeDList last = null;
	    int size = 0;
	    
	    public BracketList() { }
	    public int size() { return size; }
	    public void push(Edge e) {
		first = new EdgeDList(null, e, first);
		if (first.next!=null)
		    first.next.prev = first;
		else
		    last = first;
		e.container = first;
		size++;
	    }
	    public Edge top() { return first.e; }
	    public void delete(Edge e) {
		delete(e.container);
	    }
	    private void delete(EdgeDList el) {
		if (first==el) first=el.next;
		else el.prev.next = el.next;
		if (last==el) last = el.prev;
		else el.next.prev = el.prev;
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
		bl.first=bl.last=null; bl.size=0;
	    }
	    public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(size);
		sb.append(": { ");
		for (EdgeDList el=first; el!=null; el=el.next) {
		    sb.append(el.e);
		    if (el.next!=null)
			sb.append(", ");
		}
		sb.append(" }");
		return sb.toString();
	    }
	} /* end class BracketList */
    } /* end class Graph */
}
