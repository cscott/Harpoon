// CloneCallConstraint.java, created Tue Jun 28 15:23:09 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

import java.util.Queue;

import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

import harpoon.Analysis.MetaMethods.GenType;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;
import jpaul.DataStructs.Relation;
import jpaul.DataStructs.DSUtil;

import jpaul.Misc.Function;
import jpaul.Misc.Predicate;
import jpaul.Misc.SetMembership;

import net.cscott.jutil.DisjointSet;

/**
 * <code>CloneCallConstraint</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: CloneCallConstraint.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public class CloneCallConstraint implements Constraint {

    public CloneCallConstraint(CALL cs,
			       LVar vres,
			       LVar vex,
			       LVar vs,

			       IVar v_preI,
			       FVar v_preF,			       

			       IVar v_postI,
			       OVar v_O,
			       FVar v_postF,

			       NodeRepository nodeRep) { // to be able to generate new (load) nodes
	this(cs,	     
	     (NodeSetVar) vres,
	     (NodeSetVar) vex,
	     (NodeSetVar) vs,

	     (EdgeSetVar) v_preI,
	     (NodeSetVar) v_preF,
	     
	     (EdgeSetVar) v_postI,
	     (EdgeSetVar) v_O,
	     (NodeSetVar) v_postF,

	     nodeRep);
    }

    public CloneCallConstraint(CALL cs,
			       NodeSetVar vres,
			       NodeSetVar vex,
			       NodeSetVar vs,

			       EdgeSetVar v_preI,
			       NodeSetVar v_preF,

			       EdgeSetVar v_postI,			       
			       EdgeSetVar v_O,
			       NodeSetVar v_postF,
			       NodeRepository nodeRep) {
	this.cs = cs;

	this.vres = vres;
	this.vex  = vex;
	this.vs = vs;

	this.v_preI  = v_preI;
	this.v_preF  = v_preF;

	this.v_postI = v_postI;
	this.v_O     = v_O;
	this.v_postF = v_postF;

	this.nodeRep = nodeRep;

	in  = Arrays.<Var>asList(vs, v_preI, v_preF);
	out = new LinkedList<Var>();
	if(vres != null) { out.add(vres); }
	if(vex  != null) { out.add(vex);  }
	out.addAll(Arrays.<Var>asList(v_postI, v_O, v_postF));
    }


    private final CALL cs;

    private final NodeSetVar vres;
    private final NodeSetVar vex;
    private final NodeSetVar vs;

    private final EdgeSetVar v_preI;
    private final NodeSetVar v_preF;

    private final EdgeSetVar v_postI;
    private final EdgeSetVar v_O;
    private final NodeSetVar v_postF;

    private final NodeRepository nodeRep;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }

   
    public Constraint rewrite(DisjointSet uf) {
	return 
	    new CloneCallConstraint(cs,
				    (NodeSetVar) uf.find(vres),
				    (NodeSetVar) uf.find(vex),

				    (NodeSetVar) uf.find(vs),

				    (EdgeSetVar) uf.find(v_preI),
				    (NodeSetVar) uf.find(v_preF),

				    (EdgeSetVar) uf.find(v_postI),
				    (EdgeSetVar) uf.find(v_O),
				    (NodeSetVar) uf.find(v_postF),

				    nodeRep);
    }
    
    public String toString() {
	return 
	    "<" + vres + "," + vex + "> := clone " + vs + "\n" +
	    "\tinsd: "  + v_preI + " -> " + v_postI +
	    "; outsd: " + v_O + 
	    "; escpd: " + v_preF + " -> " + v_postF;
    }

    public int cost() { return Constraint.HIGH_COST; }


    
    public void action(SolAccessor sa) {
	// S is the set of nodes to be cloned
	Set<PANode> S = (Set<PANode>) sa.get(vs);
	if(S == null || S.isEmpty()) return;

	this.preF = PAUtil.fixNull((Set<PANode>) sa.get(v_preF));
	this.preI = PAUtil.fixNull((PAEdgeSet) sa.get(v_preI));

	Set<HField> fields = possibleFields(S);
	Collection<PANode> ES = DSUtil.<PANode>filterColl(S, 
							  new Predicate<PANode>() {
							      public boolean check(PANode node) {
								  return PAUtil.escape(node, preF);
							      }
							  },
							  DSFactories.nodeSetFactory.newColl());
	// D is the set of clones of the nodes from S
	Collection<PANode> D  = DSUtil.<PANode,PANode>mapColl(S, 
							      new Function<PANode,PANode>() {
								  public PANode f(PANode node) {
								      return nodeRep.getSpecialInside(cs, node.type);
								  }
							      },
							      DSFactories.nodeSetFactory.newColl());
	if(Flags.VERBOSE_CLONE) {
	    System.out.println("ES = " + ES);
	    System.out.println("D  = " + D);
	}

	this.deltaI = DSFactories.edgeSetFactory.create();
	this.deltaO = DSFactories.edgeSetFactory.create();

	for(HField hf : fields) {
	    simulate_load_store(S, ES, D, hf);
	}

	if(Flags.VERBOSE_CLONE) {
	    System.out.println("deltaI = \n" + deltaI);
	    System.out.println("deltaO = \n" + deltaO);
	}

	sa.join(v_postI, deltaI);
	sa.join(v_O,     deltaO);

	if(vres != null) {
	    sa.join(vres, D);
	}
	if(vex != null) {
	    sa.join(vex,  Collections.<PANode>singleton(nodeRep.getGlobalNode()));
	}

	this.preF   = null;
	this.preI   = null;
	this.deltaI = null;
	this.deltaO = null;
    }

    private PAEdgeSet preI;
    private PAEdgeSet deltaI;
    private PAEdgeSet deltaO;
    private Set<PANode> preF;

    
    private void simulate_load_store(Collection<PANode> S, Collection<PANode> ES, Collection<PANode> D, HField hf) {
	// temp = S.hf; D.hf = temp
	// handle nodes pointed to through inside edges
	for(PANode source : S) {
	    Collection<PANode> targets = preI.pointedNodes(source, hf);
	    deltaI.addEdges(D, hf, targets);
	}

	PANode lNode = nodeRep.getSpecialLoad(cs, hf);
	boolean someEscape = false;	
	for(PANode node : ES) {
	    if(deltaO.addEdge(node, hf, lNode)) {
		someEscape = true;
	    }
	}
	if(someEscape) {
	    deltaI.addEdges(D, hf, Collections.<PANode>singleton(lNode));
	}
    }



    private Set<HField> possibleFields(Set<PANode> nodes) {
	if(Flags.VERBOSE_CLONE)
	    System.out.println("nodes = " + nodes);

	Set<HClass> classes = possibleClasses(nodes);

	if(Flags.VERBOSE_CLONE)
	    System.out.println("classes = " + classes);

	Set<HField> fields = new HashSet<HField>();
	for(HClass hClass : classes) {
	    if(hClass.isArray()) {
		fields.add(PAUtil.getArrayField(PAUtil.getLinker(cs)));
	    }
	    else {
		for(HField hf : hClass.getFields()) {
		    if(!hf.getType().isPrimitive() && !hf.isStatic())
			fields.add(hf);
		}
	    }
	}

	if(Flags.VERBOSE_CLONE)
	    System.out.println("fields = " + fields);

	return fields;
    }

    private Set<HClass> possibleClasses(Set<PANode> nodes) {
	Set<HClass> classes = new HashSet<HClass>();
	for(PANode node : nodes) {
	    GenType gt = node.type;
	    if(gt.isPOLY()) {
		classes.addAll(TypeFilter.getChildren(gt.getHClass()));
	    }
	    else {
		classes.add(gt.getHClass());
	    }
	}
	return classes;
    }

}
