// UncolorableGraphException.java, created Wed Jan 13 14:22:42 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>UnableToColorGraph</code> is a control-flow construct for
 * indicating the provided Graph Coloring algorithm failed to color a 
 * given graph.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: UnableToColorGraph.java,v 1.1.2.1 2000-07-25 23:25:03 pnkfelix Exp $
 */

public class UnableToColorGraph extends Throwable {
    
    /** Creates a <code>UncolorableGraphException</code>. */
    public UnableToColorGraph() {
        super();
    }

    /** Creates a <code>UncolorableGraphException</code>. */
    public UnableToColorGraph(String s) {
        super(s);
    }
    
}
