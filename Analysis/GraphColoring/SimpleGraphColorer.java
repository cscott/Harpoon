// SimpleGraphColorer.java, created Wed Jan 13 14:17:43 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import java.util.*;

/**
 * <code>SimpleGraphColorer</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: SimpleGraphColorer.java,v 1.1.2.5 1999-01-15 02:09:39 pnkfelix Exp $
 */

public class SimpleGraphColorer extends GraphColorer {

    private static final boolean DEBUG = false;

    public SimpleGraphColorer(ColorFactory factory) {
	super(factory);
    }
    
    /** Simple Graph Colorer based on algorithm given in 6.035 lecture
	( http://ceylon.lcs.mit.edu/6035/lecture18/sld064.htm ).
    */
    public final void color( ColorableGraph graph, 
			     Vector colors ) 
	throws UncolorableGraphException {
	try {
	    Stack hidden = new Stack();
	    boolean moreNodesToHide = true;
	    Enumeration nodes;
	    while( moreNodesToHide ) {
		nodes = graph.getNodes();
		moreNodesToHide = false;
		while( nodes.hasMoreElements() ) {
		    ColorableNode node = (ColorableNode) nodes.nextElement();
		    if (graph.getDegree( node ) < colors.size() ) {
			hidden.push(node);
			if (DEBUG) 
			    System.out.println
				("Pushing " + node + " of degree " +
				 graph.getDegree( node ) + " onto stack");
			graph.hideNode( node );
			
			// removing 'node' may have made previous nodes in
			// the enumeration available to be hidden.
			moreNodesToHide = true; 
		    }
		}
	    }
	    
	    // at this point, we are assured that there are no more nodes
	    // to hide.  Either the graph is finished (no nodes remain) or
	    // this algorithm can't color it with this few colors.
	    nodes = graph.getNodes();
	    if ( nodes.hasMoreElements() ) {
		// ERROR condition:
		// reset and try again
		graph.resetGraph();
		hidden = new Stack();
		if (DEBUG) 
		    System.out.println
			("Resetting Graph and attempting coloring with " + 
			 (colors.size()+1) + " colors.");
		throw new UncolorableGraphException
		    (graph + " could not be colored " + 
		     "in this manner with " +
		     colors.size() + " colors.");
	    }
	    
	    // at this point, we are assured that all of the nodes in the
	    // graph are now on the hidden stack.  Pop the nodes off the
	    // stack and color them.
	    while (! hidden.empty() ) {
		ColorableNode node = (ColorableNode) hidden.pop();
		if (DEBUG) 
		    System.out.println
			("Poping " + node + " off stack");
		// find color that none of node's neighbors is set to.
		Enumeration neighbors = graph.getNeighborsOf( node );
		Vector neighborColors = new Vector();
		while (neighbors.hasMoreElements() ) {
		    ColorableNode neighbor = 
			(ColorableNode) neighbors.nextElement();
		    try {
			neighborColors.addElement(neighbor.getColor());
		    } catch ( NodeNotColoredException e ) {
			// this shouldn't happen, because it is
			// ensured that nodes are only replaced in the
			// graph *after* they have been assigned a
			// color.  
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		    }
		}
		
		Color color = null;
		for (int i=0; i<colors.size(); i++) {
		    Color col = (Color) colors.elementAt(i);
		    boolean colorMatch = false;
		    for (int j=0; j<neighborColors.size(); j++) {
			Color nCol = 
			    (Color) neighborColors.elementAt(j);
			if (col.equals( nCol )) {
			    colorMatch = true;
			    break;
			}
		    }
		    if (! colorMatch) {
			color = col;
			break;
		    }
		}
		
		// color should be guaranteed to have been assigned at
		// this point.
		if (color == null) {
		    // ERROR condition:
		    // put all the hidden nodes back
		    graph.unhideAllNodes();
		    graph.resetColors();
		    throw new UncolorableGraphException
			("Something is flawed in the code.  " + 
			 "All nodes should have been assigned a color.");
		}
		
		try {
		    // some nodes still have their colors set...not
		    // sure if this implies an error in the code
		    // above...assuming no...
		    node.setColor(null);
		    node.setColor(color); 
		} catch (NodeAlreadyColoredException e) { 
		    // ensured that it can't be thrown here.
		    throw new RuntimeException(e.getMessage()); 
		}
		// now that it is colored, we can unhide it. 
		graph.unhideNode( node );
		
	    }
	} catch( NodeNotPresentInGraphException e ) {
	    // this should never be thrown (every node we ask
	    // 'graph' about is one that we pulled from it) 
	    e.printStackTrace();
	    throw new RuntimeException(e.getMessage());
	} catch( NodeNotRemovedException e ) {
	    // this should never be thrown (every node we unhide 
	    // in 'graph' is one we've previously hid.
	    e.printStackTrace();
	    throw new RuntimeException(e.getMessage());
	} 
    }
    
}
