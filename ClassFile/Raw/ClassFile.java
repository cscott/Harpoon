package ClassFile;

public class ClassFile {
  static final long MAGIC=0xcafebabeL;
  int minor_version;
  int major_version;
  int constant_pool_count;
  ConstantPoolInfo constant_pool[];
  AccessFlags access_flags;

  int this_class;
  int super_class;

  int interfaces_count;
  int interfaces[];

  int fields_count;
  FieldInfo fields[];
  int methods_count;
  MethodInfo methods[];
  int attributes_count;
  AttributeInfo attributes[];

  // Read in a class from a file.
  void read(ClassDataInputStream in) throws java.io.IOException {
    long magic = in.read_u4();
    if (magic != MAGIC)
      throw new ClassDataException("Bad magic: " + Long.toHexString(magic));

    minor_version = in.read_u2();
    major_version = in.read_u2();

    constant_pool_count = in.read_u2();
    constant_pool = new ConstantPoolInfo[constant_pool_count];
    // FIXME initialize constant_pool[0]
    for (int i=1; i<constant_pool_count; i++) {
      constant_pool[i] = ConstantPoolInfo.read(in);
      if (constant_pool[i] instanceof ConstantLong ||
	  constant_pool[i] instanceof ConstantDouble)
	i++; // Long and Double constants take up two entries.
    }

    access_flags = new AccessFlags(in);

    this_class   = in.read_u2();
    super_class  = in.read_u2();

    interfaces_count = in.read_u2();
    interfaces = new int[interfaces_count];
    for (int i=0; i<interfaces_count; i++)
      interfaces[i] = in.read_u2();

    fields_count = in.read_u2();
    fields = new FieldInfo[fields_count];
    for (int i=0; i<fields_count; i++)
      fields[i] = new FieldInfo(in, constant_pool);

    methods_count = in.read_u2();
    methods = new MethodInfo[methods_count];
    for (int i=0; i<methods_count; i++)
      methods[i] = new MethodInfo(in, constant_pool);

    attributes_count = in.read_u2();
    attributes = new AttributeInfo[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = AttributeInfo.read(in, constant_pool);
  }

  public ClassFile(ClassDataInputStream in) throws java.io.IOException {
    read(in);
  }
  public ClassFile(java.io.InputStream in) throws java.io.IOException {
    read(new ClassDataInputStream(in));
  }

  // Interrogate the data structures.
  public int major_version() { return major_version; }
  public int minor_version() { return minor_version; }
  public ConstantPoolInfo[] constant_pool() { return constant_pool; }
  public AccessFlags access_flags() { return access_flags; }
  public ConstantClass this_class() 
  { return (ConstantClass) constant_pool[this_class]; }
  public ConstantClass super_class()
  { return (ConstantClass) constant_pool[super_class]; }
  public ConstantClass[] interfaces()
  { 
    ConstantClass[] interface_class = new ConstantClass[interfaces_count];
    for (int i=0; i<interfaces_count; i++)
      interface_class[i] = (ConstantClass) constant_pool[interfaces[i]];
    return interface_class;
  }
  public FieldInfo[] fields() { return fields; }
  public MethodInfo[] methods() { return methods; }
  public AttributeInfo[] attributes() { return attributes; }
}
