package harpoon.ClassFile.Raw;

/** 
 * The <code>CONSTANT_Class_info</code> structure is used to
 * represent a class or an interface.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantClass.java,v 1.6 1998-07-31 06:21:55 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.1"
 * @see Constant
 */
class ConstantClass extends Constant {
  /** The value of the <code>name_index</code> item must be a valid
      index into the <code>constant_pool</code> table of
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_Utf8_info</code> structure
      representing a valid fully qualified Java class name that has
      been converted to the <code>class</code> file's internal form. */
  int name_index;

  /** Constructor. */
  ConstantClass(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException 
  {
    super(parent);
    name_index = in.read_u2();
  }
  /** Constructor. */
  public ConstantClass(ClassFile parent, int name_index) { 
    super(parent);
    this.name_index = name_index;
  }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Class);
    out.write_u2(name_index);
  }

  // convenience.
  ConstantUtf8 name_index()
  { return (ConstantUtf8) parent.constant_pool[name_index]; }

  String name() { return name_index().val; }
}
