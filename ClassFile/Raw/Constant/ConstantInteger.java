package harpoon.ClassFile.Raw;

/**
 * The <code>CONSTANT_Integer_info</code> structure represents
 * four-byte integer numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantInteger.java,v 1.6 1998-07-31 05:51:09 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantFloat
 */
public class ConstantInteger extends ConstantPoolInfo {
  /** The value of the <code>int</code> constant. */
  int val;

  /** Constructor. */
  ConstantInteger(ClassDataInputStream in) throws java.io.IOException {
    val = in.readInt();
  }
  /** Constructor. */
  public ConstantInteger(int val) { this.val = val; }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Integer);
    out.writeInt(val);
  }

  public int intValue() { return val; }
}
