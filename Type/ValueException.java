package harpoon.Type;

/**
 * A ValueException is thrown if we attempt to perform an illegal
 * action on a Value object.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ValueException.java,v 1.1 1998-07-29 00:56:47 cananian Exp $
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
