// AllCheckRemoval.java, created Fri Jan 26 13:51 by wbeebee
// Copyright (C) 2000 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>AllCheckRemoval</code> is an overly aggressive (and often wrong!)
 * <code>CheckRemoval</code>: it just removes all the checks!
 * 
 * @author  Wes Beebee <wbeebee@mit.edu>
 */

public class AllCheckRemoval implements CheckRemoval, NoHeapCheckRemoval {
    
    /** Creates a <code>SimpleCheckRemoval</code>. */
    public AllCheckRemoval() { }

    /** <i>Too</i> aggressive treatment. */
    public boolean shouldRemoveCheck(Quad inst) { return true; }
    
    /** <i>Too</i> aggressive treatment. */
    public boolean shouldRemoveNoHeapWriteCheck(Quad inst) { return true; }

    /** <i>Too</i> aggressive treatment. */
    public boolean shouldRemoveNoHeapReadCheck(Quad inst) { return true; }
}
