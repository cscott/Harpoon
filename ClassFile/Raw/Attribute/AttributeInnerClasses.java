package harpoon.ClassFile.Raw.Attribute;

import harpoon.ClassFile.Raw.*;
import harpoon.ClassFile.Raw.Constant.*;
/**
 * <code>InnerClasses</code> attributes define the encodings of
 * bytecode names of inner classes.  The attribute is stored in
 * the <code>ClassFile</code> structure.<p>
 * Each array element in the attribute records a class with an encoded
 * name, its defining scope, its simple name, and a bitmask of the
 * originally declared, untransformed access flags.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeInnerClasses.java,v 1.2 1998-08-02 05:24:21 cananian Exp $
 * @see "Inner Classes Specification"
 * @see InnerClass
 * @see Attribute
 * @see ClassFile
 */
public class AttributeInnerClasses extends Attribute {
  /** Each array element records a class with an encoded name,
   *  its defining scope, its simple name, and its originally
   *  declared access flags. */
  public InnerClass[] classes;

  /** Constructor. */
  AttributeInnerClasses(ClassFile parent, ClassDataInputStream in,
			 int attribute_name_index) throws java.io.IOException {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    
    int number_of_classes = in.read_u2();
    classes = new InnerClass[number_of_classes];
    for (int i=0; i<number_of_classes; i++)
      classes[i] = new InnerClass(parent, in);

    if (attribute_length != attribute_length())
      throw new ClassDataException("AttributeInnerClasses attribute with " + 
				   "length " + attribute_length);
  }
  /** Constructor. */
  public AttributeInnerClasses(ClassFile parent, int attribute_name_index,
				InnerClass[] classes) {
    super(parent, attribute_name_index);
    this.classes = classes;
  }
  
  public long attribute_length() { return 2+8*number_of_classes(); }
  
  // convenience.
  public int number_of_classes() { return classes.length; }
  public InnerClass classes(int i) { return classes[i]; }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    out.write_u2(number_of_classes());
    for (int i=0; i<classes.length; i++)
      classes[i].write(out);
  }

  /** Pretty-print this attribute structure. */
  public void print(java.io.PrintWriter pw, int indent) {
    int in = indent;
    indent(pw, in, "InnerClasses Attributes ["+number_of_classes()+"]:");
    for (int i=0; i<classes.length; i++) {
      indent(pw, in+1, "#"+i+": " + classes[i].toString());
    }
  }
}
