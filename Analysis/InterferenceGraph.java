// InterferenceGraph.java, created Fri Dec  1 14:01:11 2000 by cananian
// Copyright (C) 2000  <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.Util.Grapher;

import java.util.List;
/**
 * <code>InterferenceGraph</code> is an abstract interface for
 * interference graphs.
 * 
 * @author   <cananian@alumni.princeton.edu>
 * @version $Id: InterferenceGraph.java,v 1.1.2.1 2000-12-17 18:07:39 cananian Exp $
 */
public interface InterferenceGraph extends Grapher/*<Temp>*/ {
    /* in addition to Grapher interface */
    public List/*<HCodeElement>*/ moves();
    public int spillCost(Temp t);
}
