// BasicInductionsMap.java, created Tue Jun 29 14:13:27 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Analysis.Loops.Loops;
import harpoon.ClassFile.HCode;

import java.util.HashMap;


/**
 * <code>BasicInductionsMap</code> is a mapping from <code>Loops</code> to a
 * <code>Set</code> of basic induction <code>Temp</code>s.
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: BasicInductionsMap.java,v 1.1.2.2 1999-07-01 19:23:00 bdemsky Exp $
 */
public interface BasicInductionsMap {
    /** Returns a <code>Set</code> of basic induction <code>Temp</code>s. */
    public HashMap basicInductionsMap(HCode hc, Loops lp);
}
