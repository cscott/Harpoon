// GraphColorer.java, created Thu Jan 14 19:02:55 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Vector;

/**
 * <code>GraphColorer</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColorer.java,v 1.1.2.6 1999-08-04 05:52:21 cananian Exp $
 */

public abstract class GraphColorer  {
    
    private ColorFactory factory;


    /** Creates a <code>GraphColorer</code>. */
    public GraphColorer() {
        this.factory = null;
    }

    /** Creates a <code>GraphColorer</code>, setting its
	<code>ColorFactory</code> to <code>factory</code> 
    */  
    public GraphColorer(ColorFactory factory) {
        this.factory = factory;
    }
    
    /** Finds a coloring for <code>graph</code>.
	<BR> <B>requires:</B> <code>this</code> was passed a
	                      <code>ColorFactory</code> at
			      construction. 
	<BR> <B>modifies:</B> <code>graph</code>, <code>this</code>
	<BR> <B>effects:</B> Performs a search for a near-minimum
	                     number of colors needed to color
			     <code>graph</code>, producing
			     <code>Color</code>s as needed from the
			     <code>ColorFactory</code> associated with
			     <code>this</code>.  Once an appopriate
			     set is found, colors <code>graph</code>
			     accordingly.
    */
    public void findColoring( ColorableGraph graph ) {
	// modifies: this.factory
	if (factory == null) {
	    throw new NoFactorySetException
		("Cannot perform unbounded color searching with " + 
		 "GraphColorer unless you construct it with a ColorFactory."); 
	}
	boolean notColored = true;
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
	
	// some of the arithimetic and control flow above is kooky, so
	// I'm adding a last check to ensure that things do work.  
	if (!ableToColor( graph, upperBound )) {
	    throw new RuntimeException
		("Something went horribly wrong with the color search");
	} 
	
    }

    /** Attempts to color <code>graph</code> using only
	<code>numCols</code> colors.
	<BR> <B>modifies:</B> <code>graph</code>, <code>this</code>
	<BR> <B>effects:</B> First expands or reduces the inventory of
	                     the <code>ColorFactory</code> associated
			     with <code>this</code> to make it have
			     the number of colors indicated by
			     <code>numCols</code>.  Then attempts to
			     color <code>graph</code> with that number
			     of colors, returning true if successful
			     and false otherwise.
    */
    private boolean ableToColor( ColorableGraph graph, int numCols ) {
	// modifies: this.factory
	boolean colored = false;
	while (numCols != factory.getColors().size()) {
	    if (numCols < factory.getColors().size()) {
		factory.removeColor();
	    } else {
		factory.makeColor();
	    }
	}
	try {
	    color( graph, factory.getColors() );
	    colored = true;
	} catch ( UncolorableGraphException e ) {
	}
	return colored;
    }
				  
    /** Attempts to color <code>graph</code>.
	<BR> <B>requires:</B> <code>colors</code> is a
	                      <code>Vector</code> of
			      <code>Color</code>s  
	<BR> <B>modifies:</B> <code>graph</code>
	<BR> <B>effects:</B> Attempts to color <code>graph</code>
	                     using the set of <code>Color</code>s
			     given in <code>colors</code>.  If a
			     coloring cannot be found for
			     <code>graph</code>, throws
			     <code>UncolorableGraphException</code>. 
     */
    public abstract void color(ColorableGraph graph,
			       Vector colors ) 
	throws UncolorableGraphException;
			       

}
