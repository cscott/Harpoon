// Attribute.java, created Mon Jan 18 22:44:33 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * Attributes are used in the <code>ClassFile</code>,
 * <code>field_info</code>, <code>method_info</code>, and
 * <code>Code_attribute</code> structures of the <code>class</code> file
 * format.  <code>Attribute</code> is the superclass of the different
 * types of attribute information classes.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Attribute.java,v 1.4 2003-09-05 21:45:15 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7"
 * @see ClassFile
 * @see FieldInfo
 * @see MethodInfo
 * @see AttributeCode
 */
public abstract class Attribute {
  /** ClassFile in which this attribute information is found. */
  protected ClassFile parent;

  /** The <code>attribute_name_index</code> must be a valid unsigned
      16-bit index into the constant pool of the class.  The
      <code>constant_pool</code> entry at
      <code>attribute_name_index</code> must be a
      <code>CONSTANT_Utf8</code> string representing the name of the
      attribute. */
  public int attribute_name_index;
  /** The value of the <code>attribute_length</code> item indicates
   *  the length of the attribute, excluding the initial six bytes. 
   */ 
  public abstract long attribute_length();

  /** Constructor.  Meant for use only by subclasses. */
  protected Attribute(ClassFile p, int attribute_name_index) {
    this.parent = p;
    this.attribute_name_index = attribute_name_index;
  }

  /** Read an Attribute from a ClassDataInputStream. */
  public static Attribute read(ClassFile parent, ClassDataInputStream in)
       throws java.io.IOException {
    int attribute_name_index = in.read_u2();
    String attribute_name = 
      ((ConstantUtf8) parent.constant_pool[attribute_name_index]).val;

    if (attribute_name.equals(AttributeSourceFile.ATTRIBUTE_NAME))
      return new AttributeSourceFile(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeConstantValue.ATTRIBUTE_NAME))
      return new AttributeConstantValue(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeCode.ATTRIBUTE_NAME))
      return new AttributeCode(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeExceptions.ATTRIBUTE_NAME))
      return new AttributeExceptions(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeLineNumberTable.ATTRIBUTE_NAME))
      return new AttributeLineNumberTable(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeLocalVariableTable.ATTRIBUTE_NAME))
      return new AttributeLocalVariableTable(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeInnerClasses.ATTRIBUTE_NAME))
      return new AttributeInnerClasses(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeSynthetic.ATTRIBUTE_NAME))
      return new AttributeSynthetic(parent, in, attribute_name_index);
    if (attribute_name.equals(AttributeSignature.ATTRIBUTE_NAME))
	return new AttributeSignature(parent, in, attribute_name_index);
    // Unknown attribute type.
    return new AttributeUnknown(parent, in, attribute_name_index);
  }

  /** Write Attribute to bytecode file. */
  public abstract void write(ClassDataOutputStream out) 
    throws java.io.IOException;
  
  // convenience functions.
  public ConstantUtf8 attribute_name_index()
  { return (ConstantUtf8) parent.constant_pool[attribute_name_index]; }
  public String attribute_name() { return attribute_name_index().val; }

  /** Create a human-readable representation for the Attribute. */
  public String toString()
  { return "("+attribute_name()+" Attribute["+attribute_length()+"])"; }

  /** Pretty-print this attribute structure. */
  public void print(java.io.PrintWriter pw, int indent) {
    indent(pw, indent, toString());
  }
  static void indent(java.io.PrintWriter pw, int indent, String s) {
    ClassFile.indent(pw,indent,s);
  }
}
