package ClassFile;

/**
 * The <code>CONSTANT_Double_info</code> structure represents eight-byte
 * floating-point numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantDouble.java,v 1.5 1998-07-30 11:59:00 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.5"
 * @see Constant
 * @see ConstantLong
 */
public class ConstantDouble extends ConstantPoolInfo {
  /** The value of the <code>double</code> constant. */
  double val;

  /** Constructor. */
  ConstantDouble(ClassDataInputStream in) throws java.io.IOException {
    val = in.readDouble();
  }
  /** Constructor. */
  public ConstantDouble(double val) { this.val = val; }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Double);
    out.writeDouble(val);
  }

  public double doubleValue() { return val; }
}
