// PartitionGraphViewer.java, created Mon Nov 16 23:33:21 1998 by mfoltz
// Copyright (C) 1998 Mark A. Foltz <mfoltz@ai.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// -*-Mode: Java-*- 
// PartitionGraphViewer.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Sun Oct 25 16:14:14 1998> 
// Time-stamp: <1998-11-26 13:51:07 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Observer;
import java.util.Observable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version $Id: PartitionGraphViewer.java,v 1.5 2002-02-25 20:58:28 cananian Exp $
 */

public class PartitionGraphViewer extends Observable implements ActionListener {

  static public Color _nodecolors[] = { new Color(0, 255, 0), new Color(255, 0, 0), 
					new Color(255, 255, 0), new Color(255, 0, 255),
					new Color(0, 255, 255) };
  static public Color _edgecolor = new Color(255, 255, 0);
  static public Color _textcolor = new Color(255, 255, 255);
  static public Color _bgcolor = new Color(0, 0, 128);
  static public Font _textfont = new Font("HELVETICA", Font.PLAIN, 8);
  static public final int MULTI_CIRCLE = 0;
  static public final int ONE_CIRCLE = 1;

  private Frame _frame;
  private PartitionPanel _panel;
  private Button _button;
  private int _w, _h;

  public PartitionGraphViewer(WeightedGraph[] partition, int w, int h, Observer callback, int vizstyle) {
    addObserver(callback);
    _button = new Button("do it");
    _button.addActionListener(this);
    _frame = new Frame("Partition Graph Mojo Screen");

    _w = w;
    _h = h - 20;

    _panel = new PartitionPanel(partition, vizstyle);
    _panel.setSize(_w, _h);
    _panel.setBackground(_bgcolor);

    _frame.add(_panel, "Center");
    _frame.add(_button, "South");
    _frame.setSize(_w, h);
    _frame.show();
    _panel.repaint();
  }

  public void actionPerformed(ActionEvent e) {
    setChanged();
    notifyObservers(this);
    _panel.repaint();
  }

  class PartitionPanel extends Panel {
    
    private WeightedGraph[] _partition;

    private int _vizstyle;

    public PartitionPanel(WeightedGraph[] partition, int vizstyle) {
      _partition = partition;
      _vizstyle = vizstyle;
    }

    public void paint(Graphics g) {
      
      switch (_vizstyle) {
      case PartitionGraphViewer.MULTI_CIRCLE:
	vizMultiCircle(g);
	break;
      case PartitionGraphViewer.ONE_CIRCLE:
	vizOneCircle(g);
	break;
      default:
	vizMultiCircle(g);
	break;
      }

    }

    public void vizOneCircle(Graphics g) {

      Dimension dim = getSize();
      int i, j, num_nodes = 0;
      Enumeration e, adj, weights;
      Point[] nodes;
      Point from, to;
      WGNode node, adjnode;
      long weight;
      double colorcoef;

      // g.clearRect(0,0,dim.width,dim.height);

      // first get total number of nodes
      int k = _partition.length;
      Hashtable nodemap = new Hashtable(2 * k * _partition[0].size());

      for (i = 0; i < k; i++) num_nodes += _partition[i].size(); 
      nodes = new Point[num_nodes];
      layoutCircle((int) (0.4*dim.width), num_nodes, new Point(dim.width/2, dim.height/2), nodes);

      // now layout and draw each node and put it in a big hash table.  yum.
      g.setFont(PartitionGraphViewer._textfont);
      // System.err.print("draw node:  ");
      j = 0;
      for (i = 0; i < k; i++) {
	e = _partition[i].getNodes();
	while (e.hasMoreElements()) {
	  node = (WGNode) e.nextElement();
	  nodemap.put(node._name, nodes[j]);
	  g.setColor(PartitionGraphViewer._nodecolors[i % PartitionGraphViewer._nodecolors.length]);
	  g.fillOval(nodes[j].x-4,nodes[j].y-4,8,8);
	  // System.err.print("("+nodes[j].x+","+nodes[j].y+") ");
	  g.setColor(PartitionGraphViewer._textcolor);
	  g.drawString(node._name, nodes[j].x+6, nodes[j].y);
	  j++;
	}
      }

      // System.err.print("\n");

      // now draw the edges
      // System.err.print("draw edge:  ");
      for (i = 0; i < k; i++) {
	e = _partition[i].getNodes();
	while (e.hasMoreElements()) {
	  node = (WGNode) e.nextElement();
	  adj = node.getAdjacent();
	  weights = node.getWeights();
	  from = (Point) nodemap.get(node._name);
	  while (adj.hasMoreElements()) {
	    adjnode = (WGNode) adj.nextElement();
	    weight = ((Long) weights.nextElement()).longValue();
	    colorcoef = 1.0 - Math.exp(-weight / 20);
	    g.setColor(new Color((int) (colorcoef*PartitionGraphViewer._edgecolor.getRed()),
				 (int) (colorcoef*PartitionGraphViewer._edgecolor.getGreen()),
				 (int) (colorcoef*PartitionGraphViewer._edgecolor.getBlue())));
	    to = (Point) nodemap.get(adjnode._name);
	    // System.err.print("("+from.x+","+from.y+")->("+to.x+","+to.y+") ");
	    g.drawLine(from.x, from.y, to.x, to.y);
	  }
	}
      }
      // System.err.print("\n");
      
    }

