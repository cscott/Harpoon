package ClassFile;

class AttributeConstantValue extends AttributeInfo {
  int constantvalue_index;
  ConstantPoolInfo constantvalue;

  AttributeConstantValue(ClassDataInputStream in, ConstantPoolInfo cp[],
			 int ani) throws java.io.IOException {
    super(ani, in.read_u4(), cp);
    if (attribute_length != 2)
      throw new ClassDataException("ConstantValue attribute with length " +
				   attribute_length);

    constantvalue_index = in.read_u2();
    constantvalue = cp[constantvalue_index];
  }
}
