/*
 * RegAlloc/Spiller.java
 *  rewrite instruction list to spill overflow temporaries.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package RegAlloc;

import Frame.Frame;
import Frame.Access;
import Assem.Instr;
import Assem.InstrList;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;

/**
 * Rewrite instruction list to spill overflowing temporaries to the
 * local frame.
 * @version	1.00, 25 Nov 1996
 * @author	C. Scott Ananian
 * @see RegAlloc.RegAlloc
 */
class Spiller {
  Frame    frame;
  Temp[]   spilltemp;
  Access[] spillacc;

  Spiller(Frame f, TempList spilled) {
    this.frame = f;
    // make an array of temporaries that need to be spilled
    // and allocate each of them some space in the frame.
    spilltemp = new Temp[len(spilled)];
    spillacc  = new Access[len(spilled)];
    for (int i=0; spilled!=null; spilled=spilled.tail, i++) {
      spilltemp[i] = spilled.head;
      spillacc [i] = frame.allocLocal(true);
    }
  }
  // rewrite an instruction list.
  InstrList rewrite(InstrList il) { 
    if (il==null) return null;
    // see if there is a DEF of any spilltemps:
    for (TempList defs = il.head.def(); defs!=null; defs=defs.tail) {
      for (int i=0; i<spilltemp.length; i++)
	if (defs.head == spilltemp[i]) { /* insert a store after the def. */
	  Temp newtemp = new Temp();
	  Instr newinstr = rewriteInstr(il.head, false, 
					spilltemp[i], newtemp);
	  Tree.Stm s1 = new Tree.MOVE(
			  spillacc[i].exp(new Tree.TEMP(frame.FP())),
			  new Tree.TEMP(newtemp));
	  return rewrite(append(new InstrList(newinstr,
					      frame.codegen(s1)), il.tail));
	}	      
    } 
    // see if there is a USE of any spilltemps
    for (TempList uses = il.head.use(); uses!=null; uses=uses.tail) {
      for (int i=0; i<spilltemp.length; i++)
	if (uses.head == spilltemp[i]) { /* insert a fetch before the use */
	  Temp newtemp = new Temp();
	  Tree.Stm s1 = new Tree.MOVE(
			  new Tree.TEMP(newtemp),
			  spillacc[i].exp(new Tree.TEMP(frame.FP())));
	  Instr newinstr = rewriteInstr(il.head, true,
					spilltemp[i], newtemp);
	  return rewrite(append(frame.codegen(s1),
				new Assem.InstrList(newinstr, il.tail)));
	}
    }
    // neither a use or a def; leave it alone and go on to the next instruction
    return new InstrList(il.head, rewrite(il.tail));
  }


  // Rewrite an Assem.Instr, changing the temps.
  static Instr rewriteInstr(Instr instr, boolean isuse,
			    Temp oldtemp, Temp newtemp) {
    if (instr instanceof Assem.OPER)
      return rewriteInstr((Assem.OPER)instr, isuse, oldtemp, newtemp);
    else if (instr instanceof Assem.MOVE)
      return rewriteInstr((Assem.MOVE)instr, isuse, oldtemp, newtemp);
    else throw new Error("RegAlloc.Spiller.rewriteInstr()");
  }
  static Assem.Instr rewriteInstr(Assem.OPER instr, boolean isuse,
			   Temp oldtemp, Temp newtemp) {
    if (instr.jump==null) {
      if (isuse)
	return new Assem.OPER(instr.assem, 
			      instr.dst, 
			      replace(instr.src, oldtemp, newtemp));
      else
	return new Assem.OPER(instr.assem,
			      replace(instr.dst, oldtemp, newtemp),
			      instr.src);
    } else { /* OPER includes a jump target */
      if (isuse) 
	return new Assem.OPER(instr.assem, 
			      instr.dst, 
			      replace(instr.src, oldtemp, newtemp),
			      instr.jump.labels);
      else
	return new Assem.OPER(instr.assem,
			      replace(instr.dst, oldtemp, newtemp),
			      instr.src, 
			      instr.jump.labels);
    }
  }
  static Assem.Instr rewriteInstr(Assem.MOVE instr, boolean isuse,
				  Temp oldtemp, Temp newtemp) {
    if (isuse)
      return new Assem.MOVE(instr.assem, instr.dst, newtemp);
    else
      return new Assem.MOVE(instr.assem, newtemp, instr.src);
  }

  //////// UTILITY FUNCTIONS /////////////////

  // replace Temp 'oldtemp' with the Temp 'newtemp' in TempList 'tl'
  // (returns new TempList)
  static TempList replace(TempList tl, Temp oldtemp, Temp newtemp) {
    if (tl==null) return null;
    return new TempList((tl.head==oldtemp)?newtemp:tl.head, 
			replace(tl.tail, oldtemp, newtemp));
  }
  // Append an InstrList to the end of another.
  static InstrList append(InstrList a, InstrList b) {
    if (a==null) return b;
    else {
      InstrList p;
      for (p=a; p.tail != null; p=p.tail) { }
      p.tail = b;
      return a;
    }
  }
  // Find the length of a TempList
  static int len(TempList tl) {
    if (tl==null) return 0;
    else return len(tl.tail)+1;
  }
}
