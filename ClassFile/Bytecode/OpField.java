package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpField</code> represents a field reference operand of a
 * java bytecode instruction.  It is generated from a
 * <code>CONSTANT_Fieldref</code> constant pool entry.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpField.java,v 1.3 1998-08-05 00:52:25 cananian Exp $
 * @see harpoon.ClassFile.Raw.Constant.ConstantFieldref
 */
public class OpField extends Operand {
  HField hfield;
  /** Create an <code>OpField</code> from the <code>CONSTANT_Fieldref</code>
   *  at the given index in the constant pool. */
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
  /** Return the field referenced by this operand. */
  public HField value() { return hfield; }
  /** Return the canonical name of this field.
   *  @see harpoon.ClassFile.HField#toString */
  public String toString() { return hfield.toString(); }
}
