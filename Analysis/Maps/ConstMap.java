// ConstMap.java, created Wed Aug 19 01:05:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
/**
 * <code>ConstMap</code> is a mapping from temporaries to their constant
 * values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstMap.java,v 1.3 2002-02-25 20:58:09 cananian Exp $
 */

public interface ConstMap  {
    /** 
     * Determine whether a given temporary has a constant value at
     * the specified definition point.
     * @param hce the definition point.
     * @param t  the <code>Temp</code> to be examined.
     * @return <code>true</code> is the given definition point can
     *         be proven to give the specified <code>Temp</code> a 
     *         constant value, <code>false</code> otherwise.
     */
    public boolean isConst(HCodeElement hce, Temp t);
    /** 
     * Determine the constant value of a given temporary in the
     * context of a specific definition.
     * @param hce the definition point.
     * @param t  the temporary to be examined.
     * @return an object corresponding to the constant value of this
     *         temporary defined at this point.  Values of base types
     *         get wrapped in objects in the standard way.
     * @exception Error if <code>isConst(hce, t)</code> is false.
     */
    public Object constMap(HCodeElement hce, Temp t);
}
