package ClassFile;

class ConstantUtf8 extends ConstantPoolInfo {
  String val;

  ConstantUtf8(ClassDataInputStream in) throws java.io.IOException {
    val = in.readUTF();
  }
}
