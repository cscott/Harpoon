package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpClass</code> represents a class reference operand of a
 * java bytecode instruction.  It is generated from a
 * CONSTANT_Class constant_pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpClass.java,v 1.2 1998-08-04 04:31:16 cananian Exp $
 */
public class OpClass extends Operand {
  HClass hclass;
  public OpClass(Code parent, int constant_pool_index) {
    Constant c = parent.getConstant(constant_pool_index);
    if (!(c instanceof ConstantClass))
      throw new Error("OpClass not given CONSTANT_Class");
    String classname = ((ConstantClass) c).name();
    hclass = HClass.forName(classname.replace('/','.'));
  }
  /** Get the class reference. */
  public HClass value() { return hclass; }
  /** Make human-readable. */
  public String toString() { return hclass.toString(); }
}
