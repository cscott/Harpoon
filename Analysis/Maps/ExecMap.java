// ExecMap.java, created Sat Sep 12 17:08:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
/**
 * <code>ExecMap</code> is a mapping from <code>HCodeElement</code>s to
 * their executable status.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ExecMap.java,v 1.2 1998-10-11 02:37:07 cananian Exp $
 */

public interface ExecMap  {
    /** 
     * Returns the executable status of an <code>HCodeElement</code>.
     * @param hc The <code>HCode</code> containing <code>node</code>.
     * @param node The <code>HCodeElement</code> to examine.
     * @return <code>true</code> if it is possible to execute this 
     *         <code>HCodeElement</code>; or <code>false</code> if
     *         it can be proved that this <code>HCodeElement</code>
     *         will never be executed. */
    public boolean execMap(HCode hc, HCodeElement node);
    /** 
     * Returns the executable status of an <code>HCodeEdge</code>.
     * @param hc The <code>HCode</code> containing <code>edge</code>.
     * @param edge An edge between two <code>HCodeElement</code>s in
     *             <code>HCode</code>.
     * @return <code>true</code> if it is possible to traverse this
     *         edge during execution, or <code>false</code> if it
     *         can be proved that this edge will never be followed.
     */
    public boolean execMap(HCode hc, HCodeEdge edge);
}
