/*
 * RegAlloc/RegAlloc.java
 *  register allocator
 *
 * ALGORITHM: iterate coloring and spilling until no more
 * temporaries are left to be spilled.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package RegAlloc;

import Graph.Node;
import Graph.NodeList;

import Temp.Temp;
import Temp.TempList;
import Temp.TempMap;

import FlowGraph.AssemFlowGraph;

/**
 * Register allocation module.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 */
public class RegAlloc implements Temp.TempMap {
  Frame.Frame frame;
  Color color;
	
  /**
   * A list of instructions that have been successfully allocated to
   * registers (using the mapping defined by tempMap()).
   * Instructions to spill variables may be added during the 
   * allocation process.
   * Unnecessary moves may similarly be deleted.
   */
  public Assem.InstrList instrs;

  boolean debug = false; // enable debugging output.


  /**
   * Allocate the temporaries defined and used in an Assem.InstrList to
   * machine registers defined in a Frame.Frame.
   * The resulting InstrList is placed in the instrs field of this class.
   * @param f a machine-specific frame and register description
   * @param il a list of instructions to be register allocated.
   */
  public RegAlloc(Frame.Frame f, Assem.InstrList il) {
    AssemFlowGraph flow;

    frame = f;
    instrs = il;

    // repeat these steps until no more variables need to be spilled:
    do {
      // create a flow graph
      flow = new AssemFlowGraph(this.instrs);
      // create an interference graph after analyzing the flow for liveness.
      Liveness live = new Liveness(flow);
      // possibly dump these graphs for debugging.
      if (debug) {
	flow.show(System.err);
        live.show(System.err);
      }
      // color the temporaries using the interference graph.
      color = new Color(flow, live, frame, f.registers());
      // rewrite program to spill temporaries, if necessary.
      if (color.spills() != null)
	RewriteProgram(color.spills());
    } while (color.spills() != null);

    // trim redundant instructions.
    instrs = trim(instrs);
  }
  
  // use the Spiller class to rewrite the program.
  private void RewriteProgram(TempList spilled) {
    Spiller spiller = new Spiller(frame, spilled);
    instrs = spiller.rewrite(instrs);
  }

  // trim coalesced moves from the instruction list.
  private Assem.InstrList trim(Assem.InstrList i) {
    if (i==null) return null;
    if ((i.head instanceof Assem.MOVE) &&
	(tempMap(((Assem.MOVE)i.head).src).equals(
	 tempMap(((Assem.MOVE)i.head).dst))))
      // delete coalesced move. (src=dst)
      return trim(i.tail);
    // don't delete this instruction.
    return new Assem.InstrList(i.head, trim(i.tail));
  } 

  // the tempMap is the coloring defined in RegAlloc.Color
  /**
   * A mapping of temporaries to registers.
   */
  public String tempMap(Temp temp) {
    return color.tempMap(temp);
  }
}
