package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InCti</code> is used for control-transfer instructions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InCti.java,v 1.7 1998-08-04 04:09:00 cananian Exp $
 * @see Instr
 */
public class InCti extends Instr {
  byte opcode;
  int arity;

  public InCti(String sourcefile, int linenumber, byte[] code, int pc) {
    super(sourcefile, linenumber);
    this.opcode=code[pc];
    this.arity = Op.branchTargets(code, pc).length;
    if (!Op.isUnconditionalBranch(code[pc])) this.arity++;
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

  /** Return human-readable instruction string. */
  public String toString() {
    StringBuffer sb = new StringBuffer(Op.toString(opcode));
    Instr[] targets = next();
    // skip targets[0] if this is a conditional branch.
    int start = (Op.isUnconditionalBranch(opcode))?0:1;
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
