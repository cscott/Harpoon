package harpoon.ClassFile.Raw;

/** 
 * Specialized IOException class to handle malformed class files.
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: ClassDataException.java,v 1.7 1998-08-01 22:50:06 cananian Exp $
 */
public class ClassDataException extends java.io.IOException {
  public ClassDataException() { super(); }
  public ClassDataException(String s) { super(s); }
}
