package ClassFile;

class ConstantInteger extends ConstantPoolInfo {
  int val;

  ConstantInteger(ClassDataInputStream in) throws java.io.IOException {
    val = in.readInt();
  }
}
