// AttributeSourceFile.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * The <code>SourceFile</code> attribute is an optional fixed-length
 * attribute in the <code>attributes</code> table of the
 * <code>ClassFile</code> structure.  There can be no more than one
 * <code>SourceFile</code> attribute in the <code>attributes</code>
 *  table of a given <code>ClassFile</code> structure.
 * <p>
 * Only the name of the source file is given by the
 * <code>SourceFile</code> attribute.  It never represents tha name of a
 * directory containing the file or an absolute path name for the file.
 * For instance, the <code>SourceFile</code> attribute might contain the
 * file name <code>foo.java</code> but not the UNIX pathname
 * <code>/home/lindholm/foo.java</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeSourceFile.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.2"
 * @see Attribute
 * @see ClassFile
 */
public class AttributeSourceFile extends Attribute {
  /** The value of the <code>sourcefile_index</code> item must be a
      valid index into the <code>constant_pool</code> table.  The
      constant pool entry at that index must be a
      <code>CONSTANT_Utf8_info</code> structure representing the
      string giving the name of the source file from which this
      <code>class</code> file was compiled. */
  public int sourcefile_index;
  
  /** Constructor. */
  AttributeSourceFile(ClassFile parent, ClassDataInputStream in,
		      int attribute_name_index) throws java.io.IOException {
    super(parent, attribute_name_index);
    long attribute_length = in.read_u4();
    if (attribute_length != 2)
      throw new ClassDataException("SourceFile attribute with length "
				   + attribute_length);
    sourcefile_index = in.read_u2();
  }
  /** Constructor. */
  public AttributeSourceFile(ClassFile parent, int attribute_name_index,
			     int sourcefile_index) {
    super(parent, attribute_name_index);
    this.sourcefile_index = sourcefile_index;
  }
  public long attribute_length() { return 2; }

  // convenience
  public ConstantUtf8 sourcefile_index()
  { return (ConstantUtf8) parent.constant_pool[sourcefile_index]; }
  public String sourcefile() { return sourcefile_index().val; }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(attribute_name_index);
    out.write_u4(attribute_length());
    out.write_u2(sourcefile_index);
  }

  /** Pretty-print this attribute structure. 
   *  @param indent the indentation level to use.
   */
  public void print(java.io.PrintWriter pw, int indent) {
    indent(pw, indent, "SourceFile Attribute: " + 
	   sourcefile() + " {" + sourcefile_index + "}");
  }
}
