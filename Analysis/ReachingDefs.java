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
 * @version $Id: ReachingDefs.java,v 1.2.2.1 2002-03-14 19:47:32 cananian Exp $
 */
public abstract class ReachingDefs<HCE extends HCodeElement> {
    public final static boolean TIME = false;

    /** The <code>HCode</code> for which this object contains analysis
     *  results. */
    protected final HCode<HCE> hc;
    
    /** Creates a <code>ReachingDefs</code> object for the provided
     *  <code>HCode</code>. */
    public ReachingDefs(HCode<HCE> hc) { this.hc = hc; }

    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. */
    public abstract Set<HCE> reachingDefs(HCE hce, Temp t);
}
