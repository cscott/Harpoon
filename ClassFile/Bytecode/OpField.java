package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpField</code> represents a field reference operand of a
 * java bytecode instruction.  It is generated from a
 * CONSTANT_Fieldref constant_pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpField.java,v 1.2 1998-08-04 04:31:16 cananian Exp $
 */
public class OpField extends Operand {
  HField hfield;
  public OpField(Code parent, int constant_pool_index) {
    Constant c = parent.getConstant(constant_pool_index);
    if (!(c instanceof ConstantFieldref))
      throw new Error("OpField not given CONSTANT_Fieldref");
    ConstantFieldref cf = (ConstantFieldref) c;

    HClass cls = HClass.forName(cf.class_index().name().replace('/','.'));
    hfield = cls.getField(cf.name_and_type_index().name());
    if (!hfield.getDescriptor().equals(cf.name_and_type_index().descriptor()))
      throw new Error("Field does not resolve to proper type.");
  }
  public HField value() { return hfield; }
  public String toString() { return hfield.toString(); }
}
