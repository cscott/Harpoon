package harpoon.Type;

/**
 * A ValueMethodException is thrown if we attempt to perform an
 * unsupported method on a constant value object.
 * <p>
 * An example would be attempting to initialize perform <code>add()</code>
 * on a <code>harpoon.Type.Basic.Boolean</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ValueMethodException.java,v 1.1 1998-07-29 00:56:47 cananian Exp $
 */

public class ValueMethodException extends ValueException {
  /** 
   * Constructs a ValueMethodException with no detail message. 
   */
  ValueMethodException() { super(); }
  /** 
   * Constructs a ValueMethodException with the specified detail message.
   * @param s the detail message.
   */
  ValueMethodException(String s) { super(s); }
}
