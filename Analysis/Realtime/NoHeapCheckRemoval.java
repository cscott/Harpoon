// NoHeapCheckRemoval.java, created Wed Jan 31 16:35:49 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>NoHeapCheckRemoval</code> is an interface that all classes that
 * analyze <code>harpoon.IR.Quads.SET</code>, <code>harpoon.IR.Quads.ASET</code>
 * <code>harpoon.IR.Quads.GET</code>, <code>harpoon.IR.Quads.AGET</code>
 * for possible removal of checks for heap access in a NoHeapRealtimeThread
 * should implement.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 */

public interface NoHeapCheckRemoval {

    /** Returns true iff a.b or a[b] in a.b = f or a[b] = f cannot be a heap
     *  reference. 
     */

    public boolean shouldRemoveNoHeapWriteCheck(Quad inst);

    /** Returns true iff a.b or a[b] in f = a.b or f = a[b] cannot be a heap
     *  reference.
     */

    public boolean shouldRemoveNoHeapReadCheck(Quad inst);
}
