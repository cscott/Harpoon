// GraphBuilder.java, created Fri Nov  5 14:23:00 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Properties.UseDef;
import harpoon.Analysis.GraphColoring.Graph;
import harpoon.Analysis.GraphColoring.SparseGraph;

import harpoon.Util.Collections.MultiMap;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
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
 * @version $Id: GraphBuilder.java,v 1.1.2.2 1999-11-06 01:58:34 pnkfelix Exp $
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

     */
    public GraphBuilder(Set webs, AssignmentSuggestor suggest) {
        // initialize data structures.
	regLstToNodes = new HashMap();
	interferesWith = new MultiMap();
	graph = new SparseGraph();


	Iterator webIter = webs.iterator();
	while(webIter.hasNext()) {
	    Web w = (Web) webIter.next();
	    Temp t = w.var;
	    Iterator refIter = w.refs.iterator();
	    while(refIter.hasNext()) {
		UseDef ref = (UseDef) refIter.next();
		Iterator assignments = 
		    suggest.getAssignments(t, ref).iterator();
		while(assignments.hasNext()) {
		    List lst = (List) assignments.next();
		    AssignmentNode anode = getNode(lst);
		    
		    // assignments can be repeated as this runs, so we
		    // need to ensure that we don't add the same node
		    // multiple times.
		    graph.addNode(anode);
		}
	    }
	}
    }
    
    

}
