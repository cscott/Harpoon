package ClassFile;

class ConstantString extends ConstantPoolInfo {
  int string_index;

  ConstantString(ClassDataInputStream in) throws java.io.IOException {
    string_index = in.read_u2();
  }
}
