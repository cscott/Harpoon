package ClassFile;

class ConstantLong extends ConstantPoolInfo {
  long val;
  
  ConstantLong(ClassDataInputStream in) throws java.io.IOException {
    val = in.readLong();
  }
}
