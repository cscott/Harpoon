// ConstMap.java, created Wed Aug 19 01:05:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCode;
/**
 * <code>ConstMap</code> is a mapping from temporaries to their constant
 * values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMap.java,v 1.2 1998-10-11 02:37:07 cananian Exp $
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
