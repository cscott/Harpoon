// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;

/**
 * A <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.3 1998-10-11 02:37:07 cananian Exp $
 */
public interface TypeMap  { 
    /** 
     * Return the type of a given temporary. 
     * @param hc The <code>HCode</code> containing <code>t</code>.
     * @param t The temporary to examine.
     * @return the static type of <code>t</code>.
     */
    public HClass typeMap(HCode hc, Temp t);
}
