package harpoon.ClassFile.Raw;

/**
 * The <code>Code</code> attribute is a variable-length attribute used
 * in the <code>attributes</code> table of <code>method_info</code>
 * structures.  A <code>Code</code> attribute contains the Java Virtual
 * Machine instructions and auxilliary information for a single Java
 * method, instance initialization method, or class or interface
 * initialization method.  There must be exactly one <code>Code</code>
 * attribute in each <code>method_info</code> structure.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeCode.java,v 1.5 1998-07-31 05:51:08 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.4"
 * @see AttributeLineNumberTable
 * @see AttributeLocalVariableTable
 * @see Attribute
 * @see MethodInfo
 * @see ClassFile
 */
public class AttributeCode extends Attribute {
  /** The value of the <code>max_stack</code> item gives the maximum
      number of words on the operand stack at any point during
      execution of this method. */
  int max_stack;
  /** The value of the <code>max_locals</code> item gives the number
      of local variables used by this method, including the parameters
      passes to the method on invocation.  The index of the first
      local variable is 0. */
  int max_locals;
  /** The <code>code</code> array gives the actual bytes of Java
      Virtual Machine code that implement the method. */
  byte code[];
  /** Each entry in the <code>exception_table</code> array describes
      one exception handler in the <code>code</code> array. */
  ExceptionTable exception_table[];
  /** Each value of the <code>attributes</code> table must be a
      variable-length attribute structure.  A <code>Code</code>
      attribute can have any number of optional attributes associated
      with it.  <p> Currently, the <code>LinueNumberTable</code> and
      <code>LocalVariableTable</code> attributes, both of which
      contain debugging information, are defined and used with the
      <code>Code</code> attribute. */
  Attribute attributes[];

  /** Constructor. */
  AttributeCode(ClassFile parent, ClassDataInputStream in,
		int attribute_name_index) throws java.io.IOException 
  {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();

    max_stack = in.read_u2();
    max_locals= in.read_u2();

    long code_length = in.read_u4();
    code = new byte[(int)code_length]; // XXX ugly. dies after 31-bits
    for (long l=0; l<code_length; l++)
      code[(int)l] = in.readByte();    // XXX ugly. dies after 31-bits

    int exception_table_length = in.read_u2();
    exception_table = new ExceptionTable[exception_table_length];
    for (int i=0; i<exception_table_length; i++)
      exception_table[i] = new ExceptionTable(parent, in);

    int attributes_count = in.read_u2();
    attributes = new Attribute[attributes_count];
    for (int i=0; i<attributes_count; i++)
      attributes[i] = Attribute.read(parent, in);

    if (attribute_length != attribute_length())
      throw new ClassDataException("Code attribute of incorrect length.");
  }

  /** Constructor. */
  public AttributeCode(ClassFile parent, int attribute_name_index,
		       int max_stack, int max_locals, byte code[],
		       ExceptionTable exception_table[],
		       Attribute attributes[]) {
    super(parent, attribute_name_index);
    this.max_stack = max_stack;
    this.max_locals= max_locals;
    this.code = code;
    this.exception_table = exception_table;
    this.attributes = attributes;
  }
  
  // convenience.
  long code_length() { return code.length; }
  int exception_table_length() { return exception_table.length; }
  int attributes_count() { return attributes.length; }

  long attribute_length() {
    long len = 12 + code_length() + 8*exception_table_length();
    for (int i=0; i < attributes.length; i++)
      len += attributes[i].attribute_length();
    return len;
  }

  /** Write to bytecode stream. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());

    out.write_u2(max_stack);
    out.write_u2(max_locals);
    
    out.write_u4(code_length());
    out.write(code);

    out.write_u2(exception_table_length());
    for (int i=0; i<exception_table.length; i++)
      exception_table[i].write(out);
    
    out.write_u2(attributes_count());
    for (int i=0; i<attributes.length; i++)
      attributes[i].write(out);
  }

  /** 
   * Return the symbolic name of a local variable, or null if one cannot
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
