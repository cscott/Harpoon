// OptimisticGraphColorer.java, created Fri Jul 28 18:45:20 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;

import harpoon.Util.Util;
import harpoon.Util.Collections.LinearSet;

/**
 * <code>OptimisticGraphColorer</code> uses a strategy similar to that
 * of <code>SimpleGraphColorer</code>, except after removing
 * all nodes with degree < K (where K is the number of colors
 * provided), it begins optimistically removing nodes in the hopes
 * that they will not actually need to be spilled.  By default it
 * selects the nodes with the largest degree for removal in this
 * second stage, but this is parameterizable.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: OptimisticGraphColorer.java,v 1.1.2.6 2000-08-15 01:51:32 pnkfelix Exp $
 */
public class OptimisticGraphColorer extends GraphColorer {

    private final NodeSelector selector;
    
    public static abstract class NodeSelector {
	/** Returns a element of <code>g.nodeSet()</code>, in the
	    intent that it be removed from <code>g</code>.
	    <BR> <B>requires:</B> g.nodeSet() is not empty.
	    <BR> <B>effects:</B> returns some element of g.nodeSet().
	*/
	public abstract Object chooseNode(ColorableGraph g);
    }

    private static NodeSelector DEFAULT_SELECTOR = 
	new NodeSelector() {
	    public Object chooseNode(ColorableGraph g) {
		Object spillChoice = null; 
		int maxDegree = -1;
		Set nset = g.nodeSet();
		for(Iterator ns = nset.iterator(); ns.hasNext();){
		    Object n = ns.next();
		    if (g.getDegree(n) > maxDegree && 
			g.getColor(n) == null) {
			spillChoice = n;
		    }
		}
		Util.assert(spillChoice != null);
		return spillChoice;
	    }
	};

    /** Creates a <code>OptimisticGraphColorer</code> with the default
	second stage selection strategy. */
    public OptimisticGraphColorer() { 
	this.selector = DEFAULT_SELECTOR;
    }

    /** Creates a <code>OptimisticGraphColorer</code> with
	<code>selector</code> as its second stage selection
	strategy. */ 
    public OptimisticGraphColorer(NodeSelector selector) {
	this.selector = selector;
    }

    private boolean uncoloredNodesRemain(ColorableGraph g) {
	Iterator nodes = g.nodeSet().iterator();
	while(nodes.hasNext()) {
	    if (g.getColor(nodes.next()) == null) 
		return true;
	}
	return false;
    }

    public final void color(ColorableGraph graph, List colors)
	throws UnableToColorGraph {
	boolean moreNodesToHide;
	
	HashSet spills = new HashSet();

	for(;;) {
	    do {
		moreNodesToHide = false;
		LinearSet nodeSet = new LinearSet(graph.nodeSet());
		for(Iterator ns = nodeSet.iterator(); ns.hasNext();){ 
		    Object n = ns.next();
		    if (graph.getDegree(n) < colors.size() &&
			graph.getColor(n) == null) {
			graph.hide(n);
			moreNodesToHide = true;
		    } 
		}
	    } while (moreNodesToHide);

	    // Either the graph is finished (no uncolored nodes
	    // remain) or all nodes present have degree >= K, in which
	    // case we optimistically remove the node chosen by
	    // this.selector. 
	    
	    if (!uncoloredNodesRemain(graph)) {
		break;
	    } else {
		Object choice = this.selector.chooseNode(graph);
		graph.hide(choice);
		spills.add(choice);
		continue;
	    }
	}

	boolean unableToColor = false;

	nextNode:
	for(Object n=graph.replace(); n!=null; n=graph.replace()){
	    // find color that none of n's neighbors is set to
	    
	    if (graph.getColor(n) != null) {
		// precolored, die
		Util.assert(false);
	    }
	    
	    Collection nborsC = graph.neighborsOf(n);
	    HashSet nColors = new HashSet(nborsC.size());
	    for(Iterator nbors = nborsC.iterator(); nbors.hasNext();){
		Object nb = nbors.next();
		Color col = graph.getColor(nb);
		
		// nb can have no color, if it was a failed optimistic
		// spill.  treat it as not needing a color.
		if (col != null) nColors.add(col);
	    }
	    
	nextColor:
	    for(Iterator cIter = colors.iterator(); cIter.hasNext();){
		Color col = (Color) cIter.next();
		if (!nColors.contains(col)) {
		    try {
			graph.setColor(n, col);
			spills.remove(n);
			continue nextNode;
		    } catch (ColorableGraph.IllegalColor ic) {
			// col was not legal for n
			// try another color...  
			continue nextColor;
		    }
		}
	    }
	    
	    // if we ever reach this point, we failed to color n
	    unableToColor = true;
	}
	
	if (unableToColor) {
	    UnableToColorGraph u = new UnableToColorGraph();
	    u.rmvSuggs = spills;
	    throw u;
	}
    }
    
}
