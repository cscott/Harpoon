// GraphColorer.java, created Thu Jan 14 19:02:55 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.List;

/**
 * <code>GraphColorer</code> is a class for describing a graph coloring
 * algorithm.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GraphColorer.java,v 1.1.2.14 2001-06-17 22:29:38 cananian Exp $
 */

public abstract class GraphColorer  {
    
    /** Creates a <code>GraphColorer</code>. */
    public GraphColorer() { }
			       
    /** Attempts to color <code>graph</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>colors</code> is a <code>List</code> of
	          <code>Color</code>s.
	     <LI> <code>graph</code> does not have any hidden nodes.
	     </OL>
	<BR> <B>modifies:</B> <code>graph</code>
	<BR> <B>effects:</B> Attempts to color <code>graph</code>
	     using the set of <code>Color</code>s given in
	     <code>colors</code>.  If successful, every node in
	     <code>graph</code> will be present in
	     <code>graph</code>'s Node -> Color mapping, with no two 
	     interfering nodes sharing the same color.
        @throws UncolorableGraph A coloring cannot be found
	     for <code>graph</code>.  <code>graph</code> may be left
	     with some hidden nodes and an non-empty Node -> Color
	     mapping, but can be returned to its original state with a
	     call to <code>graph.resetGraph()</code>.
    */
    public abstract void color(ColorableGraph graph, 
			       List colors) throws UnableToColorGraph;

}
