package ClassFile;

class AttributeExceptions extends AttributeInfo {
  int number_of_exceptions;
  int exception_index_table[];
  ConstantClass exception_table[];

  AttributeExceptions(ClassDataInputStream in, ConstantPoolInfo cp[],
		      int ani) throws java.io.IOException
  {
    super(ani, in.read_u4(), cp);

    number_of_exceptions = in.read_u2();
    
    if (attribute_length != 2*(number_of_exceptions+1))
      throw new ClassDataException("Exceptions attribute with length "
				   + attribute_length);

    exception_index_table = new int[number_of_exceptions];
    exception_table = new ConstantClass[number_of_exceptions];
    for(int i=0; i<number_of_exceptions; i++) {
      exception_index_table[i] = in.read_u2();
      exception_table[i] = (ConstantClass) cp[exception_index_table[i]];
    }
  }
}
    
