package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.Util.Util;

/**
 * <code>InGen</code> is used for non-branching instructions.
 */
public class InGen extends Instr {
  byte opcode;
  public InGen(String sourcefile, int linenumber, byte[] code, int pc) {
    super(sourcefile, linenumber);
    this.opcode = code[pc];
  }

  /*
  HClass  resultType;
  byte opcode;
  Operand operands[];
  HClass  operandTypes[];

  // Full constructor.
  public InGen(String sourcefile, int linenumber, HClass resultType,
	       byte opcode, Operand operands[], HClass operandTypes[]) {
    super(sourcefile, linenumber);
    this.resultType = resultType;
    this.opcode = opcode;
    this.operands = (Operand[]) Util.copy(operands);
    this.operandTypes = (HClass[]) Util.copy(operandTypes);
  }

  public byte getOpcode() { return opcode; }
  public Operand getOperand(int i) { return operands[i]; }
  public Operand[] getOperands() { return (Operand[]) Util.copy(operands); }
  public HClass  getOperandType(int i) { return operandTypes[i]; }
  public HClass[] getOperandTypes() { 
    return (HClass[]) Util.copy(operandTypes); 
  }
  */

  // provide run-time checks on arity.
  /** @see Instr#addPrev */
  void addPrev(Instr prev) {
    if (this.prev.size()>=1)
      throw new Error("Only one predecessor for InGen allowed.");
    super.addPrev(prev);
  }
  /** @see Instr#addNext */
  void addNext(Instr next) {
    if (this.next.size()>=1)
      throw new Error("Only one successor for InGen allowed.");
    super.addNext(next);
  }

  /** Return human-readable instruction string. */
  public String toString() {
    StringBuffer sb = new StringBuffer(Op.toString(opcode));
    /*
    for (int i=0; i<operands.length; i++) {
      sb.append(" (");
      sb.append(operandTypes[i].getName());
      sb.append(")");
      sb.append(operands[i].toString());
      if (i<operands.length-1)
	sb.append(", ");
    }
    */
    return sb.toString();
  }
}

