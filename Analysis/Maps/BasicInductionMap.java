// BasicInductionMap.java, created Tue Jun 29 14:13:27 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Analysis.Loops.Loops;
import harpoon.ClassFile.HCode;

import java.util.Set;


/**
 * <code>BasicInductionMap</code> is a mapping from <code>Loops</code> to a
 * <code>Set</code> of basic induction <code>Temp</code>s.
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: BasicInductionMap.java,v 1.1.2.1 1999-06-29 18:29:50 bdemsky Exp $
 */
public interface BasicInductionMap {
    /** Returns a <code>Set</code> of basic induction <code>Temp</code>s. */
    public Set basicInductionMap(HCode hc, Loops lp);
}
