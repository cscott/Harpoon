// ReachingDefs.java, created Wed Feb  2 03:08:09 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

/**
 * <code>ReachingDefs</code> defines an abstract class for
 * analyzing reaching definitions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReachingDefs.java,v 1.1.2.1 2000-02-02 08:19:15 cananian Exp $
 */
public abstract class ReachingDefs {
    /** The <code>HCode</code> for which this object contains analysis
     *  results. */
    protected final HCode hc;
    
    /** Creates a <code>ReachingDefs</code> object for the provided
     *  <code>HCode</code>. */
    public ReachingDefs(HCode hc) { this.hc = hc; }

    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. */
    public abstract Set reachingDef(HCodeElement hce, Temp t);
}
