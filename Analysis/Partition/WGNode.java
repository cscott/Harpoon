// WGNode.java, created Mon Nov 16 23:33:21 1998 by mfoltz
// Copyright (C) 1998 Mark A. Foltz <mfoltz@ai.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// -*-Mode: Java-*- 
// WGNode.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Thu Oct 22 23:42:00 1998> 
// Time-stamp: <1998-11-26 11:45:04 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.util.Vector;
import java.util.Enumeration;

/**
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version $Id: WGNode.java,v 1.4 2002-02-25 20:58:28 cananian Exp $
 */

public class WGNode {

  public String _name;
  public Object _value;
  public Vector _edges = new Vector();
  public Vector _weights = new Vector();
  public boolean _dummy = false;
  public long _d;
  public int _binding;
  
  public WGNode(String name, Object value) {
    _name = name;
    _value = value;
    _binding = -1;
  }

  public WGNode(WGNode node) {
    _name = node._name;
    _value = node._value;
    _binding = node._binding;
    _edges = (Vector) node._edges.clone();
    _weights = (Vector) node._weights.clone();
    _dummy = node._dummy;
    _d = node._d;
  }

  public Enumeration getAdjacent() {
    return _edges.elements();
  }

  public boolean adjacentTo(WGNode node) {
    return (_edges.indexOf(node) > -1);
  }

  public Enumeration getWeights() {
    return _weights.elements();
  }
  
  public long getWeight(WGNode node) {
    int i = _edges.indexOf(node);
    if (i < 0) return 0;
    else return ((Long) _weights.elementAt(i)).longValue();
  }

  public boolean isConnected() {
    return !_edges.isEmpty();
  }

  public int degree() {
    return _edges.size();
  }

  public void addToEdge(WGNode to, long weight) {
    int i = _edges.indexOf(to);
    if (i < 0) {
      _edges.addElement(to);
      _weights.addElement(new Long(weight));
    } else {
      _weights.setElementAt(new Long(((Long) _weights.elementAt(i)).longValue()+weight),i);
    }
  }

  public void setEdge(WGNode to, long weight) {
    int i = _edges.indexOf(to);
    if (i < 0) {
      _edges.addElement(to);
      _weights.addElement(new Long(weight));
    } else {
      _weights.setElementAt(new Long(weight),i);
    }
  }

  public void removeOutgoingEdge(WGNode to) {
    int i = _edges.indexOf(to);
    //    System.err.println("Removing edge "+this._name+" <--> "+to._name+" at "+i+"\n");
    if (i > -1) {
      _edges.removeElementAt(i);
      _weights.removeElementAt(i);
    }
  }

  public void removeIncomingEdge(WGNode from) {
    from.removeOutgoingEdge(this);
  }

  public void removeEdge(WGNode to) {
    to.removeOutgoingEdge(this);
    removeOutgoingEdge(to);
  }

  public void removeIncomingEdges() {
    Enumeration adjacents = getAdjacent();
    WGNode node;
    while (adjacents.hasMoreElements()) {
      node = (WGNode) adjacents.nextElement();
      node.removeOutgoingEdge(this);
    }
  }

  public void removeOutgoingEdges() {
    _edges.removeAllElements();
    _weights.removeAllElements();
  }

  public void removeEdges() {
    WGNode node;
    while (!_edges.isEmpty()) {
      node = ((WGNode) _edges.lastElement());
      node.removeOutgoingEdge(this);
      _edges.removeElementAt(_edges.size()-1);
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(_d+" "+_name+" <--->\n");
    Enumeration adj = getAdjacent();
    Enumeration w = getWeights();
    WGNode n;
    Long l;
    while (adj.hasMoreElements()) {
      n = (WGNode) adj.nextElement();
      l = (Long) w.nextElement();
      sb.append("\t"+l+"  "+n._name+"\n");
    }
    return sb.toString();
  }

}

    
    
