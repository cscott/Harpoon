package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InSwitch</code> is used for the <code>tableswitch</code> and
 * <code>lookupswitch</code> java bytecode instructions.
 * It is a control-transfer instruction.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InSwitch.java,v 1.1 1998-08-04 04:09:01 cananian Exp $
 */
public class InSwitch extends InCti {
  int keys[];

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
  public int key(int index) {
    if (index<0) throw new Error("Invalid key lookup index.");
    if (index==0) throw new Error("No key for default target.");
    return keys[index];
  }
  public int key(Instr target) {
    Instr[] targets = next();
    for (int i=0; i<targets.length; i++)
      if (target==targets[i])
	return key(i);
    throw new Error("Invalid key lookup instruction.");
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
  int u1(byte[] code, int pc) { return ((int)code[pc])&0xFF; }
}
