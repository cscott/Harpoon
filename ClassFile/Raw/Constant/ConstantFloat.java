package ClassFile;

/**
 * The <code>CONSTANT_Float_info</code> structure represents four-byte
 * floating-point numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantFloat.java,v 1.5 1998-07-30 11:59:00 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantInteger
 */
public class ConstantFloat extends ConstantPoolInfo {
  /** The value of the <code>float</code> constant. */
  float val;

  /** Constructor. */
  ConstantFloat(ClassDataInputStream in) throws java.io.IOException {
    val = in.readFloat();
  }
  /** Constructor. */
  public ConstantFloat(float val) { this.val = val; }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Float);
    out.writeFloat(val);
  }
  
  public float floatValue() { return val; }
}
