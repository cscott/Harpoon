/*
 * RegAlloc/Liveness.java
 *  examine flow graph to determine liveness of temporaries.
 *
 * ALGORITHM: Worklist-based algorithm utilizing node pre-sorting
 *  with a DFS for speed.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Temp.Temp;
import harpoon.Temp.TempList;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;
import harpoon.Backend.CSAHack.FlowGraph.FlowGraph;

import harpoon.Util.Collections.WorkSet;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * An interference graph by examination of liveness of nodes
 * in a flow graph.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 */
public class Liveness extends InterferenceGraph {
  Hashtable temp2node = new Hashtable();
  Hashtable nodeuses  = new Hashtable();

  MoveList movelist = null;

  boolean debug = false; // set to true to dump debug information.

  /**
   * Construct interference graph from flow graph.
   */
  public Liveness(FlowGraph flow) {

    WorkSet worklist = new WorkSet();
    DFS(flow.nodes().head, worklist); // add some nodes to worklist.

    // create table of TempSets to represent liveIn and liveOut
    Hashtable liveIn = new Hashtable();
    Hashtable liveOut= new Hashtable();
    for (NodeList nl=flow.nodes(); nl!=null; nl=nl.tail) {
      liveIn.put(nl.head, new TempSet());
      liveOut.put(nl.head,new TempSet());
    }

    // Worklist algorithm for computing liveness.
    while(!worklist.isEmpty()) {
      Node n = (Node) worklist.pop();

      TempSet out= (TempSet) liveOut.get(n);
      TempSet in = (TempSet) liveIn.get(n);

      in.resetChange();

      // compute new out set...
      for (NodeList nl = n.succ(); nl!=null; nl=nl.tail)
	out.union((TempSet)liveIn.get(nl.head));
      
      // compute the new in set (in = in union (out - def)
      TempSet t = new TempSet();
      t.union(out);
      t.not(flow.def(n));

      in.union(flow.use(n));
      in.union(t);
      
      // if new in set included more members than the old...
      if (in.hasChanged()) {
	// add all the predecessors of this node to the worklist.
	for (NodeList nl = n.pred(); nl!=null; nl=nl.tail)
	    worklist.add(nl.head);
      }
    }
    // done!

    // show me the liveOut sets.
    if (debug)
      show_liveOut(flow, liveOut);

    // Make interference graph.
    createGraph(flow, liveIn, liveOut);

    // assemble movelist.
    makeMoveList(flow);

//    show(System.out);
  }

  // dump the live-out sets for debugging.
  private void show_liveOut(FlowGraph flow, Hashtable liveOut) {
    for(NodeList nl=flow.nodes(); nl!=null; nl=nl.tail) {
      System.out.println(nl.head.toString()+":");
      for (TempList tl=((TempSet)liveOut.get(nl.head)).members(); 
	   tl!=null; tl=tl.tail)
	System.out.print(" "+tl.head.toString());
      System.out.print("\n");
    }
  }

  // examine nodes to create move list
  private void makeMoveList(FlowGraph flow) {
    for (NodeList nl = flow.nodes(); nl!=null; nl=nl.tail)
      if (flow.isMove(nl.head))
	movelist = new MoveList(tnode(flow.use(nl.head).head),
				tnode(flow.def(nl.head).head),
				movelist);
  }

  // add all nodes in (reverse) DFS order.
  private void DFS(Node n, WorkSet worklist) {
    WorkSet stack = new WorkSet();
    stack.add(n);
    while (!stack.isEmpty()) {
	Node nn = (Node) stack.pop();
	worklist.add(nn);
	for (NodeList nl=nn.succ(); nl!=null; nl=nl.tail)
	    if (!worklist.contains(nl.head))
		stack.add(nl.head);
    }
  }

