package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;

import java.util.Hashtable;
import java.util.Stack;

class Set {
  Hashtable lookup;
  String name;

  boolean debug = false;

  public Set() {
    lookup = new Hashtable();
    name = "unknown";
  }
  public Set(String name) {
    this();
    this.name = name;
  }
  private String nodename(Node n) {
    return n.toString();
  }
  public void add(Node n) {
    if(debug) 
      System.out.println(name+": add("+nodename(n)+")");
    if (this.contains(n)) return;
    lookup.put(n, n);
  }
  public boolean empty() {
    return lookup.isEmpty();
  }
  public void remove(Node n) {
    if(debug) 
      System.out.println(name+": remove("+nodename(n)+")");
    lookup.remove(n);
  }
  public boolean contains(Node n) {
    return (lookup.get(n) != null);
  }
  public Node head() {
    return (Node) lookup.keys().nextElement();
  }
  public NodeList nodes() {
    NodeList nl = null;
    java.util.Enumeration e=lookup.keys();
    while (e.hasMoreElements())
      nl = new NodeList((Node)e.nextElement(), nl);
    return nl;
  }
  public int size() {
    return lookup.size();
  }
  public String toString() { return name+": "+lookup.toString(); }
}

