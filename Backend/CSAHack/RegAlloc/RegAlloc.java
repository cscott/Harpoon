/*
 * RegAlloc/RegAlloc.java
 *  register allocator
 *
 * ALGORITHM: iterate coloring and spilling until no more
 * temporaries are left to be spilled.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.Generic.Frame;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMOVE;

import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Temp.TempMap;

import harpoon.Backend.CSAHack.FlowGraph.AssemFlowGraph;
import harpoon.Util.Util;

/**
 * Register allocation module.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 */
public class RegAlloc implements TempMap {
  Frame frame;
  Code  code;
  Color color;
	
  /**
   * A list of instructions that have been successfully allocated to
   * registers (using the mapping defined by tempMap()).
   * Instructions to spill variables may be added during the 
   * allocation process.
   * Unnecessary moves may similarly be deleted.
   */
  public Instr instrs; // this is the head of the list.

  boolean debug = false; // enable debugging output.


  /**
   * Allocate the temporaries defined and used in an Assem.InstrList to
   * machine registers defined in a Frame.
   * The resulting InstrList is placed in the instrs field of this class.
   * @param f a machine-specific frame and register description
   * @param il a list of instructions to be register allocated.
   */
  public RegAlloc(final Frame f, Code c, Instr root) {
    AssemFlowGraph flow;

    frame = f;
    code = c;
    instrs = root;

    // repeat these steps until no more variables need to be spilled:
    do {
      // create a flow graph
      flow = new AssemFlowGraph(this.instrs, true);
      // create an interference graph after analyzing the flow for liveness.
      Liveness live = new Liveness(flow);
      // possibly dump these graphs for debugging.
      if (debug) {
	flow.show(System.err);
        live.show(System.err);
      }
      // thunk the register array to a register list...
      TempList registers=null;
      Temp[] reg = f.getRegFileInfo().getAllRegisters();
      for (int i=reg.length-1; i>=0; i--)
	  registers = new TempList(reg[i], registers);
      // color the temporaries using the interference graph.
      color = new Color(flow, live, new TempMap() {
	  public Temp tempMap(Temp t) {
	      Util.assert(f.getRegFileInfo().isRegister(t));
	      return t;
	  }
      }, registers);
      // rewrite program to spill temporaries, if necessary.
      if (color.spills() != null)
	rewriteProgram(root, color.spills());
    } while (color.spills() != null);

    // trim redundant instructions.
    instrs = trim(instrs);
  }
  
  // use the Spiller class to rewrite the program.
  private void rewriteProgram(Instr root, TempList spilled) {
    //throw new Error(root.getFactory().getMethod()+" needs "+color.spills()+" spilled.");
    Spiller spiller = new Spiller(code, spilled);
    instrs = spiller.rewrite(instrs);
  }

  // trim coalesced moves from the instruction list.
  private Instr trim(Instr root) {
    for (Instr il = root; il != null; ) {
      if ((il instanceof InstrMOVE) &&
	  canBeCoalesced((InstrMOVE)il)) {
	// delete coalesced move. (src=dst)
	if (il == root) root = root.getNext();
	il = il.getNext();
	il.getPrev().remove();
      } else il = il.getNext();
    }
    return root;
  } 

  // check two moves for equality.
  boolean canBeCoalesced(InstrMOVE instr) {
    Temp[] u = (Temp[]) instr.use().clone();
    for (int i=0; i<u.length; i++)
      u[i] = tempMap(u[i]);
    Temp[] d = (Temp[]) instr.def().clone();
    for (int i=0; i<d.length; i++)
      d[i] = tempMap(d[i]);
    java.util.List ul =
      java.util.Arrays.asList(u), dl = java.util.Arrays.asList(d);
    return ul.containsAll(dl) && dl.containsAll(ul);
  }

  // the tempMap is the coloring defined in RegAlloc.Color
  /**
   * A mapping of temporaries to registers.
   */
  public Temp tempMap(Temp temp) {
    return color.tempMap(temp);
  }
}
