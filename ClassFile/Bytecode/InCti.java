package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InCti</code> is used for control-transfer instructions.
 * It will have exactly one predecessor, and multiple successors.
 * For conditional branches, the first successor 
 * (<code>next()[0]</code>) will be the 'fall-through' instruction
 * (corresponding to branch-not-taken).  For unconditional branches,
 * the first successor will be the target, if there is one.
 * Instructions like <code>ireturn</code> have no successors.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InCti.java,v 1.9 1998-09-09 22:23:43 cananian Exp $
 * @see Instr
 */
public class InCti extends Instr {
  byte opcode;
  int arity;

  /** Create an <code>InCti</code> from a chunk of bytecode starting
   *  at offest <code>pc</code>. */
  public InCti(String sourcefile, int linenumber, byte[] code, int pc) {
    super(sourcefile, linenumber);
    this.opcode=code[pc];
    this.arity = Op.branchTargets(code, pc).length;
    if (!Op.isUnconditionalBranch(code[pc])) this.arity++;
    if (Op.isJSR(code[pc])) this.arity++;
  }

  // Provide run-time checks on arity.
  /** @see Instr#addPrev */
  void addPrev(Instr prev) {
    if (this.prev.size()>=1) 
      throw new Error("CTI should have only one predecessor.");
    super.addPrev(prev);
  }
  /** @see Instr#addNext */
  void addNext(Instr next) {
    if (this.next.size()>=arity) 
      throw new Error("Exceeding arity of CTI.");
    super.addNext(next);
  }

  /** Return the java bytecode opcode of this instruction. */
  public byte getOpcode() { return opcode; }

  /** Return human-readable instruction string. */
  public String toString() {
    StringBuffer sb = new StringBuffer(Op.toString(opcode));
    Instr[] targets = next();
    // skip targets[0] if this is a conditional branch or JSR.
    int start = (Op.isUnconditionalBranch(opcode)&&!Op.isJSR(opcode))?0:1;
    if (targets.length > start)
      sb.append(' ');
    for (int i=start; i<targets.length; i++) {
      sb.append('#'); sb.append(targets[i].getID());
      if (i<targets.length-1)
	sb.append(", ");
    }
    return sb.toString();
  }
}
