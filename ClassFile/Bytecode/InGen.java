package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.Util.Util;

/**
 * <code>InGen</code> is used for non-branching instructions.
 */
public class InGen extends Instr {
  HClass  resultType;
  byte opcode;
  Operand operands[];
  HClass  operandTypes[];

  // doubly-linked list.
  public Instr prev, next;

  /** Full constructor. */
  public InGen(String sourcefile, int linenumber, HClass resultType,
	       byte opcode, Operand operands[], HClass operandTypes[]) {
    super(sourcefile, linenumber);
    this.resultType = resultType;
    this.opcode = opcode;
    this.operands = (Operand[]) Util.copy(operands);
    this.operandTypes = (HClass[]) Util.copy(operandTypes);
  }
  /** Constructor for no-operand instructions that don't push anything
   *  onto the Stack. */
  public InGen(String sourcefile, int linenumber, byte opcode) {
    this(sourcefile, linenumber, null, opcode, 
	 new Operand[0], new HClass[0]);
  }
  /** Constructor for no-operand instructions that push the stack */
  public InGen(String sourcefile, int linenumber,
	       HClass resultType, byte opcode) {
    this(sourcefile, linenumber, resultType, opcode, 
	 new Operand[0], new HClass[0]);
  }
  /** Constructor for one-operand instructions. */
  public InGen(String sourcefile, int linenumber, HClass resultType,
	       byte opcode, Operand operand, HClass operandType) {
    this(sourcefile, linenumber, resultType, opcode, 
	 new Operand[] { operand }, new HClass[] { operandType });
  }

  public byte getOpcode() { return opcode; }
  public Operand getOperand(int i) { return operands[i]; }
  public Operand[] getOperands() { return (Operand[]) Util.copy(operands); }
  public HClass  getOperandType(int i) { return operandTypes[i]; }
  public HClass[] getOperandTypes() { 
    return (HClass[]) Util.copy(operandTypes); 
  }

  public HClass getResultType() { return resultType; }

  /** Return a list of all the <code>Instr</code>s that can precede
   *  this one. Always returns a one-element array. */
  public Instr[] prev() { return new Instr[] { prev }; }
  /** Return a list of all the possible <code>Instr</code>s that may
   *  succeed this one. Always returns a one-element array. */
  public Instr[] next() { return new Instr[] { next }; }

  /** Return human-readable instruction string. */
  public String toString() {
    StringBuffer sb = new StringBuffer(Op.toString(opcode));
    for (int i=0; i<operands.length; i++) {
      sb.append(" (");
      sb.append(operandTypes[i].getName());
      sb.append(")");
      sb.append(operands[i].toString());
      if (i<operands.length-1)
	sb.append(", ");
    }
    return sb.toString();
  }
}

