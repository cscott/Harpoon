package harpoon.Util;

import java.lang.reflect.Array;
/** 
 * Miscellaneous static utility functions.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Util.java,v 1.3 1998-08-07 09:52:02 cananian Exp $
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
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
