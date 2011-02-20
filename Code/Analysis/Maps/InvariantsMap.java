// InvariantsMap.java, created Tue Jun 29 14:13:27 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Analysis.Loops.Loops;
import harpoon.ClassFile.HCode;

import java.util.Set;

/**
 * <code>InvariantsMap</code> is a mapping from <code>Loops</code> to a
 * <code>Set</code> of invariants.
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: InvariantsMap.java,v 1.2 2002-02-25 20:58:10 cananian Exp $
 */
public interface InvariantsMap {
    /** Returns a <code>Set</code> of invariant loop elements. */
    public Set invariantsMap(HCode hc, Loops lp);
}
