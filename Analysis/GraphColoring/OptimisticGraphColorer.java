// OptimisticGraphColorer.java, created Fri Jul 28 18:45:20 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Analysis.GraphColoring.ColorableGraph.IllegalColor;

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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: OptimisticGraphColorer.java,v 1.1.2.11 2001-06-17 22:29:39 cananian Exp $
 */
public class OptimisticGraphColorer extends GraphColorer {

    public static boolean MONITOR = false;
    private static void MONITOR(String s) {
	if (MONITOR) System.out.print(s);
    }
    
    private final NodeSelector selector;
    
    public static abstract class NodeSelector {
	/** Returns a element of <code>g.nodeSet()</code>, in the
	    intent that it be removed from <code>g</code>.
	    <BR> <B>requires:</B> g.nodeSet() is not empty.
	    <BR> <B>effects:</B> returns some element of g.nodeSet().
	*/
	public abstract Object chooseNodeForRemoval(ColorableGraph g);

	/** Returns a element of <code>g.nodeSet()</code>, in the
	    intent that it be hidden in <code>g</code>.
	    <BR> <B>requires:</B> g.nodeSet() is not empty.
	    <BR> <B>effects:</B> 
	         returns some uncolored element of g.nodeSet(), or
		 null if all the elements of g.nodeSet() are colored.
	*/
	public abstract Object chooseNodeForHiding(ColorableGraph g);

	/** Checks if node can be removed from graph to improve
	    colorability.
	    <BR> <B>requires:</B> n is in g
	    <BR> <B>effects:</B> returns True if n could ever be
	         returned from chooseNodeForRemoval(g), False
		 otherwise. 
	*/
	public abstract boolean allowedToRemove(Object n,ColorableGraph g);

    }

    private static NodeSelector DEFAULT_SELECTOR = new SimpleSelector();

    public static class SimpleSelector extends NodeSelector {
	protected SimpleSelector() { }
	public boolean allowedToRemove(Object n, ColorableGraph g) {
	    return true;
	}
	public Object chooseNodeForRemoval(ColorableGraph g) {
	    Object o = chooseNode(g);
	    Util.assert(o != null);
	    return o;
	}
	public Object chooseNodeForHiding(ColorableGraph g) {
	    Object o = chooseNode(g);
	    return o;
	}
	private Object chooseNode(ColorableGraph g) {
	    Object spillChoice = null; 
	    Set nset = g.nodeSet();
	    int maxDegree = -1;
	    for(Iterator ns = nset.iterator(); ns.hasNext();){
		Object n = ns.next();
		if (g.getColor(n) == null && 
		    g.getDegree(n) > maxDegree) {
		    spillChoice = n;
		    maxDegree = g.getDegree(n);
		}
	    }
	    return spillChoice;
	}
    }	
    
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

			// MONITOR("conservative hide: "+n+"\n");
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
		Object choice = this.selector.chooseNodeForHiding(graph);
		graph.hide(choice);
		spills.add(choice);

		MONITOR("optimistic hide: "+choice+"\n");
		continue;
	    }
	}

	HashSet allSpills = new HashSet(spills);
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
		if (spills.contains(nb)) continue;
		
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
			// MONITOR("trying "+col+" with "+n+"\n");
			graph.setColor(n, col);
			spills.remove(n);

			// MONITOR("set color of "+n+ " to "+col+"\n");
			continue nextNode;
		    } catch (IllegalColor ic) {
			// col was not legal for n
			// try another color...  
			if (false) MONITOR(col + " not legal for " + n + 
				"b/c of conflict between "+
				ic.color + " and " + ic.node+"\n");
			continue nextColor;
		    }
		}
	    }
	    
	    // if we ever reach this point, we failed to color n
	    MONITOR("failed to color "+n+"\n");
	    // MONITOR("nbors(n): "+graph.neighborsOf(n));
	    
	    Object choice = null; int max = -1;
	    for(Iterator ns=graph.neighborsOf(n).iterator();ns.hasNext();){
		Object nde = ns.next();
		if (graph.getDegree(nde) > max &&
		    selector.allowedToRemove(nde,graph)) {
		    choice = nde;
		    max = graph.getDegree(nde);
		}
	    }
	    if (choice == null)
	        choice = this.selector.chooseNodeForRemoval(graph);
	    if (choice != null) {
		spills.add(choice); 
		allSpills.add(choice);
		graph.unsetColor(choice);
	    }
	    unableToColor = true;
	}
	
	if (unableToColor) {
	    UnableToColorGraph u = new UnableToColorGraph();
	    u.rmvSuggs = spills;
	    u.rmvSuggsLarge = allSpills;
	    throw u;
	}
    }
    
}
