// ConstMap.java, created Wed Aug 19 01:05:58 1998 by cananian
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCode;
/**
 * <code>ConstMap</code> is a mapping from temporaries to their constant
 * values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMap.java,v 1.1 1998-09-13 23:57:12 cananian Exp $
 */

public interface ConstMap  {
    /** 
     * Determine whether a given temporary has a constant value.
     * @param hc the <code>HCode</code> containing the temporary.
     * @param t  the <code>Temp</code> to be examined.
     * @return <code>true</code> is the given <code>Temp</code> can
     *         be proven to have a constant value, <code>false</code>
     *         otherwise.
     */
    public boolean isConst(HCode hc, Temp t);
    /** 
     * Determine the constant value of a given temporary. 
     * @param hc the <code>HCode</code> containing the 
     *           temporary <code>t</code>.
     * @param t  the temporary to be examined.
     * @return an object corresponding to the constant value of this
     *         temporary.  Values of base types get wrapped in objects
     *         in the standard way.
     * @exception Error if <code>isConst(hc,t)</code> is false.
     */
    public Object constMap(HCode hc, Temp t);
}
