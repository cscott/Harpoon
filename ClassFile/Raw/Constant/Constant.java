package ClassFile;

public class ConstantPoolInfo {
  static final int CONSTANT_Utf8=1;
  static final int CONSTANT_Integer=3;
  static final int CONSTANT_Float=4;
  static final int CONSTANT_Long=5;
  static final int CONSTANT_Double=6;
  static final int CONSTANT_Class=7;
  static final int CONSTANT_String=8;
  static final int CONSTANT_Fieldref=9;
  static final int CONSTANT_Methodref=10;
  static final int CONSTANT_InterfaceMethodref=11;
  static final int CONSTANT_NameAndType=12;

  static ConstantPoolInfo read(ClassDataInputStream in) 
       throws java.io.IOException {
    int tag = in.read_u1();
    switch(tag) {
    case CONSTANT_Utf8:
      return new ConstantUtf8(in);
    case CONSTANT_Integer:
      return new ConstantInteger(in);
    case CONSTANT_Float:
      return new ConstantFloat(in);
    case CONSTANT_Long:
      return new ConstantLong(in);
    case CONSTANT_Double:
      return new ConstantDouble(in);
    case CONSTANT_Class:
      return new ConstantClass(in);
    case CONSTANT_String:
      return new ConstantString(in);
    case CONSTANT_Fieldref:
      return new ConstantFieldref(in);
    case CONSTANT_Methodref:
      return new ConstantMethodref(in);
    case CONSTANT_InterfaceMethodref:
      return new ConstantInterfaceMethodref(in);
    case CONSTANT_NameAndType:
      return new ConstantNameAndType(in);
    default:
      throw new Error("Unknown constant type.");
    }
  }
}
