package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>OpLocalVariable</code> represents an operand of a java bytecode
 * instruction which indicates a local variable index.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpLocalVariable.java,v 1.3 1998-08-04 02:14:45 cananian Exp $
 * @see OpConstant
 * @see Operand
 */
public class OpLocalVariable extends Operand {
  int index;
  public OpLocalVariable(int local_variable_index) {
    this.index = local_variable_index;
  }
  /** Get the local variable index */
  public int getIndex() { return index; }
  /** Return a human-readable representation of this Operand. */
  public String toString() { return "lv_"+index; }
}
