// AttributeExceptions.java, created Mon Jan 18 22:44:34 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>Exceptions</code> attribute is a variable-length
 * attribute used in the <code>attributes</code> table of a
 * <code>method_info</code> structure.  The <code>Exceptions</code>
 * attribute indicates which checked exceptions a method may throw.  The
 * must be exactly one <code>Exceptions</code> attribute in each
 * <code>method_info</code> structure.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeExceptions.java,v 1.3 2003-09-05 21:45:16 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.5"
 * @see Attribute
 * @see MethodInfo
 */
public class AttributeExceptions extends Attribute {
  /** The string naming this <code>Attribute</code> type. */
  public static final String ATTRIBUTE_NAME = "Exceptions";
  /** Each nonzero value in the <code>exception_index_table</code>
      must be a valid index into the <code>constant_pool</code>
      table.  For each table item, if
      <code>exception_index_table[i] != 0</code>, where <code>0 <= i <
      number_of_exceptions</code>, then the <code>constant_pool</code>
      entry at index <code>exception_index_table[i]</code> must be a
      <code>CONSTANT_Class_info</code> structure representing a class
      type that this method is declared to throw. */
  public int exception_index_table[];
  
  /** Constructor. */
  AttributeExceptions(ClassFile parent, ClassDataInputStream in,
		      int attribute_name_index) throws java.io.IOException
  {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();

    int number_of_exceptions = in.read_u2();
    exception_index_table = new int[number_of_exceptions];
    for(int i=0; i<number_of_exceptions; i++)
      exception_index_table[i] = in.read_u2();

    if (attribute_length != attribute_length())
      throw new ClassDataException("Exceptions attribute with length "
				   + attribute_length);
    assert ATTRIBUTE_NAME.equals(attribute_name());
  }
  
  /** Constructor. */
  public AttributeExceptions(ClassFile parent, int attribute_name_index,
			     int exception_index_table[]) {
    super(parent, attribute_name_index);
    this.exception_index_table = exception_index_table;
    assert ATTRIBUTE_NAME.equals(attribute_name());
  }

  public long attribute_length() { return 2 + 2*number_of_exceptions(); }

  // convenience.
  public int number_of_exceptions() { return exception_index_table.length; }
  /** Returns the CONSTANT_Class_info corresponding to an entry in
   *  the exception_index_table.  Returns <code>null</code> if the
   *  entry is zero.
   */
  public ConstantClass exception_index_table(int i) {
    if (exception_index_table[i]==0) return null;
    else return (ConstantClass)
	   parent.constant_pool[exception_index_table[i]];
  }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    out.write_u2(number_of_exceptions());
    for (int i=0; i<exception_index_table.length; i++)
      out.write_u2(exception_index_table[i]);
  }

  /** Pretty-print the contents of this attribute.
   *  @param indent the indentation level to use.
   */
  public void print(java.io.PrintWriter pw, int indent) {
    int in=indent;
    indent(pw, in, "Exceptions attribute ["+number_of_exceptions()+"]:");
    for (int i=0; i<exception_index_table.length; i++)
      indent(pw, in+1, "#"+i+": " +
	     (exception_index_table(i)==null?"<nothing>":
	      exception_index_table(i).name()) +
	     " {"+exception_index_table[i]+"}");
  }
}
