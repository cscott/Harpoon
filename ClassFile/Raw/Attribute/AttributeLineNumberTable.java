package ClassFile;

class AttributeLineNumberTable extends AttributeInfo {
  int line_number_table_length;
  LineNumberTable line_number_table[];

  AttributeLineNumberTable(ClassDataInputStream in, ConstantPoolInfo cp[],
			   int ani) throws java.io.IOException
  {
    super(ani, in.read_u4(), cp);
    
    line_number_table_length = in.read_u2();

    if (attribute_length != 2 + 4*line_number_table_length)
      throw new ClassDataException("LineNumberTable attribute with length "
				   + attribute_length);

    line_number_table = new LineNumberTable[line_number_table_length];
    for (int i=0; i<line_number_table_length; i++)
      line_number_table[i] = new LineNumberTable(in);
  }
}
