package harpoon.ClassFile.Raw;

/**
 * The <code>CONSTANT_String_info</code> structure is used to
 * represent constant objects of the type
 * <code>java.lang.String</code>.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantString.java,v 1.6 1998-07-31 06:21:55 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.3"
 * @see Constant
 */
class ConstantString extends Constant {
  /** The value of the <code>string_index</code> item must be a valid
      index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that point must be a
      <code>CONSTANT_Utf8_info</code> representing the sequence of
      characters to which the <code>java.lang.String</code> object is
      to be initialized. */
  int string_index;

  /** Constructor. */
  ConstantString(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    string_index = in.read_u2();
  }
  /** Constructor. */
  public ConstantString(ClassFile parent, int string_index) {
    super(parent);
    this.string_index = string_index;
  }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_String); // tag
    out.write_u2(string_index);
  }

  // convenience.
  ConstantUtf8 string_index() 
  { return (ConstantUtf8) parent.constant_pool[string_index]; }
  String string() { return string_index().val; }
}
