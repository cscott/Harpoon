package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>CheckRemoval</code> is an interface that all classes that analyze
 * RTJ <code>harpoon.IR.Quads.SET</code> or <code>harpoon.IR.Quads.ASET</code>'s for possible 
 * removal of checks should implement.
 *
 * <author>Wes Beebee</author>
 */

public interface CheckRemoval {
    /** Returns true iff it is always safe to assign a.b = f; for
     *  the given <code>harpoon.IR.Quads.SET</code> or <code>harpoon.IR.Quads.ASET</code> inst. 
     */
      
    public boolean shouldRemoveCheck(Quad inst);
}
