package harpoon.Type;

/**
 * An InvalidValueException is thrown if we attempt to initialize a
 * user-defined datatype with a constant value which exceeds the 
 * representational capacity of that type.
 * <p>
 * An example would be attempting to initialize a <code>byte</code> with
 * the value 256.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InvalidValueException.java,v 1.1 1998-07-29 00:56:46 cananian Exp $
 * @see     BaseValue#BaseValue
 * @see     BaseType#newValue
 */

public class InvalidValueException extends ValueException {
  /** 
   * Constructs an InvalidValueException with no detail message. 
   */
  InvalidValueException() { super(); }
  /** 
   * Constructs an InvalidValueException with the specified detail message.
   * @param s the detail message.
   */
  InvalidValueException(String s) { super(s); }
}
