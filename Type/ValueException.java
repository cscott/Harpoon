package harpoon.Type;

/**
 * A ValueException is thrown if we attempt to perform an illegal
 * action on a Value object.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: ValueException.java,v 1.2 1998-08-01 22:50:08 cananian Exp $
 * @see     Value
 * @see     InvalidValueException
 * @see     ValueMethodException
 */

public class ValueException extends Exception {
  /** 
   * Constructs a ValueException with no detail message. 
   */
  ValueException() { super(); }
  /** 
   * Constructs a ValueException with the specified detail message.
   * @param s the detail message.
   */
  ValueException(String s) { super(s); }
}
