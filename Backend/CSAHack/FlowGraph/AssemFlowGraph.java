/*
 * FlowGraph/AssemFlowGraph.java
 *  construct a flow-graph from a list of instructions.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */
package FlowGraph;

import Assem.Instr;
import Assem.InstrList;
import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;

import java.util.Hashtable;

/**
 * A flow graph whose nodes correspond to machine instructions.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see Assem.Instr
 */
public class AssemFlowGraph extends FlowGraph {

  /** 
   * Construct an AssemFlowGraph from a list of instructions.
   */
  public AssemFlowGraph(InstrList instrs) {
    Hashtable instr2node;
    Hashtable label2node;

    instr2node = new Hashtable();
    label2node = new Hashtable();

    // Make a node for each instruction.
    //  add these nodes to various lookup tables.
    for (InstrList il = instrs; il!=null; il=il.tail) {
      // some quick error-checking
      if (il.head==null) throw new Error("InstrList error: head==null");
      Node n = newNode(il.head);
      instr2node.put(il.head, n);
      if (il.head instanceof Assem.LABEL) {
	Object prev = label2node.put(((Assem.LABEL)il.head).label, n);
	if (prev!=null)
	  throw new Error("InstrList error: multiple definitions of label "+
			  ((Assem.LABEL)il.head).label.toString());
      }
    }

    // Add edges for each node.
    for (InstrList il = instrs; il!=null; il=il.tail) {
      if (il.head.jumps() == null || il.head.jumps().labels==null) {
	// no funky jumps; just fall through
	if (il.tail != null)
	  addEdge((Node)instr2node.get(il.head),
		  (Node)instr2node.get(il.tail.head));
      } else { // this baby jumps.
	for (Temp.LabelList ll = il.head.jumps().labels;
	     ll!=null;
	     ll=ll.tail) {
	  if (label2node.get(ll.head)==null)
	    throw new Error("Non-existent label used in instruction list: "+
			    ll.head.toString()+" used in "+il.head.toString());
	  addEdge((Node)instr2node.get(il.head),
		  (Node)label2node.get(ll.head));
	}
      }
    }
    // all done.
  }
  /**
   * @param n a node in the flow graph.
   * @return the instruction corresponding to node 'n'
   */
  public Instr instr(Node n) {
    if (!(n instanceof AssemNode))
      throw new Error("not a AssemFlowGraph node");
    else return ((AssemNode)n).instr;
  }
  /**
   * @param n a node in the flow graph.
   * @return a list of temps defined by the instruction at node 'n'
   * @see Temp.Temp
   */
  public TempList def(Node n) {
    return instr(n).def();
  }
  /**
   * @param n a node in the flow graph.
   * @return a list of temps used by the instruction at node 'n'
   * @see Temp.Temp
   */
  public TempList use(Node n) {
    return instr(n).use();
  }
  /**
   * @param n a node in the flow graph.
   * @return true if node n can be eliminated if use() == def(), false otherwise.
   */
  public boolean isMove(Node n) {
    return instr(n) instanceof Assem.MOVE;
  }
  /**
   * Create a new node in the flow graph that does not correspond to
   * an instruction. <BR>
   * <STRONG>NOT RECOMMENDED.</STRONG> Use <TT>newNode(Instr i)</TT> instead.
   */
  public Node newNode() {
    return new AssemNode(this, null);
  }
  /** Create a new node in the flow graph corresponding to the instruction
   * passed as parameter.
   */
  public Node newNode(Instr i) {
    return new AssemNode(this, i);
  }
}

/** 
 * Extension to Graph.Node to represent nodes
 * corresponding to Assem.Instr's.
 */
class AssemNode extends Graph.Node implements Temp.TempMap {
  Instr instr;
  AssemNode(Graph.Graph g, Instr i) {
    super(g);
    instr = i;
  }
  public String toString() {
    if (instr!=null) {
      String r = "Instr["+super.toString()+"]("+instr.format(this).trim()+") ";
//      r+="- use: ";
//      for (TempList tl = instr.use(); tl!=null; tl=tl.tail)
//	r+=tl.head.toString()+" ";
//      r+=", def: ";
//      for (TempList tl = instr.def(); tl!=null; tl=tl.tail)
//	r+=tl.head.toString()+" ";
      return r;
    } else return super.toString();
  }
  public String tempMap(Temp.Temp t) {
    return t.toString();
  }
}
  
