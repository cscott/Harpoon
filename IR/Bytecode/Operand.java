package harpoon.IR.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>Operand</code> represents the operands of a java bytecode
 * instruction.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Operand.java,v 1.1 1998-09-14 02:49:23 cananian Exp $
 * @see Instr
 * @see InGen
 * @see OpConstant
 * @see OpLocalVariable
 * @see OpClass
 * @see OpMethod
 * @see OpField
 */
public abstract class Operand {
  /** Return a human-readable representation of this Operand. */
  public abstract String toString(); // force a reimplementation.
}
