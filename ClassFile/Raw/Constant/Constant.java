package harpoon.ClassFile.Raw;

/**
 * The <code>Constant</code> class represents a single item in
 * the constant pool of a class file.  It is a super-class for the
 * various specific constant pool item types.
 * <p>Drawn from <i>The Java Virtual Machine Specification</i>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Constant.java,v 1.5 1998-07-31 05:51:09 cananian Exp $
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
public class Constant {
  /** ClassFile in which this Constant is found. */
  public ClassFile parent;

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

  /** Read a single ConstantPoolInfo item from an input class bytecode file,
   *  and return an object instance corresponding to it.
   * @exception java.io.IOException on error reading from input stream.
   */
  static Constant read(ClassFile p, ClassDataInputStream in) 
       throws java.io.IOException {
    this.p = parent;

    int tag = in.read_u1();
    switch(tag) {
    case CONSTANT_Utf8:
      return new ConstantUtf8(in);
    case CONSTANT_Integer:
      return new ConstantInteger(in);
    case CONSTANT_Float:
      return new ConstantFloat(in);
    case CONSTANT_Long:
      return new ConstantLong(in);
    case CONSTANT_Double:
      return new ConstantDouble(in);
    case CONSTANT_Class:
      return new ConstantClass(in);
    case CONSTANT_String:
      return new ConstantString(in);
    case CONSTANT_Fieldref:
      return new ConstantFieldref(in);
    case CONSTANT_Methodref:
      return new ConstantMethodref(in);
    case CONSTANT_InterfaceMethodref:
      return new ConstantInterfaceMethodref(in);
    case CONSTANT_NameAndType:
      return new ConstantNameAndType(in);
    default:
      throw new Error("Unknown constant type.");
    }
  }

  /** Write a single constant pool item to a class bytecode file. 
   *  @exception java.io.IOException on error writing to output stream.
   */
  abstract void write(ClassDataOutputStream out)
    throws java.io.IOException;
}
