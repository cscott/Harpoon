package harpoon.ClassFile.Raw.Constant;

import harpoon.ClassFile.Raw.*;
/**
 * The <code>Constant</code> class represents a single item in
 * the constant pool of a class file.  It is a super-class for the
 * various specific constant pool item types.
 * <p>Drawn from <i>The Java Virtual Machine Specification</i>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Constant.java,v 1.10 1998-08-02 03:19:40 cananian Exp $
 * @see ConstantUtf8
 * @see ConstantInteger
 * @see ConstantFloat
 * @see ConstantLong
 * @see ConstantDouble
 * @see ConstantClass
 * @see ConstantString
 * @see ConstantFieldref
 * @see ConstantMethodref
 * @see ConstantInterfaceMethodref
 * @see ConstantNameAndType
 */
public abstract class Constant {
  /** ClassFile in which this Constant is found. */
  protected ClassFile parent;

  /* See Table 4.2 in The Java Virtual Machine Specification */
  static final int CONSTANT_Utf8=1;
  static final int CONSTANT_Integer=3;
  static final int CONSTANT_Float=4;
  static final int CONSTANT_Long=5;
  static final int CONSTANT_Double=6;
  static final int CONSTANT_Class=7;
  static final int CONSTANT_String=8;
  static final int CONSTANT_Fieldref=9;
  static final int CONSTANT_Methodref=10;
  static final int CONSTANT_InterfaceMethodref=11;
  static final int CONSTANT_NameAndType=12;

  protected Constant(ClassFile parent) { this.parent = parent; }

  /** Read a single Constant item from an input class bytecode file,
   *  and return an object instance corresponding to it.
   * @exception java.io.IOException on error reading from input stream.
   */
  static public Constant read(ClassFile p, ClassDataInputStream in) 
       throws java.io.IOException {

    int tag = in.read_u1();
    switch(tag) {
    case CONSTANT_Utf8:
      return new ConstantUtf8(p, in);
    case CONSTANT_Integer:
      return new ConstantInteger(p, in);
    case CONSTANT_Float:
      return new ConstantFloat(p, in);
    case CONSTANT_Long:
      return new ConstantLong(p, in);
    case CONSTANT_Double:
      return new ConstantDouble(p, in);
    case CONSTANT_Class:
      return new ConstantClass(p, in);
    case CONSTANT_String:
      return new ConstantString(p, in);
    case CONSTANT_Fieldref:
      return new ConstantFieldref(p, in);
    case CONSTANT_Methodref:
      return new ConstantMethodref(p, in);
    case CONSTANT_InterfaceMethodref:
      return new ConstantInterfaceMethodref(p, in);
    case CONSTANT_NameAndType:
      return new ConstantNameAndType(p, in);
    default:
      throw new Error("Unknown constant type.");
    }
  }

  /** Write a single constant pool item to a class bytecode file. 
   *  @exception java.io.IOException on error writing to output stream.
   */
  abstract public void write(ClassDataOutputStream out)
    throws java.io.IOException;

  /** Create a human-readable representation for the Constant. */
  public String toString() { 
   return "Unknown Constant";
  }

  /** Pretty-print this constant. */
  public void print(java.io.PrintWriter pw, int indent) {
    indent(pw, indent, toString());
  }
  static void indent(java.io.PrintWriter pw, int indent, String s) {
    for (int i=0; i<indent; i++)
      pw.print("  ");
    pw.println(s);
  }
}
