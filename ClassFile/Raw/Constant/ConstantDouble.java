package harpoon.ClassFile.Raw.Constant;

import harpoon.ClassFile.Raw.*;
/**
 * The <code>CONSTANT_Double_info</code> structure represents eight-byte
 * floating-point numeric constants.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantDouble.java,v 1.10 1998-08-01 22:55:16 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.5"
 * @see Constant
 * @see ConstantLong
 */
public class ConstantDouble extends Constant {
  /** The value of the <code>double</code> constant. */
  public double val;

  /** Constructor. */
  ConstantDouble(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    this.val = in.readDouble();
  }
  /** Constructor. */
  public ConstantDouble(ClassFile parent, double val) { 
    super(parent);
    this.val = val; 
  }

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Double);
    out.writeDouble(val);
  }

  public double doubleValue() { return val; }
}
