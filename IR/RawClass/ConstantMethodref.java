// ConstantMethodref.java, created Mon Jan 18 22:44:37 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * The <code>CONSTANT_Methodref</code> structure represents a method.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantMethodref.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see "The Java Virtual Machine Specifications, section 4.4.2"
 * @see Constant
 * @see ConstantFieldref
 * @see ConstantInterfaceMethodref
 */
public class ConstantMethodref extends Constant {
  /** The value of the <code>class_index</code> item must be a valid
      index into the <code>constant_pool</code> table of the
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_Class_info</code> structure
      representing the class or interface type that contains the
      declaration of the method.
      <p>
      The <code>class_index</code> item of a
      <code>CONSTANT_Methodref</code> must be a class type, not an
      interface type. */
  public int class_index;
  /** The value of the <code>name_and_type_index</code> item must be a
      valid index into the <code>constant_pool</code> table of the
      <code>parent</code>.  The <code>constant_pool</code> entry at
      that index must be a <code>CONSTANT_NameAndType_info</code>
      structure.  This <code>constant_pool</code> entry indicates the
      name and descriptor of the method. 
      <p>
      If the name of the method of a
      <code>CONSTANT_Methodref_info</code> begins with a '&lt;'
      ('\u003c'), then the name must be one of the special internal
      methods, either &lt;init&gt; or &lt;clinit&gt;.  In this case,
      the method must return no value. */
  public int name_and_type_index;
  
  /** Constructor. */
  ConstantMethodref(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    super(parent);
    class_index = in.read_u2();
    name_and_type_index = in.read_u2();
  }
  /** Constructor. */
  public ConstantMethodref(ClassFile parent, 
			   int class_index, int name_and_type_index) {
    super(parent);
    this.class_index = class_index;
    this.name_and_type_index = name_and_type_index;
  }

  /** Write to a bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u1(CONSTANT_Methodref);
    out.write_u2(class_index);
    out.write_u2(name_and_type_index);
  }

  // convenience.
  public ConstantClass class_index()
  { return (ConstantClass) parent.constant_pool[class_index]; }
  public ConstantNameAndType name_and_type_index()
  { return (ConstantNameAndType) parent.constant_pool[name_and_type_index]; }

  /** Create a human-readable representation of this constant. */
  public String toString() {
    ConstantNameAndType cnt = name_and_type_index();
    return "CONSTANT_Methodref: " +
      class_index().name() + " {" + class_index+"} " + "(" +
      cnt.name() + " {"+cnt.name_index+"} " +
      cnt.descriptor() + " {"+cnt.descriptor_index+"}" + ")" + 
      " {"+name_and_type_index+"}";
  }
}
