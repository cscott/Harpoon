package ClassFile;

/** 
 * This object represents a table mapping local variable indexes to
 * symbolic names.  This attribute is optional; typically it is
 * not included unless debugging flags are given to the compiler.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeLocalVariableTable.java,v 1.3 1998-03-27 05:26:32 cananian Exp $
 * @see AttributeCode
 */
public class AttributeLocalVariableTable extends AttributeInfo {
  int local_variable_table_length;
  LocalVariableTable local_variable_table[];

  AttributeLocalVariableTable(ClassDataInputStream in, ConstantPoolInfo cp[],
			      int ani) throws java.io.IOException
  {
    super(ani, in.read_u4(), cp);

    local_variable_table_length = in.read_u2();

    if (attribute_length != 2 + 10 * local_variable_table_length)
      throw new ClassDataException("LocalVariableTable with length "
				   + attribute_length);

    local_variable_table = new LocalVariableTable[local_variable_table_length];
    for (int i=0; i<local_variable_table_length; i++)
      local_variable_table[i] = new LocalVariableTable(in, cp);
  }
  /** Get the (debugging) name of a local variable.
   * @param  index the index of the local variable to query.
   * @return the name of the index'th local variable, or null
   *         if none can be found.
   */
  public String localName(int index) {
    for (int i=0; i<local_variable_table.length; i++)
      if (local_variable_table[i].index == index)
	return local_variable_table[i].name;
    return null;
  }
}
