// ConstantInteger.java, created Mon Jan 18 22:44:37 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>CONSTANT_Integer_info</code> structure represents
 * four-byte integer numeric constants.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantInteger.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.4"
 * @see Constant
 * @see ConstantFloat
 */
public class ConstantInteger extends ConstantValue {
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

  /** Returns the integer value of this constant. */
  public int intValue() { return val; }
  /** Returns the value of this constant, wrapped as a 
   *  <code>java.lang.Integer</code>. */
  public Object value() { return new Integer(val); }

  /** Create a human-readable representation of this constant. */
  public String toString() {
    return "CONSTANT_Integer: "+intValue();
  }
}
