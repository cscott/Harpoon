package ClassFile;

class AttributeLocalVariableTable extends AttributeInfo {
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
}
