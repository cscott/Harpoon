package ClassFile;

/** 
 * The <code>CONSTANT_Long_info</code> structure represents eight-byte
 * integer numeric constants.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantLong.java,v 1.5 1998-07-30 11:59:00 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.5"
 * @see Constant
 * @see ConstantDouble
 */
public class ConstantLong extends ConstantPoolInfo {
  /** The value of the <code>long</code> constant. */
  long val;
  
  /** Constructor. */
  ConstantLong(ClassDataInputStream in) throws java.io.IOException {
    val = in.readLong();
  }
  /** Constructor. */
  public ConstantLong(long val) { this.val = val; }

  /** Write to a bytecode file. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Long);
    out.writeLong(val);
  }

  public long longValue() { return val; }
}
