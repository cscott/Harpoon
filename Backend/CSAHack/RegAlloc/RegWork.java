/*
 * RegAlloc/RegWork.java
 *  collections of sets used by the register allocator.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package RegAlloc;

import Graph.Node;
import Graph.NodeList;

import java.util.Hashtable;
import java.util.Stack;

class Worklist {
  Set simplify  = new Set("simplifyWorklist");
  Set freeze	= new Set("freezeWorklist");
  Set spill	= new Set("spillWorklist");
}

class NodeSet {
  Set precolored = new Set("precolored");
  Set initial    = new Set("initial");
  Worklist worklist = new Worklist();
  Set spilled	 = new Set("spilled");
  Set coalesced	 = new Set("coalesced");
  Set colored	 = new Set("colored");
  Stack selectStack= new Stack();

  int correctsize;

  NodeSet(Temp.TempList registers, InterferenceGraph ig) {
//    precolored.live = initial.live = worklist.simplify.live =
//      worklist.freeze.live = worklist.spill.live = spilled.live = 
//	coalesced.live = colored.live = ig;
    // initialize sets. (only precolored and initial are non-empty)
    for (Graph.NodeList nl = ig.nodes(); nl!=null; nl=nl.tail)
      initial.add(nl.head);
    for ( ; registers!= null; registers=registers.tail) {
      Node n = ig.tnode(registers.head);
      if (n!=null) {
	precolored.add(n);
	initial.remove(n);
      }
    }
    correctsize = size();
  }

  int size() {
    return precolored.size() + initial.size() + spilled.size() + coalesced.size() + colored.size() + selectStack.size() + worklist.simplify.size() + worklist.freeze.size() + worklist.spill.size();
  }
  void check() {
    if (size() != correctsize) throw new Error("Size invariance violated");
  }
}

class Moves { 
  Set coalesced		= new Set("coalescedMoves");
  Set constrained	= new Set("constrainedMoves");
  Set frozen		= new Set("frozenMoves");
  Set worklist		= new Set("worklistMoves");
  Set active		= new Set("activeMoves");

  Moves(FlowGraph.FlowGraph fg) {
    for (NodeList nl = fg.nodes(); nl!=null; nl=nl.tail)
      if (fg.isMove(nl.head))
	worklist.add(nl.head);
  }
}
