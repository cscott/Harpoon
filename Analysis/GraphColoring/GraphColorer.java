// GraphColorer.java, created Thu Jan 14 19:02:55 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
import java.util.Vector;

/**
 * <code>GraphColorer</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: GraphColorer.java,v 1.1.2.1 1999-01-15 00:10:54 pnkfelix Exp $
 */

public abstract class GraphColorer  {
    
    /** Creates a <code>GraphColorer</code>. */
    public GraphColorer() {
        
    }
    
    public void findColoring( ColorableGraph graph,
			      ColorFactory factory ) {
	boolean notColored = true;
	while(notColored) {
	    try {
		color( graph, factory.getColors() );
		notColored = false;
	    } catch (UncolorableGraphException e) {
		// not enough colors; make a new one
		factory.makeColor();
	    }
	}
    }

    public abstract void color(ColorableGraph graph,
			       Vector colors ) 
	throws UncolorableGraphException;
			       

}
