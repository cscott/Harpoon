package harpoon.ClassFile.Raw;

/**
 * The <code>CONSTANT_Fieldref</code> structure represents a field.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantFieldref.java,v 1.5 1998-07-31 05:51:09 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.2"
 * @see Constant
 * @see ConstantMethodref
 * @see ConstantInterfaceMethodref
 */
class ConstantFieldref extends ConstantPoolInfo {
  /** The value of the <code>class_index</code> item must be a valid
      index into the <code>constant_pool</code> table of the
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_Class_info</code> structure
      representing the class or interface type that contains the
      declaration of the field.
      <p>
      The <code>class_index</code> item of a
      <code>CONSTANT_Fieldref</code> must be a class type, not an
      interface type. */
  int class_index;
  /** The value of the <code>name_and_type_index</code> item must be a
      valid index into the <code>constant_pool</code> table of the
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_NameAndType_info</code>
      structure.  This <code>constant_pool</code> entry indicates the
      name and descriptor of the field. */
  int name_and_type_index;

  /** Constructor. */
  ConstantFieldref(ClassDataInputStream in) throws java.io.IOException {
    class_index = in.read_u2();
    name_and_type_index = in.read_u2();
  }
  /** Constructor. */
  public ConstantFieldref(int class_index, int name_and_type_index) {
    this.class_index = class_index;
    this.name_and_type_index = name_and_type_index;
  }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Fieldref);
    out.write_u2(class_index);
    out.write_u2(name_and_type_index);
  }

  // convenience.
  ConstantClass class_index()
  { return (ConstantClass) parent.constant_pool[class_index]; }
  ConstantNameAndType name_and_type_index()
  { return (ConstantNameAndType) parent.constant_pool[name_and_type_index]; }
}
