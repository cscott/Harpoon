package ClassFile;

class LocalVariableTable {
  int start_pc, length;
  int name_index, descriptor_index;
  String name, descriptor;
  int index;

  LocalVariableTable(ClassDataInputStream in, ConstantPoolInfo cp[])
       throws java.io.IOException
  {
    start_pc = in.read_u2();
    length   = in.read_u2();

    name_index       = in.read_u2();
    descriptor_index = in.read_u2();
    name       = ((ConstantUtf8) cp[name_index]).val;
    descriptor = ((ConstantUtf8) cp[descriptor_index]).val;

    index = in.read_u2();
  }
}
