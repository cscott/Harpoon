package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>KeepChecks</code> is a class that doesn't remove any checks.
 *
 * <author>Wes Beebee</author>
 */

public class KeepChecks implements CheckRemoval {
    public KeepChecks() {}
    public boolean shouldRemoveCheck(Quad inst) { return false; }
}
