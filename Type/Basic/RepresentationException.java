package harpoon.Type.Basic;

/**
 * A RepresentationException is thrown when we attempt to instantiate a
 * datatype with a value which it cannot represent.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: RepresentationException.java,v 1.2 1998-08-01 22:50:09 cananian Exp $
 * @see     harpoon.Type.InvalidValueException
 * @see     Boolean#Boolean
 * @see     Int#Int
 * @see     Long#Long
 * @see     Float#Float
 * @see     Double#Double
 */

public class RepresentationException extends Exception {
  /** Constructs a RepresentationException with no detail message. */
  RepresentationException() { super(); }
  /** Constructs a RepresentationException with the specified detail message.
   *  @param s the detail message.
   */
  RepresentationException(String s) { super(s); }
}
