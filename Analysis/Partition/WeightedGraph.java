// WeightedGraph.java, created Mon Nov 16 23:33:21 1998 by mfoltz
// Copyright (C) 1998 Mark A. Foltz <mfoltz@ai.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// -*-Mode: Java-*- 
// WeightedGraph.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Thu Oct 22 23:11:21 1998> 
// Time-stamp: <1998-11-21 18:59:53 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version $Id: WeightedGraph.java,v 1.4 2002-02-25 20:58:28 cananian Exp $
 */

public class WeightedGraph {

  Hashtable _nodes = new Hashtable();
  //  public static final long _pinfinity = Long.MAX_VALUE;
  //  public static final long _ninfinity = -Long.MAX_VALUE;
  int _dummyindex = 0;
  
  public WeightedGraph() { }

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

  public WGNode getNode(String name) {
    return (WGNode) _nodes.get(name);
  }

  public Enumeration getNodes() {
    return _nodes.elements();
  }

  public WGNode addNode(WGNode node) {
    return (WGNode) _nodes.put(node._name, node);
  }

  public WGNode clearNode(WGNode node) {
    return (WGNode) _nodes.remove(node._name);
  }

  public WGNode removeNode(WGNode node) {
    node.removeEdges();
    return (WGNode) _nodes.remove(node._name);
  }

  public boolean isEmpty() {
    return _nodes.isEmpty();
  }

  public void clear() {
    _nodes.clear();
  }

  public int size() {
    return _nodes.size();
  }

  public boolean contains(WGNode node) {
    return _nodes.containsKey(node._name);
  }

  public String toString() {
    WGNode n;
    StringBuffer sb = new StringBuffer();
    for (Enumeration e = getNodes(); e.hasMoreElements();) {
      n = (WGNode) e.nextElement();
      sb.append(n.toString());
    }
    return sb.toString();
  }

  public void addDummies(int n) {
    WGNode dummy;
    for (int i = 0; i < n; i++) {
      dummy = new WGNode("__dummy"+_dummyindex,null);
      dummy._dummy = true;
      _nodes.put(dummy._name, dummy);
      _dummyindex++;
    }
  }

  public void removeDummies() {
    WGNode node;
    for (Enumeration e = _nodes.elements(); e.hasMoreElements();) {
      node = (WGNode) e.nextElement();
      if (node._dummy) _nodes.remove(node._name);
    }
  }
				  
  static public void addToEdge(WGNode from, WGNode to, long weight) {
    from.addToEdge(to, weight);
    to.addToEdge(from, weight);
  }

  static public void setEdge(WGNode from, WGNode to, long weight) {
    from.setEdge(to, weight);
    to.setEdge(from, weight);
  }
    
  static public void exchange(WeightedGraph g1, WGNode n1, WeightedGraph g2, WGNode n2) {
    g1.addNode(g2.getNode(n2._name));
    g2.addNode(g1.getNode(n1._name));
    g1.clearNode(n1);
    g2.clearNode(n2);
  }

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

    
