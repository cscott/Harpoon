/*
 * RegAlloc/Spiller.java
 *  rewrite instruction list to spill overflow temporaries.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeEdge;
import harpoon.Backend.CSAHack.RegAlloc.Code.Access;
import harpoon.IR.Assem.Instr;
import harpoon.Backend.CSAHack.Graph.NodeList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Rewrite instruction list to spill overflowing temporaries to the
 * local frame.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see RegAlloc.RegAlloc
 */
class Spiller {
  Code code;
  Map  spills;

  Spiller(Code c, TempList spilled) {
    this.code = c;
    // make an array of temporaries that need to be spilled
    // and allocate each of them some space in the frame.
    spills = new HashMap();
    for (int i=0; spilled!=null; spilled=spilled.tail, i++)
      spills.put(spilled.head, c.allocLocal());
  }
  // rewrite an instruction list.
  Instr rewrite(Instr instrs) { 
    Set spillinstrs = new HashSet();
    for (Instr il=instrs; il!=null; il=il.getNext()) {
      if (spillinstrs.contains(il)) continue; // skip over spill code
      // see if there is a DEF of any spilltemps:
      for (Iterator it=il.defC().iterator(); it.hasNext(); ) {
	Temp def = (Temp) it.next();
	if (spills.containsKey(def)) { /* insert a store after the def. */
	  Access a = (Access) spills.get(def);
	  for (Iterator it2=il.succC().iterator(); it2.hasNext(); ) {
	    Instr storei = a.makeStore(il.getFactory(), il, def);
	    storei.insertAt((HCodeEdge)it2.next());
	    spillinstrs.add(storei);
	  }
	}    
      }
      // see if there is a USE of any spilltemps
      for (Iterator it=il.useC().iterator(); it.hasNext(); ) {
	Temp use = (Temp) it.next();
	if (spills.containsKey(use)) { /* insert a fetch before the use */
	  Access a = (Access) spills.get(use);
	  for (Iterator it2=il.predC().iterator(); it2.hasNext(); ) {
	    Instr loadi = a.makeLoad(il.getFactory(), il, use);
	    loadi.insertAt((HCodeEdge)it2.next());
	    spillinstrs.add(loadi);
	  }
	}
      }
    }
    // fixup instrs
    while (instrs.getPrev()!=null) instrs = instrs.getPrev();
    return instrs;
  }
}
