// StoreConstraint.java, created Mon Jun 27 13:00:24 2005 by salcianu
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

import net.cscott.jutil.DisjointSet;

/**
 * <code>StoreConstraint</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: StoreConstraint.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public class StoreConstraint implements Constraint {

    public StoreConstraint(LVar vs, HField hf, LVar vd, IVar postI) {
	this((NodeSetVar) vs, hf, (NodeSetVar) vd, (EdgeSetVar) postI);
    }

    private StoreConstraint(NodeSetVar vs, HField hf, NodeSetVar vd, EdgeSetVar postI) {
	this.vs = vs;
	this.hf = hf;
	this.vd = vd;
	this.postI = postI;
	this.in  = Arrays.<Var>asList(vs, vd);
	this.out = Collections.<Var>singleton(postI);
    }

    private final NodeSetVar vs;
    private final HField hf;
    private final NodeSetVar vd;
    private final EdgeSetVar postI;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }
    
    public void action(SolAccessor sa) {
	Set<PANode> S = (Set<PANode>) sa.get(vs);
	if((S == null) || S.isEmpty()) return;
	Set<PANode> D = (Set<PANode>) sa.get(vd);
	if((D == null) || D.isEmpty()) return;

	// initialize a new set of edges: newEdges <- {}
	PAEdgeSet newEdges = DSFactories.edgeSetFactory.create();
	for(PANode ns : S) {
	    // newEdges += {ns} x {hf} x D
	    newEdges.addEdges(ns, hf, D);
	}
	sa.join(postI, newEdges);
    }

    public Constraint rewrite(DisjointSet uf) {
	return 
	    new StoreConstraint((NodeSetVar) uf.find(vs),
				hf,
				(NodeSetVar) uf.find(vd),
				(EdgeSetVar) uf.find(postI));
    }

    public String toString() { 
	return vs + " x {" + hf + "} x " + vd + " <= " + postI;
    }

    public int cost() { return Constraint.AVG_COST; }

}
