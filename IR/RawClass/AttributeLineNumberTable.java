// AttributeLineNumberTable.java, created Mon Jan 18 22:44:35 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>LineNumberTable</code> attribute is an optional
 * variable-length attribute in the <code>attributes</code> table of a
 * <code>Code</code> attribute.  It may be used by debuggers to
 * determine which part of the Java Virtual Machine <code>code</code>
 * array corresponds to a given line number in the original Java source
 * file.  If <code>LineNumberTable</code> attributes are present in the
 * <code>attributes</code> table of a given <code>Code</code> attribute,
 * then they may appear in any order.  Furthermore, multiple
 * <code>LineNumberTable</code> attributes may together represent a
 * given line of a Java source file; that is,
 * <code>LineNumberTable</code> attributes need not be one-to-one with
 * source lines.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeLineNumberTable.java,v 1.3 2003-09-05 21:45:16 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.6"
 * @see AttributeCode
 * @see Attribute
 */
public class AttributeLineNumberTable extends Attribute {
  /** The string naming this <code>Attribute</code> type. */
  public static final String ATTRIBUTE_NAME = "LineNumberTable";
  /** Each entry in the <code>line_number_table</code> array indicates
      that the line number in the original Java source file changes at
      a given point in the <code>code</code> array. */
  public LineNumberTable line_number_table[];

  /** Constructor. */
  AttributeLineNumberTable(ClassFile parent, ClassDataInputStream in,
			   int attribute_name_index) throws java.io.IOException
  {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    
    int line_number_table_length = in.read_u2();
    line_number_table = new LineNumberTable[line_number_table_length];
    for (int i=0; i<line_number_table_length; i++)
      line_number_table[i] = new LineNumberTable(in);
    
    if (attribute_length != attribute_length())
      throw new ClassDataException("LineNumberTable attribute with length "
				   + attribute_length);
    assert ATTRIBUTE_NAME.equals(attribute_name());
  }

  /** Constructor. */
  public AttributeLineNumberTable(ClassFile parent, int attribute_name_index,
				  LineNumberTable line_number_table[]) {
    super(parent, attribute_name_index);
    this.line_number_table = line_number_table;
    assert ATTRIBUTE_NAME.equals(attribute_name());
  }

  public long attribute_length() { return 2 + 4*line_number_table_length(); }

  // Convenience.
  public int line_number_table_length() { return line_number_table.length; }
  
  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    
    out.write_u2(line_number_table_length());
    for (int i=0; i< line_number_table.length; i++)
      line_number_table[i].write(out);
  }

  /** Pretty-print this attribute.
   *  @param indent the indentation level to use.
   */
  public void print(java.io.PrintWriter pw, int indent) {
    int in=indent;
    indent(pw, in, 
	   "LineNumberTable attribute ["+line_number_table_length()+"]:");
    for (int i=0; i<line_number_table.length; i++)
      indent(pw, in+1, 
	     "#"+i+": " + line_number_table[i].toString());
  }
}
