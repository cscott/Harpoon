// UnboundedGraphColorer.java, created Mon Jul 24 10:56:07 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.List;

/**
 * <code>UnboundedGraphColorer</code> uses the graph coloring strategy
 * provided by another <code>GraphColorer</code> to search for a near
 * minimum number of colors required.  It generates colors dynamically
 * using a <code>ColorFactory</code>.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: UnboundedGraphColorer.java,v 1.1.2.6 2001-06-17 22:29:39 cananian Exp $
 */
public class UnboundedGraphColorer extends GraphColorer {

    private ColorFactory factory;
    private GraphColorer colorer;

    /** Creates a <code>UnboundedGraphColorer</code>. 
	<BR> <B>effects:</B> Constructs an UnboundedGraphColorer that
	     will use <code>colorer</code> for its coloring strategy
	     and <code>cf</code> to produce the set of colors used in
	     the coloring.
     */
    public UnboundedGraphColorer(GraphColorer colorer, ColorFactory cf) {
        super();
	this.factory = cf;
	this.colorer = colorer;
    }

    /** Finds a coloring for <code>graph</code>.
	<BR> <B>modifies:</B> <code>graph</code>, <code>this</code>
	<BR> <B>effects:</B> Performs a search for a near-minimum
	     number of colors needed to color <code>graph</code>,
	     producing <code>Color</code>s as needed from the 
	     <code>ColorFactory</code> associated with
	     <code>this</code>.  Once an appopriate set is found,
	     colors <code>graph</code> accordingly.
    */
    public void findColoring( ColorableGraph graph ) {
	// modifies: this.factory
	int upperBound = 1; // upperBound is inclusive
	
	while (! ableToColor( graph, upperBound ) ) {
	    upperBound *= 2;
	}
	
	// managed to color the graph; now find minimum colors
	// required.
	int lowerBound = upperBound / 2; // lowerBound is noninclusive
	
	while (upperBound - lowerBound > 1) {
	    int trial = lowerBound + (upperBound - lowerBound)/2;
	    if (ableToColor( graph, trial)) {
		upperBound = trial;
	    } else {
		lowerBound = trial;
	    }
	}

	// final call to ableToColor to properly set factory size.
	ableToColor(graph, upperBound);
	
	try {
	    color( graph, factory.getColors() );
	} catch ( UnableToColorGraph u ) {
	    throw new RuntimeException
		("Something went horribly wrong with the color search");
	} 
    }

    /** Attempts to color <code>graph</code> using only
	<code>numCols</code> colors.
	<BR> <B>modifies:</B> <code>graph</code>, <code>this</code>
	<BR> <B>effects:</B> First expands or reduces the inventory of
	     the <code>ColorFactory</code> associated with
	     <code>this</code> to make it have the number of colors
	     indicated by <code>numCols</code>.  Then attempts to
	     color <code>graph</code> with that number of colors,
	     returning true if successful and false otherwise.
    */
    private boolean ableToColor( ColorableGraph graph, int numCols ) {
	// modifies: this.factory
	if (numCols < factory.getColors().size()) {
	    while (numCols < factory.getColors().size()) {
		factory.removeColor();
	    } 
	} else if (numCols > factory.getColors().size()) {
	    while (numCols > factory.getColors().size()) {
		factory.makeColor();
	    }
	}

	try {
	    color( graph, factory.getColors() );
	    graph.resetColors();
	    return true;
	} catch ( UnableToColorGraph e ) {
	    graph.replaceAll();
	    graph.resetColors();
	    return false;
	}
    }

    public final void color(ColorableGraph graph, List colors) 
	throws UnableToColorGraph {
	colorer.color(graph, colors);
    }
}
