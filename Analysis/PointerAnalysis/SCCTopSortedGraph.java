// SCCTopSortedGraph.java, created Wed Jan 26 11:39:40 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;


/**
 * <code>SCCTopSortedGraph</code> represents a \
 graph of strongly connected components topologically sorted in decreasing \
 order. 
 * To obtain such a graph, use the <code>topSort</code> static method.
 * It uses a Depth First Search to do the sortting in linear time (see
 * Section 23.4 in Cormen and co for the exact algorithm).
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SCCTopSortedGraph.java,v 1.1.2.1 2000-01-27 06:21:22 salcianu Exp $
 */
public class SCCTopSortedGraph {
    
    private SCComponent first;
    private SCComponent last;

    // the only way to obtain an object of this class is through topSort
    private SCCTopSortedGraph(SCComponent first, SCComponent last) {
	this.first = first;
	this.last  = last;
    }

    /** Returns the first (i.e. one of the topologically biggest) \
	<code>SCComponent</code> */
    public final SCComponent getFirst(){
	return first;
    }

    /** Returns the last (i.e. one of the topologically smallest) \
	<code>SCComponent</code> */
    public final SCComponent getLast(){
	return last;
    }

    // data for the static method topSort
    private static Set reached_sccs;
    private static SCComponent first_scc;
    private static SCComponent last_scc;

    /** Sorts all the strongly connected component reachable from \
     <code>root</code> in decreasing topological order. 
    * This method sets the <code>nextTopSort</code> and
    * <code>prevTopSort</code> fields of the <code>SCComponent</code>s,
    * arranging then in a double linked list according to the 
    * aforementioned order.<br>
    * It returns a <code>SCCTopSortedGraph</code> containing the first and
    * the last elements of this list. */
    public static SCCTopSortedGraph topSort(SCComponent root){
	reached_sccs = new HashSet();
	// to facilitate insertions into the double linked list of SCCs,
	// a dummy node is created (now, we don't worry about
	// first_scc == null)
	last_scc  = new SCComponent();
	first_scc = last_scc;
	// Depth First Search to sort the SCCs topologically
	DFS_topsort(root);
	// get rid of the dummy node
	last_scc = last_scc.prevTopSort;
	last_scc.nextTopSort = null;
	SCCTopSortedGraph G = new SCCTopSortedGraph(first_scc,last_scc);
	reached_sccs = null; // enable the GC
	first_scc = null;
	last_scc =  null;
	return G;
    }

    // the DFS used by the topological sort algorithm
    private static void DFS_topsort(SCComponent scc){
	if(reached_sccs.contains(scc)) return;

	reached_sccs.add(scc);
	int nb_next = scc.nextLength();
	for(int i = 0; i < nb_next; i++)
	    DFS_topsort(scc.next(i));

	// add scc at the front of the list of sorted SCC
	scc.nextTopSort   = first_scc;
	first_scc.prevTopSort = scc;
	first_scc = scc;
    }

}
