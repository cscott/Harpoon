// Main.java, created Mon Nov 16 23:33:21 1998 by mfoltz
// Copyright (C) 1998 Mark A. Foltz <mfoltz@ai.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// -*-Mode: Java-*- 
// Main.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Sun Oct 25 12:37:16 1998> 
// Time-stamp: <1998-11-27 14:38:58 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;
import java.util.Observer;
import java.util.Observable;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version $Id: Main.java,v 1.7 2003-07-28 22:33:36 cananian Exp $
 */

public class Main  {

//   public static void main(String args[]) {

//     Properties p = new Properties();

//     try {
//       p.load(new FileInputStream("/home/ai/mfoltz/class/6892/src/harpoon/Analyze/Partition/properties"));

//       WeightedGraph pt[] = new WeightedGraph[2];
//       WeightedGraph g = 
// 	WeightedGraph.createRandomGraph(Integer.parseInt(p.getProperty("nodes")), 
// 					Integer.parseInt(p.getProperty("mean")), 
// 					Integer.parseInt(p.getProperty("variance")),
// 					Integer.parseInt(p.getProperty("maxweight")));
//       Partition.initialPartition(g, 2, pt);
//       System.err.println("Edge sum:  "+Partition.computeEdgeSum(pt[0], pt[1]));
//       Callback cb = new Callback(pt);
//       if (p.getProperty("viewerstyle").equals("ONE_CIRCLE")) {
// 	PartitionGraphViewer pgv = 
// 	  new PartitionGraphViewer(pt, 640, 660, cb, PartitionGraphViewer.ONE_CIRCLE);
//       } else {
// 	PartitionGraphViewer pgv = 
// 	  new PartitionGraphViewer(pt, 640, 660, cb, PartitionGraphViewer.MULTI_CIRCLE);
//       }
//     } catch (Exception e) {
//       System.err.println(e);
//     }
      
//   }

  
  public static void main(String args[]) {

    try {

      FileReader fr;
      BufferedReader br;
      int i;
      WeightedGraph g = new WeightedGraph();
      WeightedGraph pt[] = new WeightedGraph[2];
      WGNode pseudo_nodes[] = new WGNode[2];
      Properties p = new Properties();
      
      p.load(new FileInputStream("/home/mfoltz/Harpoon/Code/Analysis/Partition/properties"));

      // add pseudo-nodes that allow bindings of object creation sites to nodes
      for (i = 0; i < 2; i++) {
	pseudo_nodes[i] = new WGNode("Machine_"+i,null);
	pseudo_nodes[i]._dummy = true;
      }

      for (i = 0; i < args.length; i++) {
	fr = new FileReader(args[i]);
	br = new BufferedReader(fr);
	parseProfile(g,br,pseudo_nodes);
	br.close();
	fr.close();
      }

      g.addDummies(g.size());

      Partition.initialPartition(g, 2, pt);

      // add pseudo-nodes that allow bindings of object creation sites to nodes
      for (i = 0; i < 2; i++) 
	pt[i].addNode(pseudo_nodes[i]);
	

      System.err.println("Edge sum:  "+Partition.computeEdgeSum(pt[0], pt[1]));
      Callback cb = new Callback(pt);
      if (p.getProperty("viewerstyle").equals("ONE_CIRCLE")) {
 	PartitionGraphViewer pgv = 
 	  new PartitionGraphViewer(pt, 640, 660, cb, PartitionGraphViewer.ONE_CIRCLE);
      } else {
 	PartitionGraphViewer pgv = 
 	  new PartitionGraphViewer(pt, 640, 660, cb, PartitionGraphViewer.MULTI_CIRCLE);
      }

     } catch (Exception e) {
       System.err.println(e);
       e.printStackTrace();
     }
      
  }
    
  static class Callback implements Observer {

    private WeightedGraph[] _partition;

    public Callback(WeightedGraph[] partition) {
      _partition = partition;
    }

