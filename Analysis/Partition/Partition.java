// Partition.java, created Mon Nov 16 23:33:21 1998 by mfoltz
// Copyright (C) 1998 Mark A. Foltz <mfoltz@ai.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// -*-Mode: Java-*- 
// Partition.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Fri Oct 23 00:24:17 1998> 
// Time-stamp: <1998-11-26 11:47:57 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.util.Enumeration;
import java.util.Vector;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version $Id: Partition.java,v 1.5 2002-02-25 20:58:28 cananian Exp $
 */

public class Partition {

  public static void initialPartition(WeightedGraph g, int k, WeightedGraph p[]) {

    Enumeration nodes;
    WGNode node;

    p[0] = new WeightedGraph();
    p[1] = new WeightedGraph();
    
    nodes = g.getNodes();
    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      if (node._binding > -1) p[node._binding].addNode(node);
    }

    nodes = g.getNodes();
    while (p[0].size() < g.size()/2) {
      node = (WGNode) nodes.nextElement();
      if (node._binding < 0) p[0].addNode(node);
    }

    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      if (node._binding < 0) p[1].addNode(node);
    }

    return;
    
  }

  public static long exchange(WeightedGraph g1, WeightedGraph g2) throws Exception {

    // we assume g1 and g2 are the same size

    int size = g1.size();
    if (size != g2.size()) throw new Exception("exchange bailed on graphs of unequal size!!!");

    // set up A and B
    
    WGNode node;
    int pair[] = new int[2], i, j;
    Enumeration nodes;

    // clone the WeightedGraphs into A and B.

    WeightedGraph A = new WeightedGraph(g1);
    WeightedGraph B = new WeightedGraph(g2);

    // fixPartition will fix the pointers of external edges.

    fixPartition(A,B);
    fixPartition(B,A);

    // System.err.print(A);
    // System.err.print(B);

    //    WeightedGraph A = g1; 
    //    WeightedGraph B = g2; 

    // compute initial D values

    recomputeD(A.getNodes(),A,B);
    recomputeD(B.getNodes(),B,A);

    WGNode X[] = new WGNode[size];
    WGNode Y[] = new WGNode[size];
    long gains[] = new long[size];

    for (i = 0; i < size; i++) {

      // System.err.print("A:\n"+A);
      // System.err.print("B:\n"+B);

      // create sorted lists of the Ds

      WGNode Ap[] = new WGNode[A.size()];
      WGNode Bp[] = new WGNode[B.size()];
      
      nodes = A.getNodes();
      for (j = 0; j < A.size(); j++) {
	node = (WGNode) nodes.nextElement();
	Ap[j] = node;
      }

      nodes = B.getNodes();
      for (j = 0; j < B.size(); j++) {
	node = (WGNode) nodes.nextElement();
	Bp[j] = node;
      }

      // sort the lists of nodes by D and find a pair to exchange

      quicksort(Ap,0,Ap.length-1);
      quicksort(Bp,0,Bp.length-1);

      System.err.print("Ap: ");
      for (j = 0; j < Ap.length; j++)
	System.err.print("Ap["+Ap[j]._name+"]="+Ap[j]._d+" ");
      System.err.print("\n");

      System.err.print("Bp: ");
      for (j = 0; j < Bp.length; j++)
	System.err.print("Bp["+Bp[j]._name+"]="+Bp[j]._d+" ");
      System.err.print("\n");

//       System.err.print("Bp[] = ");
//       for (j = 0; j < Bp.length; j++)
// 	System.err.print(Bp[j]._d+" ");
//       System.err.print("\n");

      gains[i] = findBestPair(Ap, Bp, pair);

      System.err.println("Best pair: Ap["+Ap[pair[0]]._name+"] = "+Ap[pair[0]]._d+", Bp["+Bp[pair[1]]._name+"] = "+Bp[pair[1]]._d+"\n");

      // now put the exchange pair into X, Y and remove from A, B

      X[i] = Ap[pair[0]];
      Y[i] = Bp[pair[1]];

      A.removeNode(X[i]);
      B.removeNode(Y[i]);

      // recompute Ds for nodes adjacent to the pair

      recomputeD(X[i].getAdjacent(),A,B);
      recomputeD(Y[i].getAdjacent(),B,A);
    }

    // find k to maximize total gain 

    int k = -1;
    long gain = 0, max_gain = 0;

    for (i = 0; i < size; i++) {
      gain = WeightedGraph.weightPlus(gain,gains[i]);
      if (gain > max_gain) {
	max_gain = gain;
	k = i;
      }
    }

    // swap it
    if (max_gain > 0) {

      for (i = 0; i <= k; i++) {
	System.err.println("exchange "+X[i]._name+" and "+Y[i]._name);
	WeightedGraph.exchange(g1, X[i], g2, Y[i]);
      }

    }

    return max_gain;
    
  }

  // traverse the lists and pick the best pair a, b to exchange.
  private static long findBestPair(WGNode Ap[], WGNode Bp[], int pair[]) {

      long gain, max_gain = 0;
      int i = 0, j = 0, k;
      pair[0] = 0;
      pair[1] = 0;

      while (i < Ap.length-1 || j < Bp.length-1) {

	for (k = 0; k <= j; k++) {
	  //	  if (Ap[i]._d + Bp[k]._d < max_gain) return;
	  gain = WeightedGraph.weightPlus(WeightedGraph.weightPlus(Ap[i]._d,Bp[k]._d),
					  WeightedGraph.weightPlus(-Ap[i].getWeight(Bp[k]),
								   -Ap[i].getWeight(Bp[k])));
	  System.err.print("gain("+Ap[i]._name+","+Bp[k]._name+")="+gain+"; ");
	  if (gain > max_gain) {
	    max_gain = gain;
	    pair[0] = i;
	    pair[1] = k;
	  }
	}

	for (k = 0; k < i; k++) {
	  if (WeightedGraph.weightPlus(Ap[k]._d,Bp[j]._d) < max_gain) return max_gain;
	  gain = WeightedGraph.weightPlus(WeightedGraph.weightPlus(Ap[k]._d,Bp[j]._d),
					  WeightedGraph.weightPlus(-Ap[k].getWeight(Bp[j]),
								   -Ap[k].getWeight(Bp[j])));
	  System.err.print("gain("+Ap[k]._name+","+Bp[j]._name+")="+gain+"; ");
	  if (gain > max_gain) {
	    max_gain = gain;
	    pair[0] = k;
	    pair[1] = j;
	  }
	}

	if (i < Ap.length-1) i++;
	if (j < Bp.length-1) j++;
							 
      }

      System.err.print("\n");

      return max_gain;
  }

      
  // recompute D for nodes in the enumeration, some subset of g1
  private static void recomputeD(Enumeration nodes, WeightedGraph g1, WeightedGraph g2) {
    
    Enumeration adjacents, weights;
    WGNode node, adjacent;
    Long weight;

    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      node._d = 0;
      adjacents = node.getAdjacent();
      weights = node.getWeights();
      while (adjacents.hasMoreElements()) {
	adjacent = (WGNode) adjacents.nextElement();
	weight = (Long) weights.nextElement();
	if (g1.contains(adjacent)) 
	  node._d = WeightedGraph.weightPlus(node._d,-weight.longValue());
	//	  node._d -= weight.longValue();
	else if (g2.contains(adjacent))
	  node._d = WeightedGraph.weightPlus(node._d,weight.longValue());
	//	  node._d += weight.longValue();
      }
    }

  }

  // sort nodes in descending order by D.  
  private static void quicksort(WGNode X[], int i, int j) {

    // System.err.println("quicksort(X[],"+i+","+j+")\n");

    if (i >= j) return;
    else {
      long pivot = X[i]._d;
      int k = i;
      int m = j;
      WGNode temp;
      while (k < m) {
	while (X[k]._d > pivot && (k < m)) k++;
	while (X[m]._d <= pivot && (k < m)) m--;
	if (k != m) {
	  temp = X[m];
	  X[m] = X[k];
	  X[k] = temp;
	}
      }
      if (j == i + 1) return;
      else {
	quicksort(X,i,k);
	quicksort(X,k+1,j);
      }
    }
    return;
  }

  private static void fixPartition(WeightedGraph A, WeightedGraph B) {

    // fix up edge pointers in A to point to nodes in B.

    Enumeration nodes;
    WGNode node, to, oldto;
    int i;

    nodes = A.getNodes();
    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      for (i = 0; i < node._edges.size(); i++) {
	oldto = (WGNode) node._edges.elementAt(i);
	to = B.getNode(oldto._name);
	if (to != null) node._edges.setElementAt(to,i);
      }
    }
  }

  public static long computeEdgeSum(WeightedGraph g1, WeightedGraph g2) {
    
    Enumeration nodes;
    WGNode node, to;
    int i;
    long sum = 0;
    
    nodes = g1.getNodes();
    while (nodes.hasMoreElements()) {
      node = (WGNode) nodes.nextElement();
      for (i = 0; i < node._edges.size(); i++) {
	to = (WGNode) node._edges.elementAt(i);
	if (g2.contains(to)) {
	  sum = WeightedGraph.weightPlus(sum,((Long) node._weights.elementAt(i)).longValue());
	}
      }
    }

    return sum;

  }

    

}

