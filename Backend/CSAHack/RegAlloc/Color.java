/*
 * RegAlloc/Color.java
 *  graph coloring algorithm for register allocation.
 *
 * ALGORITHM: Worklist-based algorithm described in 
 * 'Modern Compiler Implementation in Java'
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;

import harpoon.Backend.CSAHack.RegAlloc.RegWork.Moves;
import harpoon.Backend.CSAHack.RegAlloc.RegWork.NodeSet;

import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Temp.TempMap;

import harpoon.Backend.CSAHack.FlowGraph.FlowGraph;

import java.util.Vector;
import java.util.Hashtable;

/**
 * Graph coloring algorithm for register allocation.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see	RegAlloc.RegAlloc
 */
class Color implements TempMap {
  FlowGraph flow;
  InterferenceGraph ig;
  TempList registers;
  TempMap  initial;
  Vector reg = new Vector(); // vector of register temps
  int K; // number of registers

  boolean debug = false; // en-/dis- able debugging information.

  /**
   * @return TempList of spilled registers
   */
  public TempList spills() {
    TempList r = null;
    for (NodeList nl = sets.spilled.nodes(); nl!=null; nl=nl.tail)
      r = new TempList(ig.gtemp(nl.head), r);
    return r;
  }

  /**
   * Color an interference graph.
   * @param flow flow graph defining moves able to be coalesced
   * @param ig interference graph for the temporaries
   * @param initial Initial coloring of machine registers
   * @param registers List of pre-colored machine registers.
   */
  public Color(FlowGraph flow,
	       InterferenceGraph ig,
	       TempMap initial,
	       TempList registers) {
    this.flow = flow;
    this.ig = ig;
    this.registers = registers;
    this.initial = initial;
    // Set K to the number of registers
    for (TempList tl = registers; tl!=null; tl=tl.tail)
      reg.addElement(tl.head);
    K = reg.size();

    main();
  }

  private void main() {
    Build();
    check();
    MakeWorklist();
    check();

    do {
      if (!sets.worklist.simplify.empty())	{Simplify();check();}
      else if (!moves.worklist.empty())		{Coalesce();check();}
      else if (!sets.worklist.freeze.empty())	{Freeze();  check();}
      else if (!sets.worklist.spill.empty())	{SelectSpill();check();}
    } while (!(sets.worklist.simplify.empty() && 
	       sets.worklist.freeze.empty() &&
	       sets.worklist.spill.empty() &&
	       moves.worklist.empty()));

    check();
    AssignColors();

  }

  private Moves moves;
  private NodeSet sets;

  private Hashtable moveList, degree;
  private Hashtable alias, color;

