package ClassFile;

/**
 * Each method, and each instance initialization method &lt;init&gt;,
 * is described by a variable-length <code>method_info</code>
 * structure.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodInfo.java,v 1.4 1998-07-30 11:59:01 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.6"
 * @see ClassFile
 */
public class MethodInfo {
  /** ClassFile in which this method information is found. */
  public ClassFile parent;

  /** Access permissions and properties of the method. */
  AccessFlags access_flags;

  /** The value of the <code>name_index</code> item must be a valid
      index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure representing either
      one of the special internal method names, either &lt;init&gt; or
      &lt;clint&gt;, or a valid Java method name, stored as a simple
      (not fully qualified) name. */
  int name_index;
  /** The value of the <code>descriptor_index</code> item must be a
      valid index into the <code>constant_pool</code> table.  The
      <code>constant_pool</code> entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure representing a valid
      Java method descriptor. */
  int descriptor_index;
  
  /** A method can have any number of optional attributes associated
      with it. <p> The only attributes defined by this specification
      for the <code>attributes</code> table of a
      <code>method_info</code> structure are the <code>Code</code> and
      <code>Exceptions</code> attributes. */
  AttributeInfo attributes[];
  
  /** Read a single MethodInfo item from an input class bytecode file. */
  void read(ClassFile p, ClassDataInputStream in)
       throws java.io.IOException {
    this.parent = p;

    access_flags = new AccessFlags(in);

    name_index = in.read_u2();
    descriptor_index = in.read_u2();

    int attributes_count = in.read_u2();
    attributes = new AttributeInfo[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = AttributeInfo.read(in, cp);
  }

  /** Constructor. */
  MethodInfo(ClassFile p, ClassDataInputStream in)
       throws java.io.IOException {
    read(p, in);
  }
  /** Constructor. */
  public MethodInfo(ClassFile parent, AccessFlags access_flags,
		    int name_index, int descriptor_index,
		    AttributeInfo attributes[]) {
    this.parent = parent;
    this.access_flags = access_flags;
    this.name_index = name_index;
    this.descriptor_index = descriptor_index;
    this.attributes = attributes;
  }

  /** Writes a MethodInfo item out to a class bytecode file. */
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
