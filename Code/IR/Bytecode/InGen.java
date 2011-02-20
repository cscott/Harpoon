// InGen.java, created Sun Sep 13 22:49:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

/**
 * <code>InGen</code> is used for non-branching instructions.
 * An <code>InGen</code> has an opcode and optional operands.
 * An <code>InGen</code> has exactly one predecessor and exactly
 * one successor.<p>
 * Note that <code>InGen</code> contain <code>Operand</code> objects
 * for all <b>relevant</b>, explicit or implicit operands of the
 * bytecode instruction.  This means that, for example, an 
 * <code>iload_0</code> will be given an <Code>OpLocalVariable</code>
 * operand corresponding to the implicit '0', and conversely that
 * <code>invokeinterface</code> will <b>not</b> contain operands for
 * the <code>nargs</code> operand (which is obvious from the method
 * signature) nor for the dummy placeholder value which trails the
 * opcode in the raw bytecode array.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InGen.java,v 1.4 2002-02-25 21:04:17 cananian Exp $
 */
public class InGen extends Instr {
  final byte opcode;
  final Operand operands[];

  /** Create an <code>InGen</code> from a chunk of bytecode starting at
   *  offset <code>pc</code>. <code>parent</code> is used to lookup
   *  <code>constant_pool</code> references. */
  public InGen(String sourcefile, int linenumber, 
	       byte[] code, int pc, Code parent) {
    super(sourcefile, linenumber);
    this.opcode = (code[pc]==Op.WIDE)?code[pc+1]:code[pc];
    // Make operands, if necessary.
    switch(code[pc]) {
      // Constant local variable index 0:
    case Op.ALOAD_0:
    case Op.ASTORE_0:
    case Op.DLOAD_0:
    case Op.DSTORE_0:
    case Op.FLOAD_0:
    case Op.FSTORE_0:
    case Op.ILOAD_0:
    case Op.ISTORE_0:
    case Op.LLOAD_0:
    case Op.LSTORE_0:
      operands = new Operand[] { new OpLocalVariable(0) };
      break;
      // Constant local variable index 1:
    case Op.ALOAD_1:
    case Op.ASTORE_1:
    case Op.DLOAD_1:
    case Op.DSTORE_1:
    case Op.FLOAD_1:
    case Op.FSTORE_1:
    case Op.ILOAD_1:
    case Op.ISTORE_1:
    case Op.LLOAD_1:
    case Op.LSTORE_1:
      operands = new Operand[] { new OpLocalVariable(1) };
      break;
      // Constant local variable index 2:
    case Op.ALOAD_2:
    case Op.ASTORE_2:
    case Op.DLOAD_2:
    case Op.DSTORE_2:
    case Op.FLOAD_2:
    case Op.FSTORE_2:
    case Op.ILOAD_2:
    case Op.ISTORE_2:
    case Op.LLOAD_2:
    case Op.LSTORE_2:
      operands = new Operand[] { new OpLocalVariable(2) };
      break;
      // Constant local variable index 3:
    case Op.ALOAD_3:
    case Op.ASTORE_3:
    case Op.DLOAD_3:
    case Op.DSTORE_3:
    case Op.FLOAD_3:
    case Op.FSTORE_3:
    case Op.ILOAD_3:
    case Op.ISTORE_3:
    case Op.LLOAD_3:
    case Op.LSTORE_3:
      operands = new Operand[] { new OpLocalVariable(3) };
      break;
      // Local variable index
    case Op.ALOAD:
    case Op.ASTORE:
    case Op.DLOAD:
    case Op.DSTORE:
    case Op.FLOAD:
    case Op.FSTORE:
    case Op.ILOAD:
    case Op.ISTORE:
    case Op.LLOAD:
    case Op.LSTORE:
      operands = new Operand[] { new OpLocalVariable(u1(code,pc+1)) };
      break;
      // Class
    case Op.ANEWARRAY:
    case Op.CHECKCAST:
    case Op.INSTANCEOF:
    case Op.NEW:
      operands = new Operand[] { new OpClass(parent, u2(code,pc+1)) };
      break;
      // Field
    case Op.GETFIELD:
    case Op.GETSTATIC:
    case Op.PUTFIELD:
    case Op.PUTSTATIC:
      operands = new Operand[] { new OpField(parent, u2(code,pc+1)) };
      break;
      // Method
    case Op.INVOKESPECIAL:
    case Op.INVOKESTATIC:
    case Op.INVOKEVIRTUAL:
    case Op.INVOKEINTERFACE: // note that we discard an unnecessary operand.
      operands = new Operand[] { new OpMethod(parent, u2(code,pc+1)) };
      break;
      // Byte
    case Op.BIPUSH:
    case Op.NEWARRAY:
      operands = new Operand[] { 
	new OpConstant(new Byte(code[pc+1]), HClass.Byte)
      };
      break;
      // Short
    case Op.SIPUSH:
      operands = new Operand[] { 
	new OpConstant(new Short((short)u2(code,pc+1)), HClass.Short)
      };
      break;
      // Integer constant.
    case Op.ICONST_M1:
    case Op.ICONST_0:
    case Op.ICONST_1:
    case Op.ICONST_2:
    case Op.ICONST_3:
    case Op.ICONST_4:
    case Op.ICONST_5:
      operands = new Operand[] {
	new OpConstant(new Integer((int)(code[pc]-Op.ICONST_0)), HClass.Int)
      };
      break;
      // Long constant.
    case Op.LCONST_0:
    case Op.LCONST_1:
      operands = new Operand[] {
	new OpConstant(new Long((long)(code[pc]-Op.LCONST_0)), HClass.Long)
      };
      break;
      // Floating-point constant.
    case Op.FCONST_0:
    case Op.FCONST_1:
    case Op.FCONST_2:
      operands = new Operand[] {
	new OpConstant(new Float((float)(code[pc]-Op.FCONST_0)), HClass.Float)
      };
      break;
      // Double constant.
    case Op.DCONST_0:
    case Op.DCONST_1:
      operands = new Operand[] {
	new OpConstant(new Double((double)(code[pc]-Op.DCONST_0)),
		       HClass.Double)
      };
      break;
      // General constant.
    case Op.LDC:
      operands = new Operand[] { new OpConstant(parent, u1(code,pc+1)) };
      break;
    case Op.LDC_W:
    case Op.LDC2_W:
      operands = new Operand[] { new OpConstant(parent, u2(code,pc+1)) };
      break;
      // Odd-balls
    case Op.IINC:
      operands = new Operand[] {
	new OpLocalVariable(u1(code,pc+1)),
	new OpConstant(new Integer((int)code[pc+2]), HClass.Int)
      };
      break;
    case Op.MULTIANEWARRAY:
      operands = new Operand[] {
	new OpClass(parent, u2(code,pc+1)),
	new OpConstant(new Integer(u1(code,pc+3)), HClass.Int)
      };
      break;
    case Op.RET:
      operands = new Operand[] { 
	new OpConstant(new Integer(u1(code,pc+1)), HClass.Int)
      };
      break;
    case Op.WIDE: // this is evil.
      if (code[pc+1]==Op.IINC) // very evil.
	operands = new Operand[] { 
	  new OpLocalVariable(u2(code, pc+2)),
	  new OpConstant(new Integer(s2(code,pc+4)), HClass.Int) 
	};
      else
	  operands = new Operand[] { new OpLocalVariable(u2(code, pc+2)) };
      break;
      // Takes an operand, but don't belong:
    case Op.GOTO:
    case Op.GOTO_W:
    case Op.IF_ACMPEQ:
    case Op.IF_ACMPNE:
    case Op.IF_ICMPEQ:
    case Op.IF_ICMPNE:
    case Op.IF_ICMPLT:
    case Op.IF_ICMPGE:
    case Op.IF_ICMPGT:
    case Op.IF_ICMPLE:
    case Op.IFEQ:
    case Op.IFNE:
    case Op.IFLT:
    case Op.IFGE:
    case Op.IFGT:
    case Op.IFLE:
    case Op.IFNONNULL:
    case Op.IFNULL:
    case Op.JSR:
    case Op.JSR_W:
    case Op.LOOKUPSWITCH:
    case Op.TABLESWITCH:
      throw new Error("Branch operations are not InGen's.");
    default:
      operands = new Operand[0];
      break;
    }
    // done.  yay.
  }
  /** Create an integer from an unsigned byte. */
  static int u1(byte[] code, int pc) {
    return ((int) code[pc]) & 0xFF;
  }
  /** Create an integer from an unsigned two-byte quantity (big-endian). */
  static int u2(byte[] code, int pc) {
    return (u1(code,pc) << 8) | u1(code,pc+1);
  }
  /** Create an integer from a signed two-byte quantity (big-endian). */
  static int s2(byte[] code, int pc) {
    int s = u2(code, pc);
    return (s&0x8000)==0 ? s : (s | ~0xFFFF);
  }

  /** Return the bytecode opcode of this instruction.
   *  @see Op */
  public byte getOpcode() { return opcode; }
  /** Return a specific operand of this instruction. */
  public Operand getOperand(int i) { return operands[i]; }
  /** Get all the operands of this instruction. */
  public Operand[] getOperands() { 
    return (Operand[]) Util.safeCopy(Operand.arrayFactory, operands);
  }

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
    sb.append(' ');
    for (int i=0; i<operands.length; i++) {
      sb.append(operands[i].toString());
      if (i<operands.length-1)
	sb.append(", ");
    }
    return sb.toString();
  }

}
