// TypeFilterConstraint.java, created Mon Jun 27 13:00:24 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

import harpoon.ClassFile.HClass;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;

import jpaul.DataStructs.DisjointSet;

/**
 * <code>TypeFilterConstraint</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: TypeFilterConstraint.java,v 1.2 2005-08-16 22:41:57 salcianu Exp $
 */
class TypeFilterConstraint extends Constraint {

    public TypeFilterConstraint(LVar vs, HClass hClass, LVar vd) {
	this((NodeSetVar) vs, hClass, (NodeSetVar) vd);
    }

    private TypeFilterConstraint(NodeSetVar vs, HClass hClass, NodeSetVar vd) {
	this.vs     = vs;
	this.hClass = hClass;
	this.vd     = vd;
	this.in     = Collections.<Var>singleton(vs);
	this.out    = Collections.<Var>singleton(vd);
    }

    private final NodeSetVar vs;
    private final HClass hClass;
    private final NodeSetVar vd;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }
    

    public void action(SolAccessor sa) {
	Set<PANode> nodes = (Set<PANode>) sa.get(vs);
	if((nodes == null) || nodes.isEmpty()) return;

	Set<PANode> filteredNodes = DSFactories.nodeSetFactory.create();
	for(PANode node : nodes) {
	    if(TypeFilter.compatible(node, hClass)) {
		filteredNodes.add(node);
	    }
	}

	sa.join(vd, filteredNodes);
    }


    public Constraint rewrite(DisjointSet uf) {
	return 
	    new TypeFilterConstraint((NodeSetVar) uf.find(vs),
				     hClass,
				     (NodeSetVar) uf.find(vd));
    }

    public String toString() { 
	return vd  + " := filter(" + vs + ", " + hClass + ")";
    }

    public int cost() { return Constraint.AVG_COST; }

}
