package ClassFile;

class AttributeSourceFile extends AttributeInfo {
  int sourcefile_index;
  String sourcefile;
  
  AttributeSourceFile(ClassDataInputStream in, ConstantPoolInfo cp[],
		      int ani) throws java.io.IOException {
    super(ani, in.read_u4(), cp);
    if (attribute_length != 2)
      throw new ClassDataException("SourceFile attribute with length "
				   + attribute_length);
    sourcefile_index = in.read_u2();
    sourcefile = ((ConstantUtf8) cp[sourcefile_index]).val;
  }
}
