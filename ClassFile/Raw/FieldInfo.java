package harpoon.ClassFile.Raw;

/**
 * Each field is described by a variable-length
 * <code>field_info</code> structure.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldInfo.java,v 1.6 1998-07-31 06:21:56 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.5"
 * @see ClassFile
 */
class FieldInfo {
  /** ClassFile in which this field information is found. */
  public ClassFile parent;

  /** Access permissions and properties of the field. */
  AccessFlags access_flags;

  /** The value of the <code>name_index</code> item must be a valid
      index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure which must represent a
      valud Java field name stored as a simple (not fully qualified)
      name, that is, as a Java identifier. */
  int name_index;
  /** The value of the <code>descriptor_index</code> item must be a
      valid index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure which must represent a
      valid Java field descriptor. */
  int descriptor_index;

  /** A field can have any number of attributes associated with
      it. <p> The only attributed defined for the
      <code>attributes</code> table of a <code>field_info</code>
      structure by this specification is the
      <code>ConstantValue</code> attribute. */
  Attribute attributes[];

  /** Read a single FieldInfo item from an input class bytecode file. */
  void read(ClassFile p, ClassDataInputStream in) 
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
  void write(ClassDataOutputStream out) throws java.io.IOException {
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
  ConstantUtf8 name_index()
  { return (ConstantUtf8) parent.constant_pool[name_index]; }
  ConstantUtf8 descriptor_index()
  { return (ConstantUtf8) parent.constant_pool[descriptor_index]; }

  String name() { return name_index().val; }
  String descriptor() { return descriptor_index().val; }
}
