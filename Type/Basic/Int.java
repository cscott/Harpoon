package harpoon.Type.Basic;

/**
 * Implements the built-in <code>int</code> datatype as a user module.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: Int.java,v 1.2 1998-08-01 22:50:09 cananian Exp $
 * @see     harpoon.Type.Type
 * @see     harpoon.Type.Basic.Datatype
 */

public class Int extends Datatype {
  /** the integer value of this <code>Int</code> object. */
  private int val;

  /**
   * Constructs an <code>Int</code> object from an integer constant.
   * @param val the integer constant.
   * @exception RepresentationException 
   *            if the constant will not fit in an int.
   */
  public Int(long val) throws RepresentationException {
    if (val > Integer.MAX_VALUE)
      throw new RepresentationException("Constant "+val+" is too large.");
    if (val < Integer.MIN_VALUE)
      throw new RepresentationException("Constant "+val+" is too small.");
    this.val = (int) val;
  }
  /**
   * Constructs an <code>Int</code> object from a string representation.
   * @param s the string representing the integer constant.
   * @exception RepresentationException 
   *            if the string is malformed or cannot be represented.
   */
  public Int(String s) throws RepresentationException {
    try {
      this.val = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new RepresentationException(e.toString());
    }
  }
  /** 
   * Constructs an <code>Int</code> object from an unwrapped int.
   * <BR><STRONG>Not for external use</STRONG>.
   * @param val the integer constant.
   */
  Int(int val) { this.val = val; }

  /** Returns the integer value of this object, to allow array indexing. */
  public int intValue() { return val; }

  // BINOPS

  /** Addition operator. */
  public static Int add(Int l, Int r) { return new Int(l.val+r.val); }
  /** Subtraction operator. */
  public static Int sub(Int l, Int r) { return new Int(l.val-r.val); }
  /** Multiplication operator. */
  public static Int mul(Int l, Int r) { return new Int(l.val*r.val); }
  /** Division operator. */
  public static Int div(Int l, Int r) { return new Int(l.val/r.val); }
  /** Remainder operator. */
  public static Int mod(Int l, Int r) { return new Int(l.val%r.val); }
  
  // UNARY OPERATORS
  
  /** Unary plus operator. */
  public static Int plus(Int operand) { return new Int(+operand.val); }
  /** Unary minus operator. */
  public static Int minus(Int operand) { return new Int(-operand.val); }
  /** Bitwise complement. */
  public static Int com(Int operand) { return new Int(~operand.val); }
  // /** Logical complement. */  // NOT VALID FOR INTEGERS
  // public static Int not(Int operand) { return new Int(!operand.val); }

  // SHIFT OPERATORS

  /** Signed left shift operator. */
  public static Int lshift(Int l, Int r)  { return new Int(l.val << r.val); }
  /** Signed right shift operator. */
  public static Int rshift(Int l, Int r)  { return new Int(l.val >> r.val); }
  /** Unsigned left shift operator. */
  public static Int ulshift(Int l, Int r) { return new Int(l.val << r.val); }
  /** Unsigned right shift operator. */
  public static Int urshift(Int l, Int r) { return new Int(l.val >>>r.val); }

  // RELATIONAL OPERATORS

  /** Less-than operator. */
  public static boolean lt(Int l, Int r) { return l.val < r.val; }
  /** Greater-than operator. */
  public static boolean gt(Int l, Int r) { return l.val > r.val; }
  /** Less-than-or-equal-to operator. */
  public static boolean le(Int l, Int r) { return l.val <=r.val; }
  /** Greater-than-or-equal-to operator. */
  public static boolean ge(Int l, Int r) { return l.val >=r.val; }
  /** Equality operator. */
  public static boolean eq(Int l, Int r) { return l.val ==r.val; }
  /** Inequality operator. */
  public static boolean ne(Int l, Int r) { return l.val !=r.val; }


  /** Creates a human-readable representation of the <code>Int</code>'s value.
   * <BR>This string can be fed back to the constructor.
   */
  public String toString() { return Integer.toString(val); }
}
