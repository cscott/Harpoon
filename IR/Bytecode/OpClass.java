// OpClass.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpClass</code> represents a class reference operand of a
 * java bytecode instruction.  It is generated from a
 * <code>CONSTANT_Class</code> constant pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpClass.java,v 1.2 1998-10-11 03:01:16 cananian Exp $
 * @see harpoon.ClassFile.Raw.Constant.ConstantClass
 */
public class OpClass extends Operand {
  HClass hclass;
  /** Creates an <code>OpClass</code> from the <code>CONSTANT_CLASS</code>
   *  at the given index in the constant pool.
   */
  public OpClass(Code parent, int constant_pool_index) {
    Constant c = parent.getConstant(constant_pool_index);
    if (!(c instanceof ConstantClass))
      throw new Error("OpClass not given CONSTANT_Class");
    String classname = ((ConstantClass) c).name();
    if (classname.charAt(0) != '[') // not a real descriptor yet.
	classname = "L" + classname + ";";
    hclass = HClass.forDescriptor(classname);
  }
  /** Return the class referenced. */
  public HClass value() { return hclass; }
  /** Return a human-readable string given the value of this object. */
  public String toString() { return hclass.toString(); }
}
