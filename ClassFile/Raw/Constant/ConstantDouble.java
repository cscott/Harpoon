package ClassFile;

class ConstantDouble extends ConstantPoolInfo {
  double val;

  ConstantDouble(ClassDataInputStream in) throws java.io.IOException {
    val = in.readDouble();
  }
}
