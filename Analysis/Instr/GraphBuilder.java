// GraphBuilder.java, created Fri Nov  5 14:23:00 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Properties.UseDef;
import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Analysis.GraphColoring.Graph;
import harpoon.Analysis.GraphColoring.SparseGraph;

import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.DefaultMultiMap;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

/**
 * <code>GraphBuilder</code> is a helper class for register allocation 
 * which builds interferences graphs (instances of
 * <code>GraphColoring.Graph</code>  that can be subsequently colored.
 * <BR>
 * The <code>GraphBuilder</code>, given: <OL>
 * <LI> A Set of Webs
 * <LI> A [ Web -> (Temp x Set of Refs) Pair ] mapping 
 *      (implictly contained in the Web, for now)
 * <LI> A [ (Temp x Ref) Pair -> Set of Assignments ] mapping 
 * </OL>
 * will then generate: <OL>
 * <LI> A [ Node -> ( Web | Assignment ) ] mapping 
 *      (performed with an instanceof test, for now)
 * <LI> A Graph representing the interferences of the Nodes
 * </OL>
 * 
 * (where, in the above definition, 
 *  a Ref is a Use/Def of a Temp,
 *  an Assignment is a List of Registers,
 *  and a Register is a special Temp provided by the Backend).
 * <P>
 * The <code>GraphColoring.Graph</code> produced can then be colored
 * using a <code>GraphColoring.GraphColorer</code> and used to
 * generate either register allocation or stack-slot allocation
 * information, depending on what is needed.  
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GraphBuilder.java,v 1.1.2.3 1999-11-09 06:28:26 pnkfelix Exp $
 */
public class GraphBuilder {
    
    public abstract class AssignmentSuggestor {
	/** Returns a <code>Set</code> of Register Assignments, where
	    each Assignment is a <code>List</code> of
	    <code>Temp</code>s.
	*/
	public abstract Set getAssignments(Temp t, UseDef ref);
    }

    public class AssignmentNode extends
	harpoon.Analysis.GraphColoring.DefaultSparseNode {

	/** List of Temps representing this register assignment. 
	 */
	public final List registers;
	
	/** Constructs an <code>AssignmentNode</code> with assignment
	    <code>regs</code>. 
	    <BR> <B>requires:</B> regs is a <code>List</code> of
	    <code>Temp</code>s. 
	*/
	AssignmentNode(List regs) {
	    super();
	    registers = Collections.unmodifiableList(regs);
	}
	
    }
    
    /** List[Reg] -> AssignmentNode mapping used solely by
	getNode(List). 
    */
    private HashMap regLstToNodes;

    /** Returns the <code>AssignmentNode</code> associated with
	<code>regs</code>, constructing it if necessary.
     */
    AssignmentNode getNode(List regs) {
	AssignmentNode n = 
	    (AssignmentNode) regLstToNodes.get(regs);
	if (n == null) {
	    n = new AssignmentNode(regs);
	    regLstToNodes.put(regs, n);
	}
	return n;
    }

    /** Reg -> Set[Node] mapping
     */
    private MultiMap interferesWith;

    /** Interference Graph
     */
    private SparseGraph graph; /* should Type just be ColorableGraph? */

    /** Creates a <code>GraphBuilder</code>. 
	This builds an interference <code>Graph</code> that can then
	be subsequently colored.
     */
    public GraphBuilder(Set webs, AssignmentSuggestor suggest,
			LiveVars liveVars, Map instrToBBmap) {
        // initialize object-wide data structures.
	regLstToNodes = new HashMap();
	interferesWith = new DefaultMultiMap();
	graph = new SparseGraph();

	// procedure local data structures
	HashSet regLists = new HashSet();

	// put nodes in 'graph'
	Iterator webIter = webs.iterator();
	while(webIter.hasNext()) {
	    Web w = (Web) webIter.next();
	    graph.addNode(w);

	    Temp t = w.var;
	    Iterator refIter = w.refs.iterator();
	    while(refIter.hasNext()) {
		UseDef ref = (UseDef) refIter.next();
		Iterator assignments = 
		    suggest.getAssignments(t, ref).iterator();
		while(assignments.hasNext()) {
		    List lst = (List) assignments.next();
		    regLists.add(lst);
		    AssignmentNode anode = getNode(lst);
		    
		    graph.addNode(anode);
		}
	    }
	}

	// put interference edges in 'graph'
	Iterator webIter1 = webs.iterator();
	while(webIter1.hasNext()) {
	    Web w1 = (Web) webIter1.next();
	    Iterator webIter2 = webs.iterator();
	    while(webIter2.hasNext()) {
		Web w2 = (Web) webIter2.next();
		if (w1 == w2) continue; // dont add edge to self
		
		// How do I do this?		

		// two approaches:
		// Appel: If two variables (webs?) are live
		//        at the same point, they interfere
		// Muchnik: If a web is live at another web's
		//          definition point, they interfere
		
		// Unfortunately, our webs do not store every program
		// point at which they are live; they merely store
		// points that refer to them.  

		// LiveVars, as written, deals with Temps, not Webs.
		// So should either extend it if possible or make a
		// duplicate that "does the right thing"
	    }
	}
	
	// put resource conflict edges in 'graph'
	Iterator regListIter1 = regLists.iterator();
	while(regListIter1.hasNext()){
	    List regList1 = (List) regListIter1.next();
	    Iterator regListIter2 = regLists.iterator();
	    while(regListIter2.hasNext()) {
		List regList2 = (List) regListIter2.next();

		if (regList1.equals(regList2)) {
		    continue; // don't add edge to self
		}

		if (regList1.size() != regList2.size() ||
		    regList1.size() == 1 ||
		    regList2.size() == 1) {
		    // (efficiency hacks) 
		    // different number of registers ==> interference
		    // singleton register list ==> interference
		    AssignmentNode n1 = getNode(regList1);
		    AssignmentNode n2 = getNode(regList2);
		    graph.addEdge(n1, n2);
		} else {
		    Collection intersection = null;

			// clone's not supported, so we've gotta make
			// this collection from scratch
			intersection = new
			    ArrayList(regList1.size());
			intersection.addAll(regList1);
			intersection.retainAll(regList2);

		    if (!intersection.isEmpty()) {
			// these lists share a register, put an edge
			// between them
			AssignmentNode n1 = getNode(regList1);
			AssignmentNode n2 = getNode(regList2);
			graph.addEdge(n1, n2);
		    }
		}
	    }
	}
    }

}
