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
    code = new byte[(int)code_length]; // XXX ugly. dies after 31-bits
    for (long l=0; l<code_length; l++)
      code[(int)l] = in.readByte();    // XXX ugly. dies after 31-bits

    exception_table_length = in.read_u2();
    exception_table = new ExceptionTable[exception_table_length];
    for (int i=0; i<exception_table_length; i++)
      exception_table[i] = new ExceptionTable(in, cp);

    attributes_count = in.read_u2();
    attributes = new AttributeInfo[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = AttributeInfo.read(in, cp);
  }

  /** Returns the maximum stack depth used by this code. */
  public int max_stack() { return max_stack; }
  /** Return the maximum number of local variables used by this code. */
  public int max_locals() { return max_locals; }
  /** Return the bytecode array associated with this code. */
  public byte[] code() { return code; }
  /** Return the exception table associated with this code. */
  public ExceptionTable[] exception_table() { return exception_table; }
  /** Return the attributes associated with this code. */
  public AttributeInfo[] attributes() { return attributes; }

  /** 
   * Return the symbol name of a local variable, or null if one cannot
   * be found.
   * @param index the index of the local variable to look up.
   * @return the name of the local, or null.
   */
  public String localName(int index) {
    for(int i=0; i<attributes.length; i++)
      if (attributes[i] instanceof AttributeLocalVariableTable) {
	AttributeLocalVariableTable alvt = 
	  (AttributeLocalVariableTable) attributes[i];
	String name = alvt.localName(index);
	if (name!=null) return name;
      }
    // Can't find a name for this in any table.
    return null;
  }
}

