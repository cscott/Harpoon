// CheckRemoval.java, created Mon Jan 22 18:40:16 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.IR.Quads.Quad;

/**
 * <code>CheckRemoval</code> is an interface that all classes that analyze
 * RTJ <code>harpoon.IR.Quads.SET</code> or 
 * <code>harpoon.IR.Quads.ASET</code>'s for possible removal of checks should 
 * implement.
 *
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: CheckRemoval.java,v 1.1.2.4 2001-06-17 23:07:32 cananian Exp $
 */

public interface CheckRemoval {

    /** Returns true iff it is always safe to assign a.b = f; or a[b] = f; */

    public boolean shouldRemoveCheck(Quad inst);
}
