package harpoon.ClassFile.Raw;

/** 
 * An attribute of unknown format.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeUnknown.java,v 1.1 1998-07-31 05:51:09 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7"
 * @see Attribute
 */
class AttributeUnknown extends Attribute {
  /** Generic attribute information of unknown type. */
  byte[] info;
  
  /** Constructor. */
  AttributeUnknown(ClassFile parent, ClassDataInputStream in,
		   int attribute_name_index) throws java.io.IOException {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    info = new byte[attribute_length];
    for (int i=0; i<attribute_length; i++)
      info[i] = in.read_u1();
  }
  /** Constructor. */
  public AttributeUnknown(ClassFile parent, 
			  int attribute_name_index, byte[] info) {
    super(parent, attribute_name_index);
    this.info = info;
  }

  long attribute_length() { return info.length; }

  /** Write to bytecode stream. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    out.write(info);
  }
}