    public void update(Observable o, Object arg) {

      try {
	long gain;
	gain = Partition.exchange(_partition[0], _partition[1]);
	System.err.println("Edge sum:  "+Partition.computeEdgeSum(_partition[0], _partition[1]));
	WGNode node;
	Enumeration _enum_ = _partition[0].getNodes();
	while (_enum_.hasMoreElements()) {
	  node = (WGNode) _enum_.nextElement();
	  if (!node._dummy) System.err.println("MACHINE "+node._value+" 0");
	}
	_enum_ = _partition[1].getNodes();
	while (_enum_.hasMoreElements()) {
	  node = (WGNode) _enum_.nextElement();
	  if (!node._dummy) System.err.println("MACHINE "+node._value+" 1");
	}
	if (gain == 0) {
	  System.err.println("Phase-1 optimal partition found.");
	  System.exit(0);
	}
      } catch (Exception e) {
	System.err.println(e);
	e.printStackTrace();
      }
      
    }

  }

  public static WeightedGraph parseProfile(WeightedGraph g, BufferedReader in, WGNode[] pseudo_nodes) {

    Hashtable creation_sites = new Hashtable();
    long creator, created;
    int site_id, machine_no;
    StringTokenizer st;
    NEW creation_site;
    WGNode node, source, target;

    try {

      String line = in.readLine(), token, creator_method, creator_class, created_class, node_name;

      while (line != null) {
	st = new StringTokenizer(line," ");
	if (st.countTokens() == 0) break;
	token = st.nextToken();
	if (token.startsWith("NEW")) {
	  creator = Long.parseLong(st.nextToken());
	  creator_method = st.nextToken();
	  creator_class = st.nextToken();
	  created = Long.parseLong(st.nextToken());
	  created_class = st.nextToken();
	  site_id = Integer.parseInt(st.nextToken());
	  node_name = creator_class+":"+creator_method+":"+site_id;
	  node = g.getNode(node_name);
	  if (node == null) {
	    creation_site = new NEW(creator, creator_method, creator_class, created, created_class, site_id);
	    node = new WGNode(node_name,creation_site);
	    g.addNode(node);
	  }
	  creation_sites.put(new Long(created), node);
	} else if (token.startsWith("CALL")) {
	  source = (WGNode) creation_sites.get(new Long(Long.parseLong(st.nextToken())));
	  st.nextToken();
	  target = (WGNode) creation_sites.get(new Long(Long.parseLong(st.nextToken())));
	  if (source == null || target == null || source == target) {
	    System.err.println("IGNORING: "+line);
	  } else {
	    WeightedGraph.addToEdge(source,target,1);
	  }
	} else if (token.startsWith("MAYCALL")) {
	  // do nothing
	} else if (token.startsWith("BIND")) {
	  node_name = st.nextToken();
	  machine_no = Integer.parseInt(st.nextToken());
	  if (machine_no < 0 || machine_no >= pseudo_nodes.length)
	    source = null;
	  else source = pseudo_nodes[machine_no];
	  target = g.getNode(node_name);
	  if (source == null || target == null || source == target) {
	    System.err.println("IGNORING: "+line);
	  } else {
	    WeightedGraph.setEdge(source,target,Long.MAX_VALUE);
	    target._binding = machine_no;
	  }
	} else if (token.startsWith("MACHINE")) {
	  node_name = st.nextToken();
	  machine_no = Integer.parseInt(st.nextToken());
	  target = g.getNode(node_name);
	  if (target == null) {
	    System.err.println("IGNORING: "+line);
	  } else {
	    target._binding = machine_no;
	  }
	}
	line = in.readLine();
      }

    } catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }

    return g;

  }

  static private class NEW {

    public long _creator;
    public String _creator_method;
    public String _creator_class;
    public long _created;
    public String _created_class;
    public int _site_id;

    public NEW(long creator, String creator_method, String creator_class,
	       long created, String created_class, int site_id) {
      _creator = creator;
      _creator_method = creator_method;
      _creator_class = creator_class;
      _created = created;
      _created_class = created_class;
      _site_id = site_id;
    }
    
    public String toString() {
      return _creator_class+":"+_creator_method+":"+_site_id;
    }

  }
    
}


