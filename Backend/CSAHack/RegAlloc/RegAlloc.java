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

import harpoon.Analysis.Maps.Derivation;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;

import harpoon.Temp.Temp;
import harpoon.Temp.TempList;
import harpoon.Temp.TempMap;

import harpoon.Backend.CSAHack.FlowGraph.AssemFlowGraph;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.Environment;
import harpoon.Util.Collections.HashEnvironment;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
  Instr instrs; // this is the head of the list.
    /** A derivation generator, or null. */
  DerivationGenerator dg;

  boolean debug = false; // enable debugging output.
  static boolean quiet = false; // don't show progress


  /**
   * Allocate the temporaries defined and used in an Assem.InstrList to
   * machine registers defined in a Frame.
   * The resulting InstrList is placed in the instrs field of this class.
   * @param f a machine-specific frame and register description
   * @param il a list of instructions to be register allocated.
   */
  public RegAlloc(final Frame f, Code c, Instr root, DerivationGenerator dg) {
    AssemFlowGraph flow;
    UseDefer ud0 = UseDefer.DEFAULT;

    frame = f;
    code = c;
    instrs = root;
    this.dg = dg;

    UseDefer ud = (dg==null) ? ud0 : new DerivedUseDefer(c, ud0);

    // thunk the register array to a register list...
    TempList registers=null;
    Temp[] reg = f.getRegFileInfo().getAllRegisters();
    for (int i=reg.length-1; i>=0; i--)
	registers = new TempList(reg[i], registers);
    
    // repeat these steps until no more variables need to be spilled:
    if (!quiet) System.err.print("{");
    do {
      // create a flow graph
      if (!quiet) System.err.print("F");
      flow = new AssemFlowGraph(this.instrs, ud, true);
      // create an interference graph after analyzing the flow for liveness.
      if (!quiet) System.err.print("L");
      Liveness live = new Liveness(flow);
      // possibly dump these graphs for debugging.
      if (debug) {
	flow.show(System.err);
        live.show(System.err);
      }
      // color the temporaries using the interference graph.
      if (!quiet) System.err.print("C");
      color = new Color(flow, live, new TempMap() {
	  public Temp tempMap(Temp t) {
	      Util.ASSERT(f.getRegFileInfo().isRegister(t));
	      return t;
	  }
      }, registers);
      if (!quiet) System.err.print("S");
      // rewrite program to spill temporaries, if necessary.
      if (color.spills() != null) {
	rewriteProgram(root, color.spills());
	// re-create derived ptr use info.
	if (dg!=null) ud = new DerivedUseDefer(c, ud0);
      }
    } while (color.spills() != null);
    if (!quiet) System.err.print("}");

    // trim redundant instructions.
    instrs = trim(instrs);
  }
  
  // use the Spiller class to rewrite the program.
  private void rewriteProgram(Instr root, TempList spilled) {
    //throw new Error(root.getFactory().getMethod()+" needs "+color.spills()+" spilled.");
    if (!quiet) {
      int i=0; for (TempList tl=spilled; tl!=null; tl=tl.tail) i++;
      System.err.print("("+i+")");
    }
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

  private static class DerivedUseDefer extends UseDefer {
    UseDefer ud;
    MultiMap extras = new GenericMultiMap();
    DerivedUseDefer(Code c, UseDefer ud) {
      this.ud = ud;
      if (c.getDerivation()==null) return; // no deriv info.

      if (!quiet) System.err.print("[COMPUTING EXTRAS...");
      // do a depth-first search of the CFG to determine (one of the)
      // reaching definitions for each use.
      CFGrapher cfg = CFGrapher.DEFAULT;
      dfs(cfg.getFirstElement(c), cfg, c.getDerivation(),
	  new HashEnvironment(), new HashSet());
      if (!quiet) System.err.print("done.]");
    }
    /** dfs of cfg to determine (a) reaching def for each use.
     *  current reaching defs are in 'env'. */
    private void dfs(HCodeElement hce, CFGrapher cfg, Derivation deriv,
		     Environment env, Set seen) {
	seen.add(hce); // keep track of touched hces.
	// process *this*
	//   for all uses, use current reaching def to determine 'extras'
	for (Iterator it=ud.useC(hce).iterator(); it.hasNext(); ) {
	    Temp u = (Temp) it.next();
	    HCodeElement def = (HCodeElement) env.get(u);
	    if (def==null) {
		System.err.println("WARNING: no reaching def found for " + u +
				   " in " + hce);
		continue;
	    }
	    for (Derivation.DList dl = deriv.derivation(def, u);
		 dl!=null; dl=dl.next)
		extras.add(hce, dl.base);
	}
	//   for all defs, update reaching def environment.
	for (Iterator it=ud.defC(hce).iterator(); it.hasNext(); ) {
	    Temp d = (Temp) it.next();
	    env.put(d, hce);
	}
	// recurse
	Environment.Mark mark = env.getMark();
	for (Iterator it=cfg.succC(hce).iterator(); it.hasNext(); ) {
	    HCodeElement succ = ((HCodeEdge) it.next()).to();
	    if (seen.contains(succ)) continue;
	    dfs(succ, cfg, deriv, env, seen);
	    env.undoToMark(mark);
	}
    }

    public Collection defC(HCodeElement hce) { return ud.defC(hce); }
    // add base pointers for derived temps to use set.
    public Collection useC(HCodeElement hce) {
      Collection c = ud.useC(hce);
      if (extras.containsKey(hce)) {
	  c = new ArrayList(c);
	  c.addAll(extras.getValues(hce));
      }
      return c;
    }
  }
}
