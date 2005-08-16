// StaticStoreConstraintF.java, created Mon Jun 27 13:00:24 2005 by salcianu
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
 * <code>StaticStoreConstraintF</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: StaticStoreConstraintF.java,v 1.2 2005-08-16 22:41:57 salcianu Exp $
 */
class StaticStoreConstraintF extends Constraint {

    public StaticStoreConstraintF(LVar vd, IVar preI, FVar preF, FVar postF) {
	this((NodeSetVar) vd, (EdgeSetVar) preI, (NodeSetVar) preF, (NodeSetVar) postF);
    }

    private StaticStoreConstraintF(NodeSetVar vd, EdgeSetVar preI, NodeSetVar preF, NodeSetVar postF) {
	this.vd = vd;
	this.preI  = preI;
	this.preF  = preF;
	this.postF = postF;
	this.in  = Arrays.<Var>asList(vd, preI, preF);
	this.out = Collections.<Var>singleton(postF);
    }

    private final NodeSetVar vd;
    private final EdgeSetVar preI;
    private final NodeSetVar preF;
    private final NodeSetVar postF;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }
    

    public void action(SolAccessor sa) {
	Set<PANode> D = (Set<PANode>) sa.get(vd);
	if((D == null) || D.isEmpty()) return;
	
	Set<PANode> newEsc = PAUtil.findNewEsc(D, 
					       (PAEdgeSet) sa.get(preI),
					       (Set<PANode>) sa.get(preF)
					       /*,(Set<PANode>) sa.get(postF)*/);
	sa.join(postF, newEsc);
    }

    public Constraint rewrite(DisjointSet uf) {
	return 
	    new StaticStoreConstraintF((NodeSetVar) uf.find(vd),
				       (EdgeSetVar) uf.find(preI),
				       (NodeSetVar) uf.find(preF),
				       (NodeSetVar) uf.find(postF));
    }

    public String toString() { 
	return "reachable(" + vd + "," + preI + ") <= " + postF + " (use " + preF + " for speed-up)";
    }

    public int cost() { return Constraint.AVG_COST; }

}
