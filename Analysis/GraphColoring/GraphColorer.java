// GraphColorer.java, created Thu Jan 14 19:02:55 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.List;

/**
 * <code>GraphColorer</code> is a class for describing a graph coloring
 * algorithm.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColorer.java,v 1.1.2.11 2000-07-25 23:25:03 pnkfelix Exp $
 */

public abstract class GraphColorer  {
    
    /** Creates a <code>GraphColorer</code>. */
    public GraphColorer() { }
				  
    /** Attempts to color <code>graph</code>.
	<BR> <B>requires:</B> <code>colors</code> is a
	     <code>List</code> of <code>Color</code>s  
	<BR> <B>modifies:</B> <code>graph</code>
	<BR> <B>effects:</B> Attempts to color <code>graph</code>
	     using the set of <code>Color</code>s given in
	     <code>colors</code>.  If successful, every node in
	     <code>graph</code> will be present in
	     <code>graph</code>'s Node -> Color mapping, with no two
	     interfering nodes sharing the same color.
        @throws UnableToColorGraph A coloring cannot be found
	     for <code>graph</code>.  <code>graph</code> is left with
	     no hidden nodes and an empty Node -> Color mapping.  
     */
    public abstract void color(ColorableGraphImpl graph,
			       List colors ) throws UnableToColorGraph;
			       
    /** Attempts to color <code>graph</code>.
	<BR> <B>requires:</B> <code>colors</code> is a
	     <code>List</code> of <code>Color</code>s  
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
