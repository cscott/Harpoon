package harpoon.Type.Basic;

/**
 * Implements the built-in <code>float</code> datatype as a user module.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: Float.java,v 1.2 1998-08-01 22:50:08 cananian Exp $
 * @see     harpoon.Type.Type
 * @see     harpoon.Type.Basic.Datatype;
 */

public class Float extends Datatype {
  /** the float value of this <code>Float</code> object. */
  private float val;

  /**
   * Constructs a <code>Float</code> object from an integer constant.
   * @param val the integer constant.
   * @exception RepresentationException 
   *            if the constant will not fit in an float.
   */
  public Float(long val) throws RepresentationException {
    if (val > java.lang.Float.MAX_VALUE)
      throw new RepresentationException("Constant "+val+" is too large.");
    if (val < java.lang.Float.MIN_VALUE)
      throw new RepresentationException("Constant "+val+" is too small.");
    this.val = (float) val;
  }
  /**
   * Constructs a <code>Float</code> object from an floating-point constant.
   * @param val the floating-point constant.
   * @exception RepresentationException 
   *            if the constant will not fit in an float.
   */
  public Float(double val) throws RepresentationException {
    if (val > java.lang.Float.MAX_VALUE)
      throw new RepresentationException("Constant "+val+" is too large.");
    if (val < java.lang.Float.MIN_VALUE)
      throw new RepresentationException("Constant "+val+" is too small.");
    this.val = (float) val;
  }
  /**
   * Constructs a <code>Float</code> object from a string representation.
   * @param s the string representing the float constant.
   * @exception RepresentationException 
   *            if the string is malformed or cannot be represented.
   */
  public Float(String s) throws RepresentationException {
    try {
      this.val = (new java.lang.Float(s)).floatValue();
    } catch (NumberFormatException e) {
      throw new RepresentationException(e.toString());
    }
  }
  /** 
   * Constructs a <code>Float</code> object from an unwrapped float.
   * <BR><STRONG>Not for external use</STRONG>.
   * @param val the float constant.
   */
  Float(float val) { this.val = val; }

  // BINOPS

  /** Addition operator. */
  public static Float add(Float l, Float r) { return new Float(l.val+r.val); }
  /** Subtraction operator. */
  public static Float sub(Float l, Float r) { return new Float(l.val-r.val); }
  /** Multiplication operator. */
  public static Float mul(Float l, Float r) { return new Float(l.val*r.val); }
  /** Division operator. */
  public static Float div(Float l, Float r) { return new Float(l.val/r.val); }
  /** Remainder operator. */
  public static Float mod(Float l, Float r) { return new Float(l.val%r.val); }
  
  // UNARY OPERATORS (N/A: not,com)
  
  /** Unary plus operator. */
  public static Float plus(Float operand) { return new Float(+operand.val); }
  /** Unary minus operator. */
  public static Float minus(Float operand) { return new Float(-operand.val); }

  // SHIFT OPERATORS (N/A: lshift, rshift, ulshift, urshift)

  // RELATIONAL OPERATORS

  /** Less-than operator. */
  public static boolean lt(Float l, Float r) { return l.val < r.val; }
  /** Greater-than operator. */
  public static boolean gt(Float l, Float r) { return l.val > r.val; }
  /** Less-than-or-equal-to operator. */
  public static boolean le(Float l, Float r) { return l.val <= r.val; }
  /** Greater-than-or-equal-to operator. */
  public static boolean ge(Float l, Float r) { return l.val >= r.val; }
  /** Equality operator. */
  public static boolean eq(Float l, Float r) { return l.val == r.val; }
  /** Inequality operator. */
  public static boolean ne(Float l, Float r) { return l.val != r.val; }


  /** Creates a human-readable representation of the floating-point value.
   * <BR>This string can be fed back to the constructor.
   */
  public String toString() { return java.lang.Float.toString(val); }
}