  // Create nodes and edges of interference graph from the liveness
  // information.
  private void createGraph(FlowGraph flow,
			   Hashtable liveIn, Hashtable liveOut) {
    // for each node in the flow graph
    for (NodeList nl = flow.nodes(); nl!=null; nl=nl.tail)
      // for each definition in that node
      for (TempList dl = flow.def(nl.head); dl!=null; dl=dl.tail)
	// for each live temporary at that node
	for (TempList tl = ((TempSet)liveOut.get(nl.head)).members();
	     tl!=null; tl=tl.tail) 
	  // add interference edges, conditionally
	  addSomeEdges(flow, nl.head, dl.head, tl.head);

    //-------------------------------------------------------
    
    // Now count defs and uses of each variable, to aid in the
    // later computation of spillCost()
    
    // for each node in the flow graph
    for (NodeList nl = flow.nodes(); nl!=null; nl=nl.tail) {
      // for each definition in that node
      for (TempList dl = flow.def(nl.head); dl!=null; dl=dl.tail) {
	// increment the nodeuse
	Node defnode = tnode(dl.head);
	Integer n = (Integer) nodeuses.get(defnode);
	if (n==null) n=new Integer(0);
	nodeuses.put(defnode, new Integer(n.intValue()+1));
      }
      // for each use in that node
      for (TempList dl = flow.use(nl.head); dl!=null; dl=dl.tail) {
	// increment the nodeuse
	Node usenode = tnode(dl.head);
	Integer n = (Integer) nodeuses.get(usenode);
	if (n==null) n=new Integer(0);
	nodeuses.put(usenode, new Integer(n.intValue()+1));
      }
    }
  }
  private void addSomeEdges(FlowGraph flow, Node flownode,
			    Temp deftemp, Temp livetemp) {
    // if this is a move instruction, maybe we don't want to add this edge
    if (flow.isMove(flownode)) 
      // see if this livetemp is part of the source of this move
      for (TempList sl = flow.use(flownode); sl!=null; sl=sl.tail)
	// if so, don't add an edge
	if (sl.head == livetemp)
	  return;
    
    // tests okay, add the edge.
    addEdge(tnode(deftemp), tnode(livetemp));
    addEdge(tnode(livetemp),tnode(deftemp));
  }
	
  /**
   * @param temp a temporary used in the flow graph
   * @return the interference graph node corresponding to the temporary.
   */
  public Node tnode(Temp temp) {
    Node n = (Node) temp2node.get(temp);
    if (n==null) 
      // make new nodes on demand.
      return newNode(temp);
    else return n;
  }
  /**
   * @param node a node in the interference graph.
   * @return the temporary corresponding to the node.
   */
  public Temp gtemp(Node node) {
    if (!(node instanceof TempNode))
      throw new Error("Node "+node.toString()+" not a member of graph.");
    else return ((TempNode)node).temp;
  }
  /**
   * @param node a node in the interference graph.
   * @return an estimate of the cost of spilling this node.
   */
  public int spillCost(Node node) {
    // Our heuristic: the number of uses and defs of this node, divided by
    // degree (to make more conflicted nodes better to spill).
    // Scaled by 1000 to accomodate integer division.
    Integer numuses = (Integer) nodeuses.get(node);
    return 1000*numuses.intValue()/node.degree();
  }
  /**
   * @return a list of moves found in the flow graph.
   */
  public MoveList moves() {
    return movelist;
  }
  public Node newNode(Temp t) {
    TempNode n = new TempNode(this, t);
    temp2node.put(t, n);
    return n;
  } 
}

/**
 * Sub-class to associate a temporary with a Graph.Node.
 * @see harpoon.Temp.Temp
 */
class TempNode extends harpoon.Backend.CSAHack.Graph.Node {
  Temp temp;
  TempNode(harpoon.Backend.CSAHack.Graph.Graph g, Temp t) {
    super(g);
    temp = t;
  }
  public String toString() {
    return temp.toString(); // +"("+super.toString()+")";
  }
  public int hashCode() { return temp.hashCode(); }
}
  
/**
 * A set of temporaries, based on a hashtable.
 */
class TempSet {
  boolean dirty = false;
  Hashtable h = new Hashtable();

  /**
   * create a new empty set.
   */
  TempSet() { }
  /**
   * create a new set.
   * @param tl a list of initial members of the set.
   */
  TempSet(TempList tl) { union(tl); }

  /**
   * add a member to the set, if not previously present.
   * @param t a temporary to add to the set.
   */
  void add(Temp t) {
    if (!contains(t)) {
      h.put(t, new Object());
      dirty=true;
    }
  }
  /**
   * remove an element, if it is a member of the set.
   * @param t a temporary to be removed from the set.
   */
  void remove(Temp t) {
    if (contains(t)) {
      h.remove(t);
      dirty=true;
    }
  }
  /**
   * add to this set all the members of another.
   */
  void union(TempSet  ts) { union(ts.members()); }
  /**
   * add a list of temporaries to this set.
   */
  void union(TempList tl) {
    for ( ; tl!=null; tl=tl.tail)
      add(tl.head);
  }
  /**
   * remove from this set all the members of another.
   */
  void not(TempSet  ts) { not(ts.members()); }
  /**
   * remove a list of temporaries from this set.
   */
  void not(TempList tl) {
    for ( ; tl!=null; tl=tl.tail)
      remove(tl.head);
  }
  /**
   * Create a list of the members of this set.
   */
  TempList members() {
    TempList m = null;
    Enumeration e=h.keys();
    while( e.hasMoreElements() )
      m = new TempList((Temp)e.nextElement(), m);
    return m;
  }
  /**
   * @param t a temporary
   * @return true if t is a member of the set.
   */
  boolean contains(Temp t) {
    return h.get(t)!=null;
  }
  void    resetChange() { dirty = false; }
  /**
   * @return true if members have been added or removed since the last call to resetChange(); false otherwise.
   */
  boolean hasChanged()  { return dirty;  }
}
  
