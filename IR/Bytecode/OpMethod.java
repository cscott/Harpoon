// OpMethod.java, created Sun Sep 13 22:49:23 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.IR.RawClass.Constant;
import harpoon.IR.RawClass.ConstantClass;
import harpoon.IR.RawClass.ConstantNameAndType;
import harpoon.IR.RawClass.ConstantMethodref;
import harpoon.IR.RawClass.ConstantInterfaceMethodref;

/**
 * <code>OpMethod</code> represents a method reference operand of a
 * java bytecode instruction.  It is generated from a
 * <code>CONSTANT_Methodref</code> or 
 * <code>CONSTANT_InterfaceMethodref</code> constant pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpMethod.java,v 1.4 2002-09-19 20:12:24 cananian Exp $
 * @see harpoon.IR.RawClass.ConstantMethodref
 * @see harpoon.IR.RawClass.ConstantInterfaceMethodref
 */
public final class OpMethod extends Operand {
  final boolean isInterfaceMethod;
  final HMethod hmethod;
  /** Create an <code>OpMethod</code> from the <code>CONSTANT_Methodref</code>
   *  or <code>CONSTANT_InterfaceMethodref</code> at the given index in
   *  the constant pool. */
  public OpMethod(Code parent, int constant_pool_index) {
    Constant c = parent.getConstant(constant_pool_index);
    ConstantClass cc;
    ConstantNameAndType cnt;
    if (c instanceof ConstantMethodref) {
      ConstantMethodref cm = (ConstantMethodref)c;
      cc = cm.class_index();
      cnt= cm.name_and_type_index();
      isInterfaceMethod=false;
    } else if (c instanceof ConstantInterfaceMethodref) {
      ConstantInterfaceMethodref cm = (ConstantInterfaceMethodref)c;
      cc = cm.class_index();
      cnt= cm.name_and_type_index();
      isInterfaceMethod=true;
    } else 
      throw new Error("OpMethod not given Methodref or InterfaceMethodref");

    // jikes is very sketchy: they invoke methods of arrays, so cc.name()
    // may start with a '[', in which case treat cc.name() as a descriptor
    // instead of a funny form of /-delimited class name.
    HClass cls = cc.name().charAt(0)=='[' ?
	parent.linker.forDescriptor(cc.name()) :
	parent.linker.forDescriptor("L"+cc.name()+";");
    this.hmethod = cls.getMethod(cnt.name(), cnt.descriptor());
  }
  /** Return the method referenced by this operand. */
  public HMethod value() { return hmethod; }
  /** Indicates whether this operand references an interface method. */
  public boolean isInterface() { return isInterfaceMethod; }
  /** Return the canonical name of the method referenced. */
  public String toString() { return hmethod.toString(); }
}
