// GraphColorer.java, created Thu Jan 14 19:02:55 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import java.util.Vector;

/**
 * <code>GraphColorer</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColorer.java,v 1.1.2.3 1999-01-19 16:07:59 pnkfelix Exp $
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
    
    /** Finds a coloring for <code>graph</code> producing
	<code>Color</code>s as needed from <code>factory</code>
	<BR> modifies: <code>graph</code>, <code>this.factory</code>
	<BR> effects: If <code>this.factory</code> is null, throws a
	              NoFactorySetException.  
		      Else performs a search for the mininum number of 
	              colors needed to color <code>graph</code>, and
		      colors <code>graph</code> accordingly.
    */
    public void findColoring( ColorableGraph graph ) 
	throws NoFactorySetException {
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
	<BR> modifies: <code>graph</code>, <code>this.factory</code>
	<BR> effects: First expands <code>this.factory</code> to make
	              it have the number of colors indicated by
		      <code>numCols</code>.  Then attempts to color
		      <code>graph</code> with that number of colors,
		      returning true if successful and false
		      otherwise. 
    */
    private boolean ableToColor( ColorableGraph graph, int numCols ) {
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
				  

    public abstract void color(ColorableGraph graph,
			       Vector colors ) 
	throws UncolorableGraphException;
			       

}
