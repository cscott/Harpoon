package ClassFile;

public class AttributeInfo {
  int attribute_name_index;
  long attribute_length;
  String attribute_name;

  AttributeInfo(int ani, long len, ConstantPoolInfo cp[]) {
    attribute_name_index = ani;
    attribute_length = len;
    attribute_name = ((ConstantUtf8) cp[attribute_name_index]).val;
  }

  static AttributeInfo read(ClassDataInputStream in, ConstantPoolInfo cp[])
       throws java.io.IOException {
    int attribute_name_index = in.read_u2();
    String attribute_name = ((ConstantUtf8) cp[attribute_name_index]).val;
 
    if (attribute_name.equals("SourceFile"))
      return new AttributeSourceFile(in, cp, attribute_name_index);
    if (attribute_name.equals("ConstantValue"))
      return new AttributeConstantValue(in, cp, attribute_name_index);
    if (attribute_name.equals("Code"))
      return new AttributeCode(in, cp, attribute_name_index);
    if (attribute_name.equals("Exceptions"))
      return new AttributeExceptions(in, cp, attribute_name_index);
    if (attribute_name.equals("LineNumberTable"))
      return new AttributeLineNumberTable(in, cp, attribute_name_index);
    if (attribute_name.equals("LocalVariableTable"))
      return new AttributeLocalVariableTable(in, cp, attribute_name_index);

    // We don't know this attribute type.  Discard.
    System.err.println("Discarding attribute /"+attribute_name+"/");

    long attribute_length = in.read_u4();
    for (long l=0; l<attribute_length; l++)
      in.read_u1();

    return new AttributeInfo(attribute_name_index, attribute_length, cp);;
  }

  public String toString() { return "("+attribute_name+")"; }
}

  
