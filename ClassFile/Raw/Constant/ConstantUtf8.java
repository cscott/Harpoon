package harpoon.ClassFile.Raw;

/**
 * The <code>CONSTANT_Utf8_info</code> structure is used to represent
 * constant string values. <p> UTF-8 strings are encoded so that
 * character sequences that contain only non-null ASCII characters can
 * be represented using only one byte per character, but characters of
 * up to 16 bits can be represented.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantUtf8.java,v 1.5 1998-07-31 05:51:10 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.7"
 * @see Constant
 */
class ConstantUtf8 extends ConstantPoolInfo {
  /** The value of the string constant */
  String val;

  /** Constructor. */
  ConstantUtf8(ClassDataInputStream in) throws java.io.IOException {
    val = in.readUTF();
  }
  /** Constructor. */
  public ConstantUtf8(String val) { this.val = val; }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Utf8);
    out.writeUTF(val);
  }
}
