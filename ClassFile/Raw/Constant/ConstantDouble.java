package ClassFile;

public class ConstantDouble extends ConstantPoolInfo {
  double val;

  ConstantDouble(ClassDataInputStream in) throws java.io.IOException {
    val = in.readDouble();
  }

  public double doubleValue() { return val; }
}
