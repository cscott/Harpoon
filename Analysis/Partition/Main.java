// -*-Mode: Java-*- 
// Main.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Sun Oct 25 12:37:16 1998> 
// Time-stamp: <1998-11-16 23:49:39 mfoltz> 
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
 * @version 
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

      FileReader fr = new FileReader(args[0]);
      WeightedGraph g = parseProfile(new BufferedReader(fr));
      WeightedGraph pt[] = new WeightedGraph[2];
      Properties p = new Properties();
      
      p.load(new FileInputStream("/home/mfoltz/Harpoon/Code/Analysis/Partition/properties"));
 
      g.addDummies(g.size());
      Partition.initialPartition(g, 2, pt);
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
     }
      
  }
    
  static class Callback implements Observer {

    private WeightedGraph[] _partition;

    public Callback(WeightedGraph[] partition) {
      _partition = partition;
    }

    public void update(Observable o, Object arg) {
      Partition.exchange(_partition[0], _partition[1]);
      System.err.println("Edge sum:  "+Partition.computeEdgeSum(_partition[0], _partition[1]));
      WGNode node;
      Enumeration enum = _partition[0].getNodes();
      while (enum.hasMoreElements()) {
	node = (WGNode) enum.nextElement();
	if (!node._dummy) System.err.println("MACHINE 1 "+node._value);
      }
      enum = _partition[1].getNodes();
      while (enum.hasMoreElements()) {
	node = (WGNode) enum.nextElement();
	if (!node._dummy) System.err.println("MACHINE 2 "+node._value);
      }

    }
  }

  public static WeightedGraph parseProfile(BufferedReader in) {

    Hashtable creation_sites = new Hashtable();
    long creator, created;
    int site_id;
    StringTokenizer st;
    WeightedGraph g = new WeightedGraph();
    NEW creation_site;
    WGNode node, source, target;

    try {

      String line = in.readLine(), token, creator_method, creator_class, created_class;

      while (line != null) {
	st = new StringTokenizer(line," ");
	token = st.nextToken();
	if (token.startsWith("NEW")) {
	  creator = Long.parseLong(st.nextToken());
	  creator_method = st.nextToken();
	  creator_class = st.nextToken();
	  created = Long.parseLong(st.nextToken());
	  created_class = st.nextToken();
	  site_id = Integer.parseInt(st.nextToken());
	  creation_site = new NEW(creator, creator_method, creator_class, created, created_class, site_id);
	  node = new WGNode(creator_class+":"+creator_method+":"+site_id,creation_site);
	  creation_sites.put(new Long(created), node);
	  g.addNode(node);
	} else if (token.startsWith("CALL")) {
	  source = (WGNode) creation_sites.get(new Long(Long.parseLong(st.nextToken())));
	  st.nextToken();
	  target = (WGNode) creation_sites.get(new Long(Long.parseLong(st.nextToken())));
	  if (source == null || target == null || source == target) {
	    System.err.println("IGNORING: "+line);
	  } else {
	    WeightedGraph.addToEdge(source,target,1);
	  }
	}
	line = in.readLine();
      }

    } catch (Exception e) {
      System.err.println(e);
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


