// -*-Mode: Java-*- 
// WGNode.java -- 
// Author: Mark Foltz <mfoltz@ai.mit.edu> 
// Maintainer: Mark Foltz <mfoltz@ai.mit.edu> 
// Version: 
// Created: <Thu Oct 22 23:42:00 1998> 
// Time-stamp: <1998-12-03 00:38:06 mfoltz> 
// Keywords: 

package harpoon.Analysis.Partition;

import java.util.Vector;
import java.util.Enumeration;

/** A node representing an object creation site.
 * 
 * @author  Mark A. Foltz <mfoltz@ai.mit.edu>
 * @version 
 */

public class WGNode {

  /** A unique name for the node. */
  public String _name;
  /** A value for this node. */
  public Object _value;
  /** The nodes adjacent to this node. */
  public Vector _edges = new Vector();
  /** The weights of the edges incident to this node.  The order of <code>_edges</code> and <code>_weights</code> must
   be kept consistent. */
  public Vector _weights = new Vector();
  /** <code>true</code> if the node was created as a dummy. */
  public boolean _dummy = false;
  /** The difference between external and internal edge weights. */
  public long _d;
  /** The binding for this node.  If > 0, to partition n; if < 0, unbound. */
  public int _binding;
  
  /** Create a node with the given <code>name</code> and <code>value</code>. */
  public WGNode(String name, Object value) {
    _name = name;
    _value = value;
    _binding = -1;
  }

  /** Create a clone of <code>node</code>. */
  public WGNode(WGNode node) {
    _name = node._name;
    _value = node._value;
    _binding = node._binding;
    _edges = (Vector) node._edges.clone();
    _weights = (Vector) node._weights.clone();
    _dummy = node._dummy;
    _d = node._d;
  }

  /** Get the nodes adjacent to this node. */
  public Enumeration getAdjacent() {
    return _edges.elements();
  }

  /** Return <code>true</code> if <code>node</code> is adjacent to this node in the graph. */
  public boolean adjacentTo(WGNode node) {
    return (_edges.indexOf(node) > -1);
  }

  /** Get the weights of the edges incident to this node. */
  public Enumeration getWeights() {
    return _weights.elements();
  }
  
  /** Get the weight of the edge to <code>node</code>, or 0 if <code>node</code> is not adjacent. */
  public long getWeight(WGNode node) {
    int i = _edges.indexOf(node);
    if (i < 0) return 0;
    else return ((Long) _weights.elementAt(i)).longValue();
  }

  /** Return <code>true</code> if this node is adjacent to any node. */
  public boolean isConnected() {
    return !_edges.isEmpty();
  }

  /** Return the degree of this node. */
  public int degree() {
    return _edges.size();
  }

  /** Add <code>weight</code> to the edge between this node and <code>to</code>. */
  public void addToEdge(WGNode to, long weight) {
    int i = _edges.indexOf(to);
    if (i < 0) {
      _edges.addElement(to);
      _weights.addElement(new Long(weight));
    } else {
      _weights.setElementAt(new Long(((Long) _weights.elementAt(i)).longValue()+weight),i);
    }
  }

  /** Set the weight of the edge between this node and <code>to</code> to be <code>weight</code> */
  public void setEdge(WGNode to, long weight) {
    int i = _edges.indexOf(to);
    if (i < 0) {
      _edges.addElement(to);
      _weights.addElement(new Long(weight));
    } else {
      _weights.setElementAt(new Long(weight),i);
    }
  }

  /** Remove the outgoing edge from this node to to. */ 
  private void removeOutgoingEdge(WGNode to) {
    int i = _edges.indexOf(to);
    //    System.err.println("Removing edge "+this._name+" <--> "+to._name+" at "+i+"\n");
    if (i > -1) {
      _edges.removeElementAt(i);
      _weights.removeElementAt(i);
    }
  }

  /** Remove the incoming edge from from to this node. */ 
  private void removeIncomingEdge(WGNode from) {
    from.removeOutgoingEdge(this);
  }

  /** Remove the edge between this node and <code>to</code>. */
  public void removeEdge(WGNode to) {
    to.removeOutgoingEdge(this);
    removeOutgoingEdge(to);
  }

  /** Remove all the incoming edges to this node. */
  private void removeIncomingEdges() {
    Enumeration adjacents = getAdjacent();
    WGNode node;
    while (adjacents.hasMoreElements()) {
      node = (WGNode) adjacents.nextElement();
      node.removeOutgoingEdge(this);
    }
  }

  /** Remove all the outgoing edges from this node. */
  private void removeOutgoingEdges() {
    _edges.removeAllElements();
    _weights.removeAllElements();
  }

  /** Remove all of the edges incident to this node. */
  public void removeEdges() {
    WGNode node;
    while (!_edges.isEmpty()) {
      node = ((WGNode) _edges.lastElement());
      node.removeOutgoingEdge(this);
      _edges.removeElementAt(_edges.size()-1);
    }
  }

  /** Return an external string representation of this node. */
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

    
    
