package ClassFile;

class FieldInfo {
  AccessFlags access_flags;

  int name_index;
  int descriptor_index;
  String name, descriptor;

  int attributes_count;
  AttributeInfo attributes[];

  void read(ClassDataInputStream in, ConstantPoolInfo cp[]) 
       throws java.io.IOException {
    access_flags = new AccessFlags(in);

    name_index   = in.read_u2();
    descriptor_index = in.read_u2();
    name = ((ConstantUtf8) cp[name_index]).val;
    descriptor = ((ConstantUtf8) cp[descriptor_index]).val;

    attributes_count = in.read_u2();
    attributes = new AttributeInfo[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = AttributeInfo.read(in, cp);
  }

  FieldInfo(ClassDataInputStream in, ConstantPoolInfo cp[]) 
       throws java.io.IOException {
    read(in, cp);
  }
}
