// InnerClass.java, created Mon Jan 18 22:44:38 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * Each <code>InnerClass</code> object describes one encoded inner
 * class name, its defining scope, its simple name, and a bitmask
 * of the originally declared, untransformed access flags.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InnerClass.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see "Inner Classes Specification"
 * @see AttributeInnerClasses
 */
public class InnerClass {
  /** ClassFile in which this attribute information is found. */
  protected ClassFile parent;

  /** <code>CONSTANT_Class_info</code> pointer to class with encoded name. */
  public int inner_class_info_index;
  /** <code>CONSTANT_Class_info</code> pointer to the defining scope for 
   *  the inner class.
   *  If the inner class is not a member (of an outer class), then
   *  the <code>outer_class_info_index</code> is zero. */
  public int outer_class_info_index;
  /** <code>CONSTANT_Utf8_info</code> pointer to the simple name of the 
   *  encoded inner class. 
   *  If the inner class is anonymous, its <code>inner_name_index</code>
   *  is zero. <p>
   *  <STRONG>ERRATA</STRONG>: although the specification dictates the
   *  above, current compilers seem to generate a pointer to a name
   *  string of <code>""</code> (that is, a zero-length string) instead.
   */
  public int inner_name_index;
  /** Originally declared, untransformed <code>access_flags</code>. */
  public AccessFlags inner_class_access_flags;

  /** Constructor. */
  InnerClass(ClassFile parent, ClassDataInputStream in)
       throws java.io.IOException 
  {
    this.parent = parent;

    inner_class_info_index = in.read_u2();
    outer_class_info_index = in.read_u2();
    inner_name_index       = in.read_u2();
    inner_class_access_flags = new AccessFlags(in);
  }

  /** Constructor. */
  public InnerClass(ClassFile parent, 
		    int inner_class_info_index, 
		    int outer_class_info_index, 
		    int inner_name_index, 
		    AccessFlags inner_class_access_flags) {
    this.parent = parent;

    this.inner_class_info_index = inner_class_info_index;
    this.outer_class_info_index = outer_class_info_index;
    this.inner_name_index       = inner_name_index;
    this.inner_class_access_flags = inner_class_access_flags;
  }

  /** Write to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(inner_class_info_index);
    out.write_u2(outer_class_info_index);
    out.write_u2(inner_name_index);
    inner_class_access_flags.write(out);
  }

  // convenience
  public ConstantClass inner_class_info()
  { return (ConstantClass) parent.constant_pool[inner_class_info_index]; }
  public ConstantClass outer_class_info() {
    if (outer_class_info_index==0) return null;
    return (ConstantClass) parent.constant_pool[outer_class_info_index]; 
  }
  public ConstantUtf8  inner_name_index() {
    if (inner_name_index==0) return null;
    return (ConstantUtf8)  parent.constant_pool[inner_name_index]; 
  }
  public String inner_name() { 
    if (inner_name_index==0) return null;
    return inner_name_index().val; 
  }

  /** Human-readable representation. */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("InnerClass: ");
    sb.append(inner_class_info().name());
    sb.append(" {"+inner_class_info_index+"}");
    if (outer_class_info_index!=0) {
      sb.append(" in " + outer_class_info().name());
      sb.append(" {"+outer_class_info_index+"}");
    }
    sb.append(", originally ");
    sb.append(inner_class_access_flags.toString());
    sb.append(" ");
    if (inner_name()==null || inner_name().equals(""))
      sb.append("<anonymous>");
    else
      sb.append(inner_name());
    sb.append(" {"+inner_name_index+"}");
    return sb.toString();
  }
}
