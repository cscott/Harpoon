// LoadConstraint1.java, created Mon Jun 27 13:00:24 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

import harpoon.ClassFile.HField;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;

import jpaul.DataStructs.DisjointSet;

/**
 * <code>LoadConstraint1</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: LoadConstraint1.java,v 1.2 2005-08-16 22:41:57 salcianu Exp $
 */
class LoadConstraint1 extends Constraint {

    public LoadConstraint1(LVar vd, LVar vs, HField hf, IVar preI) {
	this((NodeSetVar) vd, (NodeSetVar) vs, hf, (EdgeSetVar) preI);
    }

    private LoadConstraint1(NodeSetVar vd, NodeSetVar vs, HField hf, EdgeSetVar preI) {
	this.vd = vd;
	this.vs = vs;
	this.hf = hf;
	this.preI = preI;

	this.in  = Arrays.<Var>asList(vs, preI);
	this.out = Arrays.<Var>asList(vd);	
    }			    

    private final NodeSetVar vd;
    private final NodeSetVar vs;
    private final HField hf;
    private final EdgeSetVar preI;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }
    
    public void action(SolAccessor sa) {
	PAEdgeSet insideEdges = (PAEdgeSet) sa.get(preI);
	if(insideEdges == null) return;

	Set<PANode> S = (Set<PANode>) sa.get(vs);
	if(S == null) return;

	for(PANode n : S) {
	    Collection<PANode> dests = insideEdges.pointedNodes(n, hf);
	    if(!dests.isEmpty()) {
		sa.join(vd, dests);
	    }
	}
    }

    public Constraint rewrite(DisjointSet uf) {
	return 
	    new LoadConstraint1((NodeSetVar) uf.find(vd),
				(NodeSetVar) uf.find(vs),
				hf,
				(EdgeSetVar) uf.find(preI));
    }
    
    public String toString() { 
	return 
	    "load1: " + vd + " := " + vs + ".(" + preI + ") {" + hf + "}";
    }

    public int cost() { return Constraint.AVG_COST; }

}
