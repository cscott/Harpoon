// InSwitch.java, created Sun Sep 13 22:49:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InSwitch</code> is used for the <code>tableswitch</code> and
 * <code>lookupswitch</code> java bytecode instructions.
 * It is a control-transfer instruction.  It should have exactly
 * one predecessor.  The first successor will be the default
 * target; each subsequent successor corresponds to a <code>case</code>
 * of the switch statement; the match key can be looked up using the
 * <code>key</code> method.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InSwitch.java,v 1.3 2002-02-25 21:04:17 cananian Exp $
 * @see Instr
 */
public class InSwitch extends InCti {
  final int keys[];

  /** Constructor.  Creates an <code>InSwitch</code> from a chunk of
   *  bytecode starting at the given pc.
   *  @exception Error if the opcode at <code>code[pc]</code> doesn't
   *             correspond to a <code>tableswitch</code> or
   *             <code>lookupswitch</code>.
   */
  public InSwitch(String sourcefile, int linenumber, byte[] code, int pc) {
    super(sourcefile, linenumber, code, pc);
    int pad = 3-(pc%4);
    if (code[pc]==Op.LOOKUPSWITCH) {
      int npairs = s4(code, pc+pad+5);
      if (npairs<0) throw new Error("Invalid npairs in lookupswitch.");
      keys = new int[npairs+1];
      // keys[0] is invalid; the first succ. is the default branch target.
      for (int i=0; i<npairs; i++)
	keys[i+1] = s4(code,pc+pad+9+(8*i));
    } else if (code[pc]==Op.TABLESWITCH) {
      int low = s4(code, pc+pad+5);
      int high= s4(code, pc+pad+9);
      int npairs=high-low+1;
      if (low>high) throw new Error("Invalid low/high in tableswitch.");
      keys = new int[npairs+1];
      // again, keys[0] is invalid.  No key for default target.
      for (int i=0; i<npairs; i++)
	keys[i+1]=low+i;
    } else throw new Error("InSwitch constructor given invalid opcode.");
  }
  /** 
   * Returns the case key corresponding to the given branch target index.<p>
   * <code>InSwitch.key(n)</code> corresponds to 
   * <code>InSwitch.next()[n]</code> for 
   * <code>0<n<InSwitch.next().length</code>.
   * @exception Error if the default target or
   *                  an instruction not corresponding to a target is given.
   */
  public int key(int index) {
    if (index<0) throw new Error("Invalid key lookup index.");
    if (index==0) throw new Error("No key for default target.");
    return keys[index];
  }
  /** Return human-readable instruction string. */
  public String toString() {
    StringBuffer sb = new StringBuffer(Op.toString(opcode));
    Instr[] targets = next();
    sb.append(" { default: goto #"); sb.append(targets[0].getID());
    for (int i=1; i<targets.length; i++)
      sb.append("; case "+keys[i]+": goto #"+targets[i].getID());
    sb.append(" }");
    return sb.toString();
  }

  // utility.
  /** Make signed 4-bit int from four bytes. */
  int s4(byte[] code, int pc) {
    return 
      ( u1(code,pc)   << 24) |
      ( u1(code,pc+1) << 16) |
      ( u1(code,pc+2) <<  8) |
      ( u1(code,pc+3) <<  0);
  }
  /** Make integer from <i>unsigned</i> byte. */
  int u1(byte[] code, int pc) { return ((int)code[pc])&0xFF; }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
