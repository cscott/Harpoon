// ReachingDefs.java, created Wed Feb  2 03:08:09 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import java.util.Set;
/**
 * <code>ReachingDefs</code> defines an abstract class for
 * analyzing reaching definitions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReachingDefs.java,v 1.2 2002-02-25 20:56:10 cananian Exp $
 */
public abstract class ReachingDefs {
    public final static boolean TIME = false;

    /** The <code>HCode</code> for which this object contains analysis
     *  results. */
    protected final HCode hc;
    
    /** Creates a <code>ReachingDefs</code> object for the provided
     *  <code>HCode</code>. */
    public ReachingDefs(HCode hc) { this.hc = hc; }

    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. */
    public abstract Set reachingDefs(HCodeElement hce, Temp t);
}
