// ConstMap.java, created Wed Aug 19 01:05:58 1998 by cananian
package harpoon.Temp;

import harpoon.ClassFile.HMethod;
/**
 * <code>ConstMap</code> is a mapping from temporaries to their constant
 * values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMap.java,v 1.3 1998-09-11 13:12:50 cananian Exp $
 */

public interface ConstMap  {
    /** Determine whether a given temporary has a constant value. */
    public boolean isConst(HMethod m, Temp t);
    /** Determine the constant value of a given temporary. */
    public Object constMap(HMethod m, Temp t);
}
