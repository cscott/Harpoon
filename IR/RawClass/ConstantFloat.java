// ConstantFloat.java, created Mon Jan 18 22:44:37 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>CONSTANT_Float_info</code> structure represents four-byte
 * floating-point numeric constants.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantFloat.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantInteger
 */
public class ConstantFloat extends ConstantValue {
  /** The value of the <code>float</code> constant. */
  public float val;

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
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Float);
    out.writeFloat(val);
  }
  
  /** Returns the floating-point value of this constant. */
  public float floatValue() { return val; }
  /** Returns the value of this constant, wrapped as a 
   *  <code>java.lang.Float</code>. */
  public Object value() { return new Float(val); }

  /** Create a human-readable representation of this constant. */
  public String toString() {
    return "CONSTANT_Float: "+floatValue();
  }
}
