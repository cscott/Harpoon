package harpoon.Util;

import java.lang.reflect.Array;
/** 
 * Miscellaneous static utility functions.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Util.java,v 1.2 1998-08-03 07:19:41 cananian Exp $
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
}