  void Build() {
    alias = new Hashtable();
    sets = new NodeSet(registers, ig);
    moves = new Moves(flow);

    // Create mapping of interference graph node to the move instruction
    // nodes it is associated with.
    moveList = new Hashtable();
    for (NodeList nl = flow.nodes(); nl!=null; nl=nl.tail) {
      if (flow.isMove(nl.head)) {
	for (TempList tl = merge(flow.use(nl.head),flow.def(nl.head));
	     tl!=null; tl=tl.tail) {
	  Node tempnode = ig.tnode(tl.head);
	  // quick error check hack
	  if (tempnode == null) // this should never happen!
	    throw new Error("Interference graph doesn't contain a node "+
			    "for temporary "+tl.head.toString()+", which "+
			    "is use/def in node "+nl.head.toString());
	  moveList.put(tempnode,
		       new NodeList(nl.head,
				    (NodeList) moveList.get(tempnode)));
	}
      }
    }
    // Initialize degree with computed degree of each node.
    degree = new Hashtable();
    for (NodeList nl = ig.nodes(); nl!=null; nl=nl.tail)
      degree.put(nl.head, new Integer(
		 len(union(nl.head.succ(), nl.head.pred()))));
  }
  void MakeWorklist() {
    while (!sets.initial.empty()) {
      Node n = sets.initial.head();
      sets.initial.remove(n);

      if(Degree(n) >= K)
	sets.worklist.spill.add(n);
      else if (MoveRelated(n))
	sets.worklist.freeze.add(n);
      else
	sets.worklist.simplify.add(n);
    }
  }
  void Simplify() {
    Node n = sets.worklist.simplify.head();
    sets.worklist.simplify.remove(n);
    sets.selectStack.push(n);
    for (NodeList nl=Adjacent(n); nl!=null; nl=nl.tail)
      DecrementDegree(nl.head);
  }
  void Coalesce() {
    Node m = moves.worklist.head();

    if (!flow.isMove(m)) throw new Error("Internal error");
    TempList use = flow.use(m);
    TempList def = flow.def(m);
    if (use.tail != null || def.tail != null)
      throw new Error("Node supposed to be a MOVE");

    Node x = GetAlias(ig.tnode(def.head));
    Node y = GetAlias(ig.tnode(use.head));
    Node u=x,v=y;
    if (sets.precolored.contains(y)) {
      u=y; v=x;
    }
    moves.worklist.remove(m);
    if (u==v) {
      // Coalesce u and v
      moves.coalesced.add(m);
      AddWorkList(u);
    } else if (sets.precolored.contains(v) || u.adj(v)) {
      // Constrain u
      moves.constrained.add(m);
      AddWorkList(u);
      AddWorkList(v);
    } else if ((sets.precolored.contains(u) && OK(Adjacent(v),u)) ||
	       ((!sets.precolored.contains(u)) &&
		Conservative(merge(Adjacent(u), Adjacent(v))))) {
      // Combine u and v
      moves.coalesced.add(m);
      Combine(u,v);
      AddWorkList(u);
    } else
      moves.active.add(m);
  }
  void Freeze() {
    Node u = sets.worklist.freeze.head();
    sets.worklist.freeze.remove(u);
    sets.worklist.simplify.add(u);
    FreezeMoves(u);
  }
  void FreezeMoves(Node u2) {
    for (NodeList m = NodeMoves(u2); m!=null; m=m.tail) {
      if (moves.active.contains(m.head))
	moves.active.remove(m.head);
      else
	moves.worklist.remove(m.head);
      moves.frozen.add(m.head);
      
      if (!flow.isMove(m.head)) throw new Error("Internal error");
      TempList use = flow.use(m.head);
      TempList def = flow.def(m.head);
      if (use.tail != null || def.tail != null)
	throw new Error("Node supposed to be a MOVE");
      
      Node u = ig.tnode(def.head);
      Node v = ig.tnode(use.head);
      if ( (NodeMoves(u)==null) && (Degree(u) < K) ) {
	sets.worklist.freeze.remove(u);
	sets.worklist.simplify.add(u);
      }
      if ( (NodeMoves(v)==null) && (Degree(v) < K) ) {
	sets.worklist.freeze.remove(v);
	sets.worklist.simplify.add(v);
      }
    }
  }
  void SelectSpill() {
    Node bestspill = sets.worklist.spill.head();
    int  bestscore = ig.spillCost(bestspill);
    for (NodeList nl = sets.worklist.spill.nodes(); nl!=null; nl=nl.tail) {
      int nodescore = ig.spillCost(nl.head);
      // bestscore is lowest spillCost
      if (nodescore < bestscore) {
	bestspill = nl.head;
	bestscore = nodescore;
      }
    }
    // OK, spill this guy.
//System.out.println("Spilling "+ig.gtemp(bestspill).toString());
    sets.worklist.spill.remove(bestspill);
    sets.worklist.simplify.add(bestspill);
    FreezeMoves(bestspill);
  }
  void AssignColors() {
    int i=0, k;
    color = new Hashtable();

    for(TempList tl=registers; tl!=null; tl=tl.tail, i++)
      color.put(tl.head, new Integer(i));
    
    while(!sets.selectStack.empty()) {
      Node n = (Node) sets.selectStack.pop();
      boolean okColors[] = new boolean[K];
      for (k=0; k<K; k++)
	okColors[k]=true;
      for (NodeList nl = union(n.succ(),n.pred()); nl!=null; nl=nl.tail) {
	Node w = GetAlias(nl.head);
	if (sets.colored.contains(w) || sets.precolored.contains(w))
	  okColors[getColor(ig.gtemp(w))]=false;
      }
      for (k=0; k<K; k++)
	if (okColors[k]) {
	  sets.colored.add(n);
	  color.put(ig.gtemp(n), new Integer(k));
	  break;
	}
      if (k==K)  // no ok colors!
	sets.spilled.add(n);
    }
    for (NodeList nl = sets.coalesced.nodes(); nl!=null; nl=nl.tail)
      color.put(ig.gtemp(nl.head), 
		new Integer(getColor(ig.gtemp(GetAlias(nl.head)))));
//    for (java.util.Enumeration e=color.keys(); e.hasMoreElements(); ) {
//      Temp t = (Temp) e.nextElement();
//      System.out.println(t.toString()+" <= "+
//			 ((Temp)reg.elementAt(getColor(t))).toString());
//    }
  }
  int getColor(Temp w) {
    harpoon.Util.Util.assert(color.get(w)!=null, "No color for "+w);
    return ((Integer)color.get(w)).intValue();
  }

  public Temp tempMap(Temp t) {
    return initial.tempMap((Temp)reg.elementAt(getColor(t)));
  }

  // Utility functions:
  void AddEdge(Node u, Node v) {
    if (!u.adj(v) && (u!=v)) {
      ig.addEdge(u,v);
//      if (!sets.precolored.contains(u))
	degree.put(u, new Integer(Degree(u)+1));
//      if (!sets.precolored.contains(v))
	degree.put(v, new Integer(Degree(v)+1));
    }
  }

