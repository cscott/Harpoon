/*
 * FlowGraph/AssemFlowGraph.java
 *  construct a flow-graph from a list of instructions.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */
package harpoon.Backend.CSAHack.FlowGraph;

import harpoon.Backend.StrongARM.TwoWordTemp;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Properties.UseDefer;
import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * A flow graph whose nodes correspond to machine instructions.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see Assem.Instr
 */
public class AssemFlowGraph extends FlowGraph {
  UseDefer ud;
  boolean twoWord;
  /** 
   * Construct an AssemFlowGraph from a list of instructions.
   */
  public AssemFlowGraph(Instr root, UseDefer ud, boolean twoWord) {
    this.ud = ud;
    this.twoWord = twoWord;
    Util.assert(root.getPrev()==null);
    Hashtable instr2node;
    Hashtable label2node;

    instr2node = new Hashtable();
    label2node = new Hashtable();

    // Make a node for each instruction.
    //  add these nodes to various lookup tables.
    for (Instr il = root; il!=null; il=il.getNext()) {
      Node n = newNode(il);
      instr2node.put(il, n);
      if (il instanceof InstrLABEL) {
	Object prev = label2node.put(((InstrLABEL)il).getLabel(), n);
	if (prev!=null)
	  throw new Error("InstrList error: multiple definitions of label "+
			  ((InstrLABEL)il).getLabel());
      }
    }

    // Add edges for each node.
    for (Instr il = root; il!=null; il=il.getNext()) {
      // handle fall through.
      if (il.canFallThrough) {
	if (il.getNext() != null)
	  addEdge((Node)instr2node.get(il),
		  (Node)instr2node.get(il.getNext()));
      }
      // handle jumps.
      for (Iterator it = il.getTargets().iterator(); it.hasNext(); ) {
	Label ll = (Label) it.next();
	if (label2node.get(ll)==null)
	    throw new Error("Non-existent label used in instruction list: "+
			    ll.toString()+" used in "+il.toString());
	addEdge((Node)instr2node.get(il),
		(Node)label2node.get(ll));
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
    // CSA'99: clunk.  def() returns an array in FLEX.  Thunk it to a TempList.
    Temp[] d = ud.def(instr(n));
    TempList tl = null;
    for (int i=d.length-1; i>=0; i--)
      if (twoWord && d[i] instanceof TwoWordTemp) {
	harpoon.Util.Util.assert(instr(n).getAssem().indexOf("`d"+i)!=-1);
	if (instr(n).getAssem().indexOf("`d"+i+"l")!=-1)
	  tl = new TempList(((TwoWordTemp)d[i]).getLow(), tl);
	if (instr(n).getAssem().indexOf("`d"+i+"h")!=-1)
	  tl = new TempList(((TwoWordTemp)d[i]).getHigh(), tl);
      } else tl = new TempList(d[i], tl);
    return tl;
  }
  /**
   * @param n a node in the flow graph.
   * @return a list of temps used by the instruction at node 'n'
   * @see Temp.Temp
   */
  public TempList use(Node n) {
    // CSA'99: clunk.  use() returns an array in FLEX.  Thunk it to a TempList.
    Temp[] u = ud.use(instr(n));
    TempList tl = null;
    for (int i=u.length-1; i>=0; i--)
      if (twoWord && u[i] instanceof TwoWordTemp) {
	harpoon.Util.Util.assert(instr(n).getAssem().indexOf("`s"+i)!=-1);
	if (instr(n).getAssem().indexOf("`s"+i+"l")!=-1)
	  tl = new TempList(((TwoWordTemp)u[i]).getLow(), tl);
	if (instr(n).getAssem().indexOf("`s"+i+"h")!=-1)
	  tl = new TempList(((TwoWordTemp)u[i]).getHigh(), tl);
      } else tl = new TempList(u[i], tl);
    return tl;
  }
  /**
   * @param n a node in the flow graph.
   * @return true if node n can be eliminated if use() == def(), false otherwise.
   */
  public boolean isMove(Node n) {
    return instr(n) instanceof InstrMOVE;
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
class AssemNode extends harpoon.Backend.CSAHack.Graph.Node
  //  implements harpoon.Temp.TempMap
{
  Instr instr;
  AssemNode(harpoon.Backend.CSAHack.Graph.Graph g, Instr i) {
    super(g);
    instr = i;
  }
  public String toString() {
    if (instr!=null) {
      String r = "Instr["+super.toString()+"]";
      //r+="("+instr.format(this).trim()+") ";
      r+="("+instr.toString().trim()+") ";
      /*
      r+="- use: ";
      Temp[] u = instr.use();
      for (int i=0; i<u.length; i++)
	r+=u[i].toString()+" ";
      r+=", def: ";
      Temp[] d = instr.def();
      for (int i=0; i<d.length; i++)
	r+=d[i].toString()+" ";
      */
      return r;
    } else return super.toString();
  }
  public String tempMap(harpoon.Temp.Temp t) {
    return t.toString();
  }
  public int hashCode() { return instr.hashCode(); }
}
  
