// UncolorableGraphException.java, created Wed Jan 13 14:22:42 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Collections;
import java.util.Collection;

/**
 * <code>UnableToColorGraph</code> is a control-flow construct for
 * indicating the provided Graph Coloring algorithm failed to color a 
 * given graph.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: UnableToColorGraph.java,v 1.1.2.4 2001-06-17 22:29:39 cananian Exp $
 */

public class UnableToColorGraph extends Throwable {
    Collection rmvSuggsLarge;
    Collection rmvSuggs;

    /** returns a Collection of nodes that are suggested for
	removal to make some external graph colorable.
	No guarantees are made about the colorability of the graph
	after any nodes are removed; these are merely
	heuristically-driven hints to the catcher on how to recover. 
     */
    public Collection getRemovalSuggestions() {
	return Collections.unmodifiableCollection(rmvSuggs);
    }

    /** returns a Collection of nodes that are suggested for
	removal to make some external graph colorable.
	No guarantees are made about the colorability of the graph
	after any nodes are removed; these are merely
	heuristically-driven hints to the catcher on how to recover. 
     */
    public Collection getRemovalSuggestionsBackup() {
	return Collections.unmodifiableCollection(rmvSuggsLarge);
    }
    /** Creates a <code>UncolorableGraphException</code>. */
    public UnableToColorGraph() {
        super();
	rmvSuggsLarge = rmvSuggs = Collections.EMPTY_SET;
    }

    /** Creates a <code>UncolorableGraphException</code>. */
    public UnableToColorGraph(String s) {
        super(s);
	rmvSuggsLarge = rmvSuggs = Collections.EMPTY_SET;
    }
    
}
