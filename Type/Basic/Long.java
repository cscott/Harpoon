package harpoon.Type.Basic;

/**
 * Implements the built-in <code>long</code> datatype as a user module.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Long.java,v 1.3 1998-08-01 22:55:18 cananian Exp $
 * @see     harpoon.Type.Type
 * @see     harpoon.Type.Basic.Datatype
 */

public class Long extends Datatype {
  /** the long value of this <code>Long</code> object. */
  private long val;

  /**
   * Constructs a <code>Long</code> object from an long constant.
   * @param val the long constant.
   */
  public Long(long val) {
    this.val = val;
  }
  /**
   * Constructs a <code>Long</code> object from a string representation.
   * @param s the string representing the long constant.
   * @exception RepresentationException 
   *            if the string is malformed or cannot be represented.
   */
  public Long(String s) throws RepresentationException {
    try {
      this.val = java.lang.Long.parseLong(s);
    } catch (NumberFormatException e) {
      throw new RepresentationException(e.toString());
    }
  }

  /** 
   * Returns the integer value of this object, to allow array indexing. 
   * @exception RepresentationException 
   *            if the current value does not fit in an integer.
   */
  public int intValue() throws RepresentationException {
    if (val > Integer.MAX_VALUE ||
	val < Integer.MIN_VALUE)
      throw new RepresentationException("Value overflows "+
					"integer representation.");
    return (int)val;
  }

  // BINOPS

  /** Addition operator. */
  public static Long add(Long l, Long r) { return new Long(l.val+r.val); }
  /** Subtraction operator. */
  public static Long sub(Long l, Long r) { return new Long(l.val-r.val); }
  /** Multiplication operator. */
  public static Long mul(Long l, Long r) { return new Long(l.val*r.val); }
  /** Division operator. */
  public static Long div(Long l, Long r) { return new Long(l.val/r.val); }
  /** Remainder operator. */
  public static Long mod(Long l, Long r) { return new Long(l.val%r.val); }
  
  // UNARY OPERATORS
  
  /** Unary plus operator. */
  public static Long plus(Long operand) { return new Long(+operand.val); }
  /** Unary minus operator. */
  public static Long minus(Long operand) { return new Long(-operand.val); }
  /** Bitwise complement. */
  public static Long com(Long operand) { return new Long(~operand.val); }

  // SHIFT OPERATORS

  /** Signed left shift operator. */
  public static Long lshift(Long l, Long r) { return new Long(l.val << r.val);}
  /** Signed right shift operator. */
  public static Long rshift(Long l, Long r) { return new Long(l.val >> r.val);}
  /** Unsigned left shift operator. */
  public static Long ulshift(Long l, Long r){ return new Long(l.val << r.val);}
  /** Unsigned right shift operator. */
  public static Long urshift(Long l, Long r){ return new Long(l.val >>>r.val);}

  // RELATIONAL OPERATORS

  /** Less-than operator. */
  public static boolean lt(Long l, Long r){ return l.val < r.val;}
  /** Greater-than operator. */
  public static boolean gt(Long l, Long r){ return l.val > r.val;}
  /** Less-than-or-equal-to operator. */
  public static boolean le(Long l, Long r){ return l.val <=r.val;}
  /** Greater-than-or-equal-to operator. */
  public static boolean ge(Long l, Long r){ return l.val >=r.val;}
  /** Equality operator. */
  public static boolean eq(Long l, Long r){ return l.val ==r.val;}
  /** Inequality operator. */
  public static boolean ne(Long l, Long r){ return l.val !=r.val;}


  /** Creates a human-readable representation of the object's value.
   * <BR>This string can be fed back to the constructor.
   */
  public String toString() { return java.lang.Long.toString(val); }
}
