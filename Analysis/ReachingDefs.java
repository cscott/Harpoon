// ReachingDefs.java, created Wed Feb  2 03:08:09 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import java.util.Set;
/**
 * <code>ReachingDefs</code> defines an interface for
 * analyzing reaching definitions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReachingDefs.java,v 1.1.2.2 2000-02-02 08:30:28 cananian Exp $
 */
public interface ReachingDefs {
    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. */
    public Set reachingDefs(HCodeElement hce, Temp t);
}
