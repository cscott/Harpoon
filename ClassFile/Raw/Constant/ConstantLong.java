package harpoon.ClassFile.Raw.Constant;

import harpoon.ClassFile.Raw.*;
/** 
 * The <code>CONSTANT_Long_info</code> structure represents eight-byte
 * integer numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantLong.java,v 1.10 1998-08-01 22:55:17 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.5"
 * @see Constant
 * @see ConstantDouble
 */
public class ConstantLong extends Constant {
  /** The value of the <code>long</code> constant. */
  public long val;
  
  /** Constructor. */
  ConstantLong(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    val = in.readLong();
  }
  /** Constructor. */
  public ConstantLong(ClassFile parent, long val) { 
    super(parent);
    this.val = val; 
  }

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Long);
    out.writeLong(val);
  }

  public long longValue() { return val; }
}
