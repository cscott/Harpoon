package harpoon.ClassFile.Raw;

/**
 * The <code>CONSTANT_Float_info</code> structure represents four-byte
 * floating-point numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantFloat.java,v 1.7 1998-07-31 06:21:55 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantInteger
 */
public class ConstantFloat extends Constant {
  /** The value of the <code>float</code> constant. */
  float val;

  /** Constructor. */
  ConstantFloat(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    val = in.readFloat();
  }
  /** Constructor. */
  public ConstantFloat(ClassFile parent, float val) { 
    super(parent);
    this.val = val; 
  }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Float);
    out.writeFloat(val);
  }
  
  public float floatValue() { return val; }
}
