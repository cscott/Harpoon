// MRA.java, created Mon Oct  1 16:42:17 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.IR.Quads.Quad;
import java.util.Set;

/**
 * <code>MRA</code> is answers the question "which 
 * <code>Temp<code>s contain the address of the most 
 * recently allocated object at this program point?"
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: MRA.java,v 1.1.2.3 2001-10-15 17:55:10 kkz Exp $
 */
public interface MRA {

    /** Returns the <code>Set</code> of <code>Temp</code>s
     *  that contain the address of the most recently
     *  allocated object at the given <code>Quad</code>.
     */
    public Set mra_before(Quad q);

}

