// ConstantClass.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * The <code>CONSTANT_Class_info</code> structure is used to
 * represent a class or an interface.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantClass.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.4.1"
 * @see Constant
 */
public class ConstantClass extends Constant {
  /** The value of the <code>name_index</code> item must be a valid
      index into the <code>constant_pool</code> table of
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_Utf8_info</code> structure
      representing a valid fully qualified Java class name that has
      been converted to the <code>class</code> file's internal form. */
  public int name_index;

  /** Constructor. */
  ConstantClass(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException 
  {
    super(parent);
    name_index = in.read_u2();
  }
  /** Constructor. */
  public ConstantClass(ClassFile parent, int name_index) { 
    super(parent);
    this.name_index = name_index;
  }

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Class);
    out.write_u2(name_index);
  }

  // convenience.
  public ConstantUtf8 name_index()
  { return (ConstantUtf8) parent.constant_pool[name_index]; }

  public String name() { return name_index().val; }

  /** Create a human-readable representation of this constant. */
  public String toString() {
    return "CONSTANT_Class: " + name() + " {"+name_index+"}";
  }
}
