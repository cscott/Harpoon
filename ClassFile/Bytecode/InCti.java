package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InCti</code> is used for control-transfer instructions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InCti.java,v 1.3 1998-08-03 10:35:39 cananian Exp $
 * @see Instr
 */
public class InCti extends Instr {
  byte opcode;
  int arity;

  public InCti(String sourcefile, int linenumber, byte[] code, int pc) {
    super(sourcefile, linenumber);
    this.opcode=code[pc];
    this.arity = Op.branchTargets(pc, code).length;
    if (!Op.isUnconditionalBranch(code[pc])) this.arity++;
    // FIXME
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
}
