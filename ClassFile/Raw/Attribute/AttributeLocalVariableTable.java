package harpoon.ClassFile.Raw.Attribute;

import harpoon.ClassFile.Raw.*;
import harpoon.ClassFile.Raw.Constant.*;
/** 
 * The <code>LocalVariableTable</code> attribute is an optional
 * variable-length attribute of a <code>Code</code> attribute.  It may
 * be used by debuggers to determine the value of a given local variable
 * during the execution of a method.  If <code>LocalVariableTable</code>
 * attributes are present in the <code>attributes</code> table of a
 * given <code>Code</code> attribute, then they may appear in any
 * order.  There may be no more than one <code>LocalVariableTable</code>
 * attribute per local variable in the <code>Code</code> attribute.
 * <p>
 * This object represents a table mapping local variable indexes to
 * symbolic names.  This attribute is optional; typically it is
 * not included unless debugging flags are given to the compiler.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: AttributeLocalVariableTable.java,v 1.9 1998-08-01 22:50:07 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.7"
 * @see AttributeCode
 * @see Attribute
 */
public class AttributeLocalVariableTable extends Attribute {
  /** Each entry in the <code>local_variable_table</code> array
      indicates a range of <code>code</code> offsets within which a
      local variable has a value.  It also indicates the index into
      the local variables of the current frame at which that local
      variable can be found. */
  public LocalVariableTable local_variable_table[];

  /** Constructor. */
  AttributeLocalVariableTable(ClassFile parent, ClassDataInputStream in,
			      int attribute_name_index) 
    throws java.io.IOException
  {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();

    int local_variable_table_length = in.read_u2();
    local_variable_table = new LocalVariableTable[local_variable_table_length];
    for (int i=0; i<local_variable_table_length; i++)
      local_variable_table[i] = new LocalVariableTable(in);

    if (attribute_length != attribute_length())
      throw new ClassDataException("LocalVariableTable with length "
				   + attribute_length);
  }
  /** Constructor. */
  public AttributeLocalVariableTable(ClassFile parent,int attribute_name_index,
				     LocalVariableTable local_variable_table[])
  {
    super(parent, attribute_name_index);
    this.local_variable_table = local_variable_table;
  }

  public long attribute_length() { return 2 + 10*local_variable_table_length(); }

  // Convenience.
  public int local_variable_table_length() 
  { return local_variable_table.length; }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());

    out.write_u2(local_variable_table_length());
    for (int i=0; i< local_variable_table.length; i++)
      local_variable_table[i].write(out);
  }

  /** Get the (debugging) name of a local variable.
   * @param  index the index of the local variable to query.
   * @return the name of the index'th local variable, or null
   *         if none can be found.
   */
  public String localName(int index) {
    for (int i=0; i<local_variable_table.length; i++)
      if (local_variable_table[i].index == index)
	return local_variable_table[i].name();
    return null;
  }

  /*****************************************************************/
  // INNER CLASS: LineNumberTable
  /*****************************************************************/

  /** Each object indicates a range of <code>code</code> array offsets
      within which a local variable has a value. */
  public class LocalVariableTable {
    /** The given local variable must have a value at indices into the
	<code>code</code> array in the closed interval
	<code>[start_pc, start_pc + length]</code>.
	<p> The value of <code>start_pc</code> must be a valid index
	into the <code>code</code> array of this <code>Code</code>
	attribute of the opcode of an instruction. */
    public int start_pc;
    /** The given local variable must have a value at indices into the
	<code>code</code> array in the closed interval
	<code>[start_pc, start_pc + length]</code>.
	<p> The value of <code>start_pc+length</code> must be either a 
	valid index into the <code>code</code> array of this
	<code>Code</code> attribute of the opcode of an instruction,
	or the first index beyond the end of that <code>code</code>
	array.  */
    public int length;
    /** The value of the <code>name_index</code> item must be a valid
	index into the <code>constant_pool</code> table.  The
	<code>constant_pool</code> entry at that index must contain a
	<code>CONSTANT_Utf8_info</codE> structure representing a valid
	Java local variable name stored as a simple name. */
    public int name_index;
    /** The value of the <code>descriptor_index</code> item must be a
	valid index into the <code>constant_pool</code> table.  The
	<code>constant_pool</code> entry at that index must contain a
	<code>CONSTANT_Utf8_info</code> structure representing a valid
	descriptor for a Java local variable.  Java local variable
	descriptors have the same form as field descriptors. */
    public int descriptor_index;
    /** The given local variable must be at <code>index</code> in its
	method's local variables.  If the local variable at
	<code>index</code> is a two-word type (<code>double</code> or
	<code>long</code>), it occupies both <code>index</code> and
	<code>index+1</code>. */
    public int index;

    /** Constructor. */
    LocalVariableTable(ClassDataInputStream in) throws java.io.IOException {
      start_pc = in.read_u2();
      length   = in.read_u2();
      
      name_index       = in.read_u2();
      descriptor_index = in.read_u2();
      
      index = in.read_u2();
    }
    /** Constructor. */
    public LocalVariableTable(int start_pc, int length,
			      int name_index, int descriptor_index,
			      int index) {
      this.start_pc = start_pc;
      this.length = length;
      this.name_index = name_index;
      this.descriptor_index = descriptor_index;
      this.index = index;
    }
    /** Writes to bytecode stream. */
    public void write(ClassDataOutputStream out) throws java.io.IOException {
      out.write_u2(start_pc);
      out.write_u2(length);

      out.write_u2(name_index);
      out.write_u2(descriptor_index);
      
      out.write_u2(index);
    }
    // convenience functions.
    public ConstantUtf8 name_index()
    { return (ConstantUtf8) parent.constant_pool[name_index]; }
    public ConstantUtf8 descriptor_index()
    { return (ConstantUtf8) parent.constant_pool[descriptor_index]; }

    public String name() { return name_index().val; }
    public String descriptor() { return descriptor_index().val; }
  }
}
