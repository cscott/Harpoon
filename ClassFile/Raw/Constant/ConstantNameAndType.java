package ClassFile;

class ConstantNameAndType extends ConstantPoolInfo {
  int name_index;
  int descriptor_index;

  ConstantNameAndType(ClassDataInputStream in) throws java.io.IOException {
    name_index = in.read_u2();
    descriptor_index = in.read_u2();
  }
}
