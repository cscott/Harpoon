package harpoon.Type.Basic;

/**
 * Implements the built-in <code>double</code> datatype as a user module.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Double.java,v 1.1 1998-07-29 00:56:48 cananian Exp $
 * @see     harpoon.Type.Type
 * @see	    harpoon.Type.Basic.Datatype;
 */

public class Double extends Datatype {
  /** the double value of this <code>Double</code> object. */
  private double val;

  /**
   * Constructs a <code>Double</code> object from an integer constant.
   * @param val the integer constant.
   * @exception RepresentationException 
   *            if the constant will not fit in an double.
   */
  public Double(long val) throws RepresentationException {
    if (val > java.lang.Double.MAX_VALUE)
      throw new RepresentationException("Constant "+val+" is too large.");
    if (val < java.lang.Double.MIN_VALUE)
      throw new RepresentationException("Constant "+val+" is too small.");
    this.val = (double) val;
  }
  /**
   * Constructs a <code>Double</code> object from an floating-point constant.
   * @param val the floating-point constant.
   */
  public Double(double val) {
    this.val = val;
  }
  /**
   * Constructs a <code>Double</code> object from a string representation.
   * @param s the string representing the double constant.
   * @exception RepresentationException 
   *            if the string is malformed or cannot be represented.
   */
  public Double(String s) throws RepresentationException {
    try {
      this.val = (new java.lang.Double(s)).doubleValue();
    } catch (NumberFormatException e) {
      throw new RepresentationException(e.toString());
    }
  }

  // BINOPS

  /** Addition operator. */
  public static Double add(Double l, Double r) 
  { return new Double(l.val+r.val); }
  /** Subtraction operator. */
  public static Double sub(Double l, Double r) 
  { return new Double(l.val-r.val); }
  /** Multiplication operator. */
  public static Double mul(Double l, Double r) 
  { return new Double(l.val*r.val); }
  /** Division operator. */
  public static Double div(Double l, Double r) 
  { return new Double(l.val/r.val); }
  /** Remainder operator. */
  public static Double mod(Double l, Double r) 
  { return new Double(l.val%r.val); }
  
  // UNARY OPERATORS (N/A: not,com)
  
  /** Unary plus operator. */
  public static Double plus(Double operand) 
  { return new Double(+operand.val); }
  /** Unary minus operator. */
  public static Double minus(Double operand) 
  { return new Double(-operand.val); }

  // SHIFT OPERATORS (N/A: lshift, rshift, ulshift, urshift)

  // RELATIONAL OPERATORS

  /** Less-than operator. */
  public static boolean lt(Double l, Double r) { return l.val < r.val; }
  /** Greater-than operator. */
  public static boolean gt(Double l, Double r) { return l.val > r.val; }
  /** Less-than-or-equal-to operator. */
  public static boolean le(Double l, Double r) { return l.val <= r.val; }
  /** Greater-than-or-equal-to operator. */
  public static boolean ge(Double l, Double r) { return l.val >= r.val; }
  /** Equality operator. */
  public static boolean eq(Double l, Double r) { return l.val == r.val; }
  /** Inequality operator. */
  public static boolean ne(Double l, Double r) { return l.val != r.val; }


  /** Creates a human-readable representation of the floating-point value.
   * <BR>This string can be fed back to the constructor.
   */
  public String toString() { return java.lang.Double.toString(val); }
}
