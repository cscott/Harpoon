package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpMethod</code> represents a method reference operand of a
 * java bytecode instruction.  It is generated from a
 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref constant_pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpMethod.java,v 1.1 1998-08-04 01:56:56 cananian Exp $
 */
public class OpMethod extends Operand {
  boolean isInterfaceMethod;
  HMethod hmethod;
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

    HClass cls = HClass.forDescriptor("L"+cc.name()+";");
    this.hmethod = cls.getMethod(cnt.name(), cnt.descriptor());
  }
  /** Get the method reference */
  public HMethod value() { return hmethod; }
  /** Make human-readable */
  public String toString() { return hmethod.toString(); }
}