    public void vizMultiCircle(Graphics g) {

      Dimension dim = getSize();
      int i, j;
      Enumeration e, adj, weights;
      Point[] partition_ctr, nodes;
      Point from, to;
      WGNode node, adjnode;
      long weight;
      double colorcoef;

      g.clearRect(0,0,dim.width,dim.height);

      // first get centers of partitions
      int k = _partition.length;
      Hashtable nodemap = new Hashtable(2 * k * _partition[0].size());
      partition_ctr = new Point[k];
      layoutCircle((int)(dim.height/3), k, new Point(dim.width/2,dim.height/2), partition_ctr);

      // now layout and draw each node and put it in a big hash table.  yum.
      g.setFont(PartitionGraphViewer._textfont);
      // System.err.print("draw node:  ");
      for (i = 0; i < k; i++) {
	nodes = new Point[_partition[i].size()];
	layoutCircle((int) (dim.height/7 * Math.sin(Math.PI / nodes.length)), 
		     nodes.length, partition_ctr[i], nodes);
	e = _partition[i].getNodes();
	for (j = 0; j < nodes.length; j++) {
	  node = (WGNode) e.nextElement();
	  nodemap.put(node._name, nodes[j]);
	  g.setColor(PartitionGraphViewer._nodecolors[i % PartitionGraphViewer._nodecolors.length]);
	  g.fillOval(nodes[j].x-4,nodes[j].y-4,8,8);
	  // System.err.print("("+nodes[j].x+","+nodes[j].y+") ");
	  g.setColor(PartitionGraphViewer._textcolor);
	  g.drawString(node._name, nodes[j].x+6, nodes[j].y);
	}
      }
      // System.err.print("\n");

      // now draw the edges
      // System.err.print("draw edge:  ");
      for (i = 0; i < k; i++) {
	e = _partition[i].getNodes();
	while (e.hasMoreElements()) {
	  node = (WGNode) e.nextElement();
	  adj = node.getAdjacent();
	  weights = node.getWeights();
	  from = (Point) nodemap.get(node._name);
	  while (adj.hasMoreElements()) {
	    adjnode = (WGNode) adj.nextElement();
	    weight = ((Long) weights.nextElement()).longValue();
	    colorcoef = 1.0 - Math.exp(-weight / 20);
	    g.setColor(new Color((int) (colorcoef*PartitionGraphViewer._edgecolor.getRed()),
				 (int) (colorcoef*PartitionGraphViewer._edgecolor.getGreen()),
				 (int) (colorcoef*PartitionGraphViewer._edgecolor.getBlue())));
	    to = (Point) nodemap.get(adjnode._name);
	    // System.err.print("("+from.x+","+from.y+")->("+to.x+","+to.y+") ");
	    g.drawLine(from.x, from.y, to.x, to.y);
	  }
	}
      }
      // System.err.print("\n");

    }

    public void layoutCircle(int r, int n, Point center, Point[] coords) {
      
      if (n == 1) {
	coords[0] = new Point(center.x, center.y);
      } else {
	double dtheta = 2 * Math.PI / n;
	for (int i = 0; i < n; i++) 
	  coords[i] = new Point(((int) (r * Math.cos(i * dtheta))) + center.x,
				((int) (-r * Math.sin(i * dtheta))) + center.y);
      }

    }
      
  }

}
  


  

  


