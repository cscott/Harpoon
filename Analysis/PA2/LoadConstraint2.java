// LoadConstraint2.java, created Mon Jun 27 13:00:24 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

import harpoon.ClassFile.HField;
import harpoon.IR.Quads.Quad;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;

import net.cscott.jutil.DisjointSet;

/**
 * <code>LoadConstraint2</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: LoadConstraint2.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public class LoadConstraint2 implements Constraint {

    public LoadConstraint2(LVar vd, LVar vs, HField hf, FVar preF, OVar O,
			   Quad q, NodeRepository nodeRep) {
	this((NodeSetVar) vd, (NodeSetVar) vs, hf, (NodeSetVar) preF, (EdgeSetVar) O,
	     q, nodeRep);
    }

    private LoadConstraint2(NodeSetVar vd, NodeSetVar vs, HField hf, NodeSetVar preF, EdgeSetVar O,
			    Quad q, NodeRepository nodeRep) {
	this.vd = vd;
	this.vs = vs;
	this.hf = hf;
	this.preF = preF;
	this.O = O;
	this.q = q;
	this.nodeRep = nodeRep;

	this.in  = Arrays.<Var>asList(vs, preF);
	this.out = Arrays.<Var>asList(vd, O);
    }

    private final NodeSetVar vd;
    private final NodeSetVar vs;
    private final HField hf;
    private final NodeSetVar preF;
    private final EdgeSetVar O;

    private final Quad q;
    private final NodeRepository nodeRep;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }
    
    public void action(SolAccessor sa) {
	Set<PANode> sources = (Set<PANode>) sa.get(vs);
	if(sources == null) return;
	Set<PANode> F = (Set<PANode>) sa.get(preF);
	PANode nl = null;
	PAEdgeSet newOutsideEdges = null;
	for(PANode n : sources) {
	    if(PAUtil.escape(n, F)) {
		if(nl == null) {
		    nl = nodeRep.getLoadNode(q, hf.getType());
		}
		if(newOutsideEdges == null) {
		    newOutsideEdges = DSFactories.edgeSetFactory.create();
		}
		newOutsideEdges.addEdge(n, hf, nl);
	    }
	}
	if(newOutsideEdges != null) {
	    sa.join(O, newOutsideEdges);
	}

	// if at least one of the sources escapes, make vd point to
	// the load node (in addition to other nodes it may point to
	// as a result of the corresponding LoadConstraint1 constraint
	if(nl != null) {
	    sa.join(vd, Collections.<PANode>singleton(nl));
	}
    }

    public Constraint rewrite(DisjointSet uf) {
	return 
	    new LoadConstraint2((NodeSetVar) uf.find(vd),
				(NodeSetVar) uf.find(vs),
				hf,
				(NodeSetVar) uf.find(preF),
				(EdgeSetVar) uf.find(O),
				q,
				nodeRep);
    }
    
    public String toString() {
	return 
	    "load2: " + vd + " := " + vs + ".{" + hf + 
	    "} (reads escape:" + preF + ", updates outside edges:" + O + ")";
    }

    public int cost() { return Constraint.AVG_COST; }

}
