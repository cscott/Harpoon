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
  public InGen prev, next;

  /** Full constructor. */
  public InGen(String sourcefile, int linenumber, HClass resultType,
	       byte opcode, Operand operands[], HClass operandTypes[]) {
    super(sourcefile, linenumber);
    this.resultType = resultType;
    this.opcode = opcode;
    this.operands = operands;
    this.operandTypes = operandTypes;
    if (this.operands==null) this.operands=new Operand[0];
    if (this.operandTypes==null) this.operandTypes=new HClass[0];
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
}

