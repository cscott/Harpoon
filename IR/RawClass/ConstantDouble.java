// ConstantDouble.java, created Mon Jan 18 22:44:37 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>CONSTANT_Double_info</code> structure represents eight-byte
 * floating-point numeric constants.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantDouble.java,v 1.3 2003-09-05 21:45:16 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.5"
 * @see Constant
 * @see ConstantLong
 */
public class ConstantDouble extends ConstantValue {
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
  public int entrySize() { return 2; } // takes up two entries in table.

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Double);
    out.writeDouble(val);
  }

  /** Returns the value of this constant. */
  public double doubleValue() { return val; }
  /** Returns the value of this constant, wrapped as a 
   *  <code>java.lang.Double</code>. */
  public Object value() { return new Double(val); }

  /** Create a human-readable representation of this constant. */
  public String toString() {
    return "CONSTANT_Double: "+doubleValue();
  }
}
