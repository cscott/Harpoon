// AttributeSynthetic.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * Java 1.1 compilers are required, when producing bytecodes, to mark any
 * field or member not directly defined in the source code with an 
 * attribute names <code>Synthetic</code>.  These hidden fields and methods
 * are synthesized by the compiler in order to implement the scoping of
 * names for inner classes. <p>
 * The <code>Synthetic</code> attribute is intended to allow tools to
 * avoid displaying these methods and fields unnecessarily.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeSynthetic.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 * @see "Inner Classes Specification"
 * @see AttributeInnerClasses
 * @see Attribute
 * @see ClassFile
 */
public class AttributeSynthetic extends Attribute {
  /** Constructor. */
  AttributeSynthetic(ClassFile parent, ClassDataInputStream in,
		     int attribute_name_index) throws java.io.IOException {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    if (attribute_length != attribute_length())
      throw new ClassDataException("Synthetic attribute with length " +
				   attribute_length);
  }
  /** Constructor. */
  public AttributeSynthetic(ClassFile parent, int attribute_name_index) {
    super(parent, attribute_name_index);
  }

  public long attribute_length() { return 0; }
  
  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
  }
}
