package harpoon.ClassFile.Raw;

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
 * @version $Id: AttributeLineNumberTable.java,v 1.4 1998-07-31 05:51:09 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.6"
 * @see AttributeCode
 * @see Attribute
 */
class AttributeLineNumberTable extends Attribute {
  /** Each entry in the <code>line_number_table</code> array indicates
      that the line number in the original Java source file changes at
      a given point in the <code>code</code> array. */
  LineNumberTable line_number_table[];

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
  }

  /** Constructor. */
  public AttributeLineNumberTable(ClassFile parent, int attribute_name_index,
				  LineNumberTable line_number_table[]) {
    super(parent, attribute_name_index);
    this.line_number_table = line_number_table;
  }

  long attribute_length() { return 2 + 4*line_number_table_length(); }

  // Convenience.
  int line_number_table_length() { return line_number_table.length; }
  
  /** Write to bytecode stream. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    
    out.write_u2(line_number_table_length());
    for (int i=0; i< line_number_table.length; i++)
      line_number_table[i].write(out);
  }
  
  /**************************************************************/
  // INNER CLASS: LineNumberTable
  /**************************************************************/

  /** Each object indicates that the line number in the original Java
      source file changes at a given point in the <code>code</code>
      array. */ 
  class LineNumberTable {
    /** The value of the <code>start_pc</code> item must indicate the
	index into the <code>code</code> array at which the code for a
	new line in the original Java source file begins.  The value
	of <code>start_pc</code> must be less than the value of the
	<code>code_length</code> item of the <code>Code</code>
	attribute of which this <code>LineNumberTable</code> is an
	attribute. */
    int start_pc;
    /** The value of the <code>line_number</code> item must give the
	corresponding line number in the original Java source file. */
    int line_number;

    /** Constructor. */
    LineNumberTable(ClassDataInputStream in) throws java.io.IOException {
      start_pc = in.read_u2();
      line_number = in.read_u2();
    }
    /** Constructor. */
    public LineNumberTable(int start_pc, int line_number) {
      this.start_pc = start_pc;
      this.line_number = line_number;
    }
    /** Writes to bytecode stream. */
    void write(ClassDataOutputStream out) throws java.io.IOException {
      out.write_u2(start_pc);
      out.write_u2(line_number);
    }
  }
}
