package ClassFile;

class ConstantFieldref extends ConstantPoolInfo {
  int class_index;
  int name_and_type_index;

  ConstantFieldref(ClassDataInputStream in) throws java.io.IOException {
    class_index = in.read_u2();
    name_and_type_index = in.read_u2();
  }
}
