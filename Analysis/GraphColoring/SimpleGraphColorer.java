// SimpleGraphColorer.java, created Wed Jan 13 14:17:43 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;

import harpoon.Util.Util;

/**
 * <code>SimpleGraphColorer</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: SimpleGraphColorer.java,v 1.1.2.14 2000-07-28 03:06:46 pnkfelix Exp $
 */

public class SimpleGraphColorer extends GraphColorer {

    private static final boolean DEBUG = false;

    public SimpleGraphColorer() { }
    
    /** colors <code>graph</code> using the "progressively remove
	nodes with degree less than X" heuristic, where X is the
	number of colors provided.  This is a very conservative
	heuristic; several improvements upon it are available for
	development.  
	( http://ceylon.lcs.mit.edu/6035/lecture18/sld064.htm ).
    */
    public final void color(ColorableGraph graph, List colors) 
	throws UnableToColorGraph {
	// System.out.println("entered color("+graph+", "+colors+")");

	boolean moreNodesToHide = false;
	do {
	    moreNodesToHide = false;
	    
	    // make new copy of nodeSet (can't modify graph and
	    // iterate over it at same time)
	    HashSet nodeSet = new HashSet(graph.nodeSet());
	    Iterator nodes = nodeSet.iterator();
	    while(nodes.hasNext()) {
		Object n = nodes.next();
		if (graph.getDegree( n ) < colors.size() ) {
		    graph.hide(n);
		    
		    // removing n may have made previous nodes in
		    // the enumeration available to be hidden 
		    moreNodesToHide = true;
		}
	    }
	} while(moreNodesToHide);
	
	// at this point, we are assured that there are no more
	// nodes to hide.  Either the graph is finished (no nodes
	// remain) or all nodes present have degree >=
	// colors.size(), in which case this algorithm can't color
	// it without more colors.
	if (!graph.nodeSet().isEmpty()) {
	    throw new UnableToColorGraph();
	}
	
	for(Object n=graph.replace(); n!=null; n=graph.replace()){
	    // find color that none of n's neighbors is set to

	    if (graph.getColor(n) != null) {
		// precolored, skip
		System.out.println("skipping "+n+
				   "b/c its precolored to "+
				   graph.getColor(n));
		continue;
	    }

	    Collection nborsC = graph.neighborsOf(n);
	    HashSet nColors = new HashSet(nborsC.size());
	    for(Iterator nbors = nborsC.iterator(); nbors.hasNext();){
		Object nb = nbors.next();
		nColors.add(graph.getColor(nb));
	    }
	    
	    Color color = null;
	    for(Iterator cIter = colors.iterator(); cIter.hasNext();){
		Color col = (Color) cIter.next();
		if (!nColors.contains(col)) {
		    color = col;
		    break;
		}
	    }
	    
	    // color should be guaranteed to have been assigned at
	    // this point
	    Util.assert(color != null);

	    graph.setColor(n, color);

	}
    }

}
