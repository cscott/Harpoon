package ClassFile;

class ConstantFloat extends ConstantPoolInfo {
  float val;

  ConstantFloat(ClassDataInputStream in) throws java.io.IOException {
    val = in.readFloat();
  }
}
