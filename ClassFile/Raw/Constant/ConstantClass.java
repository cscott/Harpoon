package ClassFile;

class ConstantClass extends ConstantPoolInfo {
  int name_index;

  ConstantClass(ClassDataInputStream in) throws java.io.IOException {
    name_index = in.read_u2();
  }
}
