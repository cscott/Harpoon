// FieldInfo.java, created Mon Jan 18 22:44:38 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * Each field is described by a variable-length
 * <code>field_info</code> structure.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldInfo.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.5"
 * @see ClassFile
 * @see harpoon.ClassFile.HField
 */
public class FieldInfo {
  /** ClassFile in which this field information is found. */
  protected ClassFile parent;

  /** Access permissions and properties of the field. */
  public AccessFlags access_flags;

  /** The value of the <code>name_index</code> item must be a valid
      index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure which must represent a
      valid Java field name stored as a simple (not fully qualified)
      name, that is, as a Java identifier. */
  public int name_index;
  /** The value of the <code>descriptor_index</code> item must be a
      valid index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure which must represent a
      valid Java field descriptor. */
  public int descriptor_index;

  /** A field can have any number of attributes associated with
      it. <p> The only attributed defined for the
      <code>attributes</code> table of a <code>field_info</code>
      structure by this specification is the
      <code>ConstantValue</code> attribute. */
  public Attribute attributes[];

  /** Read a single FieldInfo item from an input class bytecode file. */
  public void read(ClassFile p, ClassDataInputStream in) 
       throws java.io.IOException {
    this.parent = p;

    access_flags = new AccessFlags(in);

    name_index   = in.read_u2();
    descriptor_index = in.read_u2();

    int attributes_count = in.read_u2();
    attributes = new Attribute[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = Attribute.read(p, in);
  }

  /** Constructor. */
  FieldInfo(ClassFile p, ClassDataInputStream in) 
       throws java.io.IOException {
    read(p, in);
  }
  /** Constructor. */
  public FieldInfo(ClassFile parent, AccessFlags access_flags,
		   int name_index, int descriptor_index,
		   Attribute attributes[]) {
    this.parent = parent;
    this.access_flags = access_flags;
    this.name_index = name_index;
    this.descriptor_index = descriptor_index;
    this.attributes = attributes;
  }

  /** Writes a FieldInfo item out to a class bytecode file. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    access_flags.write(out);
    out.write_u2(name_index);
    out.write_u2(descriptor_index);
    
    if (attributes.length > 0xFFFF)
      throw new ClassDataException("Attributes list too long: " +
				   attributes.length);
    out.write_u2(attributes.length);
    for (int i=0; i<attributes.length; i++)
      attributes[i].write(out);
  }

  // convenience
  public ConstantUtf8 name_index()
  { return (ConstantUtf8) parent.constant_pool[name_index]; }
  public ConstantUtf8 descriptor_index()
  { return (ConstantUtf8) parent.constant_pool[descriptor_index]; }

  public String name() { return name_index().val; }
  public String descriptor() { return descriptor_index().val; }

  /** Pretty-print this field_info structure. */
  public void print(java.io.PrintWriter pw, int indent) {
    int in=indent;
    ClassFile.indent(pw, in, "Access Flags: " + access_flags);
    ClassFile.indent(pw, in, "Name: " + name() + " {"+name_index+"}");
    ClassFile.indent(pw, in, "Descriptor: " + 
		     descriptor() + " {"+descriptor_index+"}");
    ClassFile.indent(pw, in, "Attributes ["+attributes.length+"]:");
    for (int i=0; i<attributes.length; i++) {
      ClassFile.indent(pw, in+1, "#"+i+":");
      attributes[i].print(pw, in+2);
    }
  }
}
