// ConstMap.java, created Wed Aug 19 01:05:58 1998 by cananian
package harpoon.Temp;

/**
 * <code>ConstMap</code> is a mapping from temporaries to their constant
 * values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMap.java,v 1.1 1998-08-19 05:09:06 cananian Exp $
 */

public interface ConstMap  {
    /** Determine whether a given temporary has a constant value. */
    public boolean isConst();
    /** Determine the constant value of a given temporary. */
    public Object constMap(harpoon.Temp.Temp t);
}
