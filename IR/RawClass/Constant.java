// Constant.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>Constant</code> class represents a single item in
 * the constant pool of a class file.  It is a super-class for the
 * various specific constant pool item types.
 * <p>Drawn from <i>The Java Virtual Machine Specification</i>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Constant.java,v 1.3 2003-09-05 21:45:16 cananian Exp $
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
  public int entrySize() { return 1; } // most constants take up one entry in table

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
    ClassFile.indent(pw, indent, toString());
  }
}
