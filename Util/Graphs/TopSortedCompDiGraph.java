// TopSortedCompDiGraph.java, created Thu Mar  4 08:10:43 2004 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

import net.cscott.jutil.ReverseListIterator;

import harpoon.Util.DataStructs.ReverseListView;

/**
 * <code>TopSortedCompDiGraph</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: TopSortedCompDiGraph.java,v 1.2 2004-03-05 15:38:14 salcianu Exp $
 */
public class TopSortedCompDiGraph<Vertex> 
    extends DiGraph<SCComponent/*<Vertex>*/> {

    /** Constructs the topologically sorted component digraph of
        <code>digraph</code>. */
    public TopSortedCompDiGraph(DiGraph graph) {
	DiGraph<SCComponent/*<Vertex>*/> sccGraph = 
	    graph.getComponentDiGraph();
	sccRoots = sccGraph.getRoots();
	// build list of topologically sorted SCCs
	sccSortedList = new LinkedList();
	sccGraph.dfs
	    (null, // no action on node entry
	     // on dfs termination, add scc to front of sccSortedList
	     new VertexVisitor<SCComponent/*<Vertex>*/>() {
		public void visit(SCComponent/*<Vertex>*/ scc) {
		    sccSortedList.addFirst(scc);
		}
	    });
    }

    public TopSortedCompDiGraph(Set/*<Vertex>*/ roots,
				Navigator/*<Vertex>*/ nav) {
	this(DiGraph.diGraph(roots, nav));
    }

    // set of top-level SCCs (no incoming edges + all SCCs are
    // reachable from here)
    private final Set<SCComponent/*<Vertex>*/> sccRoots;
    // list of all SCCs, in decreasing topologic order
    private final LinkedList<SCComponent/*<Vertex>*/> sccSortedList;

    public Set<SCComponent/*<Vertex>*/> getRoots() {
	return sccRoots;
    }

    public Navigator<SCComponent/*<Vertex>*/> getNavigator() {
	return SCComponent.SCC_NAVIGATOR;
    }

    /** @return list of the strongly connected components of the
        underlying digraph, in <b>decreasing</b> topologic order,
        i.e., starting with the SCCs with no incoming edges. */
    public List<SCComponent/*<Vertex>*/> decrOrder() {
	return sccSortedList;
    }
    
    /** @return list of the strongly connected components of the
        underlying digraph, in <b>increasing</b> topologic order,
        i.e., starting with the SCCs with no outgoing edges. */
    public List<SCComponent/*<Vertex>*/> incrOrder() {
	return new ReverseListView<SCComponent/*<Vertex>*/>(sccSortedList);
    }
    
}