  NodeList Adjacent(Node n) {
    NodeList r=null;
    for (NodeList nl=union(n.succ(),n.pred()); nl!=null; nl=nl.tail)
      if (!sets.coalesced.contains(nl.head) &&
	  !sets.selectStack.contains(nl.head))
	r = new NodeList(nl.head, r);
    return r;
  }
  void AddWorkList(Node u) {
    if ((!sets.precolored.contains(u)) && 
	(!MoveRelated(u)) && 
	(Degree(u) < K)) {
      sets.worklist.freeze.remove(u);
      sets.worklist.simplify.add(u);
    }
  }
  boolean OK(Node t, Node r) {
    return ((Degree(t) < K) ||
	    sets.precolored.contains(t) ||
	    t.adj(r));
  }
  boolean OK(NodeList nl, Node r) {
    for ( ; nl!=null; nl=nl.tail)
      if (!OK(nl.head, r)) return false;
    return true;
  }
  boolean Conservative(NodeList nl) {
    int k = 0;
    for ( ; nl!=null; nl=nl.tail)
      if (Degree(nl.head) >= K)
	k++;
    return ( k < K );
  }
  Node GetAlias(Node n) {
    if (sets.coalesced.contains(n))
      return GetAlias((Node)alias.get(n));
    else
      return n;
  }
  
  void Combine(Node u, Node v) {
    if (sets.worklist.freeze.contains(v)) 
      sets.worklist.freeze.remove(v);
    else
      sets.worklist.spill.remove(v);

    sets.coalesced.add(v);
    alias.put(v, u);

    moveList.put(u, merge((NodeList)moveList.get(u),
			  (NodeList)moveList.get(v)));
    for (NodeList nl = Adjacent(v); nl!=null; nl=nl.tail) {
      AddEdge(nl.head,u);
      DecrementDegree(nl.head);
    }
    if (Degree(u) >= K && sets.worklist.freeze.contains(u)) {
      sets.worklist.freeze.remove(u);
      sets.worklist.spill.add(u);
    }
  }

  int Degree(Node m) {
    return ((Integer)degree.get(m)).intValue();
  }
  void DecrementDegree(Node m) {
    int d = Degree(m);
    degree.put(m, new Integer(d-1));
//System.out.println("DecDegree("+ig.gtemp(m).toString()+"), "+d);
    if (d == K) {
if (sets.worklist.spill.contains(m)) {
      EnableMoves(new NodeList(m, Adjacent(m)));
      sets.worklist.spill.remove(m);
      if (MoveRelated(m))
	sets.worklist.freeze.add(m);
      else
	sets.worklist.simplify.add(m);
}
    }
  }
  void EnableMoves(NodeList nl) {
    for ( ; nl!=null; nl=nl.tail) 
      for (NodeList ml=NodeMoves(nl.head); ml!=null; ml=ml.tail)
	if (moves.active.contains(ml.head)) {
	  moves.active.remove(ml.head);
	  moves.worklist.add(ml.head);
	}
  }
  boolean MoveRelated(Node n) {
    return NodeMoves(n)!=null;
  }
  NodeList NodeMoves(Node n) {
    NodeList r = null;
    // Node is an InterferenceGraph node, corresponding to a temp.
    // loop through move instruction nodes associated with this temp.
    for (NodeList nl = (NodeList) moveList.get(n); nl!=null; nl=nl.tail)
      // if this move is on the activeMoves or worklistMoves lists,
      // then this (interference graph) node is regarded as 'move-related'
      if (moves.active.contains(nl.head) ||
	  moves.worklist.contains(nl.head))
	r = new NodeList(nl.head, r);
    return r;
  }

  // More utility functions...

  NodeList union(NodeList a, NodeList b) {
    NodeList r=a;
    for( ; b!=null; b=b.tail) {
      NodeList nlp;
      for (nlp = a; nlp!=null; nlp=nlp.tail)
	if (b.head == nlp.head) break;
      if (nlp==null)
	r = new NodeList(b.head, r);
    }
    return r;
  }
  TempList merge(TempList a, TempList b) {
    TempList p, r=null;
    for (p=a; p!=null; p=p.tail) { 
      r=new TempList(p.head, r);
    }
    for (p=b; p!=null; p=p.tail) {
      r=new TempList(p.head, r);
    }
    return r;
  }
  NodeList merge(NodeList a, NodeList b) {
    NodeList p, r=null;
    for (p=a; p!=null; p=p.tail) { 
      r=new NodeList(p.head, r);
    }
    for (p=b; p!=null; p=p.tail) {
      r=new NodeList(p.head, r);
    }
    return r;
  }
  int len(TempList tl) {
    int n=0;
    for ( ; tl!=null; tl=tl.tail)
      n++;
    return n;
  }
  int len(NodeList nl) {
    int n=0;
    for ( ; nl!=null; nl=nl.tail)
      n++;
    return n;
  }
  // check that the 'degree' stored in the hashtable matches the adjacency.
  void check(Node n) {
    if (len(Adjacent(n)) != Degree(n)) 
      throw new Error("Inconsistent data for node "+ig.gtemp(n).toString()+
		      ": Adj("+len(Adjacent(n))+") != Degree("+Degree(n)+")");
  }
  void check(NodeList nl) {
    for ( ; nl!=null; nl=nl.tail)
      if (!sets.coalesced.contains(nl.head) &&
	  !sets.selectStack.contains(nl.head))
	check(nl.head);
  }
  // perform sanity checks on the sets and the interference graph nodes.
  void check() {
    if (debug) {
      sets.check();
      check(ig.nodes());
    }
  }
}
