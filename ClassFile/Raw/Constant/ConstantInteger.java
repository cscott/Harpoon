package harpoon.ClassFile.Raw.Constant;

import harpoon.ClassFile.Raw.*;
/**
 * The <code>CONSTANT_Integer_info</code> structure represents
 * four-byte integer numeric constants.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: ConstantInteger.java,v 1.9 1998-08-01 22:50:07 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantFloat
 */
public class ConstantInteger extends Constant {
  /** The value of the <code>int</code> constant. */
  public int val;

  /** Constructor. */
  ConstantInteger(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    val = in.readInt();
  }
  /** Constructor. */
  public ConstantInteger(ClassFile parent, int val) { 
    super(parent);
    this.val = val; 
  }

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Integer);
    out.writeInt(val);
  }

  public int intValue() { return val; }
}
