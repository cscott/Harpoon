package ClassFile;

public class AttributeCode extends AttributeInfo {
  int max_stack;
  int max_locals;
  long code_length;
  byte code[];
  
  int exception_table_length;
  ExceptionTable exception_table[];
  
  int attributes_count;
  AttributeInfo attributes[];

  AttributeCode(ClassDataInputStream in, ConstantPoolInfo cp[],
		int ani) throws java.io.IOException 
  {
    super(ani, in.read_u4(), cp);

    max_stack = in.read_u2();
    max_locals= in.read_u2();

    code_length=in.read_u4();
    code = new byte[(int)code_length]; // XXX ugly.
    for (long l=0; l<code_length; l++)
      code[(int)l] = in.readByte();    // XXX ugly.

    exception_table_length = in.read_u2();
    exception_table = new ExceptionTable[exception_table_length];
    for (int i=0; i<exception_table_length; i++)
      exception_table[i] = new ExceptionTable(in, cp);

    attributes_count = in.read_u2();
    attributes = new AttributeInfo[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = AttributeInfo.read(in, cp);
  }

  public int max_stack() { return max_stack; }
  public int max_locals() { return max_locals; }
  public byte[] code() { return code; }
  public ExceptionTable[] exception_table() { return exception_table; }
  public AttributeInfo[] attributes() { return attributes; }
}

