package harpoon.ClassFile.Raw.Attribute;

import harpoon.ClassFile.Raw.*;
import harpoon.ClassFile.Raw.Constant.*;
/** 
 * An attribute of unknown format.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: AttributeUnknown.java,v 1.4 1998-08-01 22:50:07 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7"
 * @see Attribute
 */
public class AttributeUnknown extends Attribute {
  /** Generic attribute information of unknown type. */
  public byte[] info;
  
  /** Constructor. */
  AttributeUnknown(ClassFile parent, ClassDataInputStream in,
		   int attribute_name_index) throws java.io.IOException {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    info = new byte[(int)attribute_length]; // XXX breaks after 31 bits.
    in.read(info);
  }
  /** Constructor. */
  public AttributeUnknown(ClassFile parent, 
			  int attribute_name_index, byte[] info) {
    super(parent, attribute_name_index);
    this.info = info;
  }

  public long attribute_length() { return info.length; }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    out.write(info);
  }
}
