package harpoon.Util;

import java.lang.reflect.Array;
/** 
 * Miscellaneous static utility functions.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Util.java,v 1.5 1998-09-11 15:16:33 cananian Exp $
 */
public final class Util {
  // Disable constructor.  Only static methods here.
  private Util() { }
  /**
   * Copy an array type to prevent modification.  Does not bother
   * to copy array types of length 0, because they're already immutable.
   */
  public static final Object[] copy(Object[] src) {
    if (src.length==0) return src;
    Object[] dst=(Object[])Array.newInstance(src.getClass().getComponentType(),
					     src.length);
    System.arraycopy(src,0,dst,0,src.length);
    return dst;
  }
  /** Escape the contents of a String so they are safe to print. */
  public static final String escape(String str) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isISOControl(c)) {
	String hexval=Integer.toHexString((int)c);
	while(hexval.length()<4) hexval="0"+hexval;
	sb.append('\\'); sb.append('u');
	sb.append(hexval);
      }
      else sb.append(c);
    }
    return sb.toString();
  }
  /** Repeat a given string a certain number of times.
   *  @return a string consisting of <code>s</code> repeated <code>n</code>
   *          times. */
  public static final String repeatString(String s, int n) {
    if (n==0) return "";
    if (n==1) return s;
    if ((n & 1) == 1)  // n is odd
      return repeatString(s, n-1) + s;
    else {
      String half = repeatString(s, n/2);
      return half + half;
    }
  }
  // another way of doing the same thing.
  private static final String repeatString2(String s, int n) {
    StringBuffer sb = new StringBuffer();
    for (int bit=30; bit>=0; bit--) {
      sb = sb.append(sb.toString());
      if ( (n & (1<<bit)) == 1)
	sb = sb.append(s);
    }
    return sb.toString();
  }

  /** Hacked assertion facility */
  public static final void assert(boolean val) {
    if (!val)
      throw new RuntimeException("Assertion Failure.") { };
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
