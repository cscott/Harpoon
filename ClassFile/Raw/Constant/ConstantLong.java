package ClassFile;

public class ConstantLong extends ConstantPoolInfo {
  long val;
  
  ConstantLong(ClassDataInputStream in) throws java.io.IOException {
    val = in.readLong();
  }

  public long longValue() { return val; }
}
