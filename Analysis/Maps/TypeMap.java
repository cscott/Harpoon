// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;

/**
 * A <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.3.2.2 1999-08-09 20:22:10 duncan Exp $
 */
public interface TypeMap  { 
    /** 
     * Return the type of a given temporary.  If the type of the temporary
     * is not known, returns null.  If the temporary represents a derived
     * pointer, an error is thrown.  If either parameter is null, an 
     * error is thrown.  
     * 
     * @param hc The <code>HCodeElement</code> containing <code>t</code>.
     * @param t The temporary to examine.
     * @return the static type of <code>t</code>.  
     */
    public HClass typeMap(HCodeElement hc, Temp t);
}
