// -*-Mode: Java-*- 
// WeightedGraph.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Thu Oct 22 23:11:21 1998> 
// Time-stamp: <1998-12-03 00:07:39 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.util.Enumeration;
import java.util.Hashtable;

/** A class representing an undirected graph with integer
 * edge weights between nodes.
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version 
 */

public class WeightedGraph {

  /** The nodes of the graph. */
  Hashtable _nodes = new Hashtable();
  //  public static final long _pinfinity = Long.MAX_VALUE;
  //  public static final long _ninfinity = -Long.MAX_VALUE;
  /** An index used to generate names of dummy nodes. */
  int _dummyindex = 0;
  
  /** Create an empty <code>WeightedGraph</code>. */
  public WeightedGraph() { }

  /** Create a clone of the <code>WeightedGraph g</code>. */
  public WeightedGraph(WeightedGraph g) {
    Enumeration nodes = g.getNodes();
    WGNode node, oldto, to;
    int i;

    while (nodes.hasMoreElements()) {
      addNode(new WGNode((WGNode) nodes.nextElement()));
    }

    nodes = getNodes();
    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      for (i = 0; i < node.degree(); i++) {
	oldto = (WGNode) node._edges.elementAt(i);
	to = getNode(oldto._name);
	if (to != null) node._edges.setElementAt(to, i);
      }
    }

  }
  
  /** Get the node named <code>name</code>. */
  public WGNode getNode(String name) {
    return (WGNode) _nodes.get(name);
  }

  /** Get all of the nodes. */
  public Enumeration getNodes() {
    return _nodes.elements();
  }

  /** Add a node named <code>name</code>. */
  public WGNode addNode(WGNode node) {
    return (WGNode) _nodes.put(node._name, node);
  }
  
  /** Remove a node from this graph, but leave its edges. */
  public WGNode clearNode(WGNode node) {
    return (WGNode) _nodes.remove(node._name);
  }

  /** Remove a node from this graph, and remove its edges to other nodes. */
  public WGNode removeNode(WGNode node) {
    node.removeEdges();
    return (WGNode) _nodes.remove(node._name);
  }
  
  /** Return <code>true</code> if this graph is empty. */
  public boolean isEmpty() {
    return _nodes.isEmpty();
  }

  /** Clear all of the nodes in the graph. */
  public void clear() {
    _nodes.clear();
  }
  
  /** Return the number of nodes in the graph. */
  public int size() {
    return _nodes.size();
  }

  /** Return <code>true</code> if the graph contains <code>node</code>. */
  public boolean contains(WGNode node) {
    return _nodes.containsKey(node._name);
  }

  /** Return a string representation of this graph. */
  public String toString() {
    WGNode n;
    StringBuffer sb = new StringBuffer();
    for (Enumeration e = getNodes(); e.hasMoreElements();) {
      n = (WGNode) e.nextElement();
      sb.append(n.toString());
    }
    return sb.toString();
  }

  /** Add <code>n</code> dummy nodes to the graph.  Dummies have no edges. */
  public void addDummies(int n) {
    WGNode dummy;
    for (int i = 0; i < n; i++) {
      dummy = new WGNode("__dummy"+_dummyindex,null);
      dummy._dummy = true;
      _nodes.put(dummy._name, dummy);
      _dummyindex++;
    }
  }
  
  /** Remove all of the dummies in this graph. */
  public void removeDummies() {
    WGNode node;
    for (Enumeration e = _nodes.elements(); e.hasMoreElements();) {
      node = (WGNode) e.nextElement();
      if (node._dummy) _nodes.remove(node._name);
    }
  }
				  
  /** Add <code>weight</code> on the edge between <code>from</code> and <code>to</code>. */
  static public void addToEdge(WGNode from, WGNode to, long weight) {
    from.addToEdge(to, weight);
    to.addToEdge(from, weight);
  }

  /** Set the edge weight to <code>weight</code> between <code>from</code> and <code>to</code>. */
  static public void setEdge(WGNode from, WGNode to, long weight) {
    from.setEdge(to, weight);
    to.setEdge(from, weight);
  }

  /** Exchange the node <code>n1</code> in <code>g1</code> with <code>n2</code> in <code>g2</code>. */
  static public void exchange(WeightedGraph g1, WGNode n1, WeightedGraph g2, WGNode n2) {
    g1.addNode(g2.getNode(n2._name));
    g2.addNode(g1.getNode(n1._name));
    g1.clearNode(n1);
    g2.clearNode(n2);
  }

  /** Create a graph of <code>n</code> nodes, with edge weights uniformly distributed
   * from <code>u-v</code> to <code>u+v</code> and connectivity uniformly distributed
   * from 0 to <code>w</code>.
   */
  static WeightedGraph createRandomGraph(int n, int u, int v, long w) {

    WeightedGraph g = new WeightedGraph();
    WGNode[] nodes = new WGNode[n];
    int num_edges, i, j, k;

    for (i = 0; i < n; i++) {
      nodes[i] = new WGNode("Node"+i,null);
      g.addNode(nodes[i]);
    }

    for (i = 0; i < n; i++) {
      num_edges = u - v + (int) (Math.random()*2*v);
      for (j = 0; j < num_edges; j++) {
	k = (int) (Math.random()*n);
	if (k != i) WeightedGraph.addToEdge(nodes[i], nodes[k], (long) (Math.random()*w));
      }
    }

    return g;

  }
  
  // a Long.MAX_VALUE weight is +infinity. a -Long.MAX_VALUE weight is -infinity.
  // must maintain this distinction through ops.

  /** Add two edge weights, respecting the special values <code>Long.MAX_VALUE</code>
   * as +infinity and <code>-Long.MAX_VALUE</code> as -infinity. */
  static long weightPlus(long w1, long w2) {
    if ((w1 == Long.MAX_VALUE && w2 == -Long.MAX_VALUE)
      || (w1 == -Long.MAX_VALUE && w2 == Long.MAX_VALUE)) return 0;
    if (w1 == Long.MAX_VALUE || w2 == Long.MAX_VALUE) return Long.MAX_VALUE;
    else if (w1 == -Long.MAX_VALUE || w2 == -Long.MAX_VALUE) return -Long.MAX_VALUE;
    else return w1+w2;
  }

//   static boolean weightLEQ(long w1, long w2) {
//     if (w2 == -1) return true;
//     else if (w1 == -1 && w2 != -1) return false;
//     else return (w1 <= w2);
//   }

//   static long weightNeg(long w) {
//     if (w == -1) return -2;
//     else if (w == -2) return -1;
//     else return -w;
//   }
			    
}

    
