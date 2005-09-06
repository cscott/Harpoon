// MutationNFA.java, created Wed Aug 31 15:59:59 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.Factory;
import jpaul.DataStructs.MapWithDefault;

import jpaul.Graphs.LDiGraph.LForwardNavigator;
import jpaul.RegExps.NFA;
import jpaul.RegExps.RegExp;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;

import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.PAUtil;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.PAEdgeSet;

/**
 * <code>MutationNFA</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MutationNFA.java,v 1.4 2005-09-06 04:39:05 salcianu Exp $
 */
public class MutationNFA extends NFA<PANode,MLabel> {
    
    public MutationNFA(HMethod hm, InterProcAnalysisResult ipar, PointerAnalysis pa) {
	final Map<PANode,List<Pair<PANode,MLabel>>> state2trans = 
	    new MapWithDefault(new LinkedListFactory<Pair<PANode,MLabel>>(), true);

	addParamTrans(state2trans, hm, ipar, pa);

	addOutsideEdgesTrans(state2trans, ipar);

	addMutationTrans(state2trans, ipar, pa);

	addGblEscTrans(state2trans, ipar);

	lFwdNav = new LForwardNavigator<PANode,MLabel>() {
	    public List<Pair<PANode,MLabel>> lnext(PANode state) {
		return state2trans.get(state);
	    }
	};
    }


    private void addParamTrans(Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
			       HMethod hm,
			       InterProcAnalysisResult ipar,
			       PointerAnalysis pa) {
	for(ParamInfo pi : MAUtil.getParamInfo(hm, pa)) {
	    if(pi.type().isPrimitive()) continue;

	    assert pi.node() != null;
	    state2trans.get(startState).add
		(new Pair<PANode,MLabel>(pi.node(),
					 new MLabel.Param(pi.temp(), pi.declName())));
	}
    }


    private void addOutsideEdgesTrans(final Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				      InterProcAnalysisResult ipar) {
	ipar.eomO().forAllEdges(new PAEdgeSet.EdgeAction() {
	    public void action(PANode src, HField hf, PANode dst) {
		state2trans.get(src).add(new Pair<PANode,MLabel>
					 (dst,
					  MLabel.field2mlabel(hf)));
	    }
	});
    }


    private void addMutationTrans(Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				  InterProcAnalysisResult ipar,
				  PointerAnalysis pa) {
	boolean addedGBL = false;

	for(Pair<PANode,HField> abstrField : ipar.eomWrites()) {
	    PANode node = abstrField.left;
	    HField hf   = abstrField.right;
	    assert !((node == null) && (hf == null));

	    if((node != null) && (node.kind == PANode.Kind.GBL)) {
		if(!addedGBL) {
		    addedGBL = true;
		    state2trans.get(startState).
			add(new Pair<PANode,MLabel>
			    (node,
			     MLabel.reachFromStat));
		}
	    }

	    PANode state = 
		// for mutated static fields, the transition starts in the startState
		(node == null) ?
		startState :
		// for instance fields, it starts in the corresponding PANode
		node;

	    state2trans.get(state).add(new Pair<PANode,MLabel>
				       (acceptState,
					MLabel.field2mlabel(hf)));
	}	
    }


    private void addGblEscTrans(Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				InterProcAnalysisResult ipar) {
	for(PANode node : ipar.eomAllGblEsc()) {
	    // the fact that GBL escapes globally is not that
	    // interesting; we are interested only in the normal nodes
	    // tat were compressed into GBL
	    if(node.kind == PANode.Kind.GBL) continue;
	    state2trans.get(node).add(new Pair<PANode,MLabel>(node, MLabel.field2mlabel(null)));
	}
    }

    // pseudo-node for the starting state
    private static final PANode startState  = 
	new PANode(PANode.Kind.NULL, null) {
	    public String toString() { return "START"; }
	};
    // pseudo-node for the only accepting state
    private static final PANode acceptState = 
	new PANode(PANode.Kind.NULL, null) {
	    public String toString() { return "ACCEPT"; }
	};

    public PANode startState() { return startState; }

    protected Collection<PANode> _acceptStates() { 
	return Collections.<PANode>singleton(acceptState); 
    }
    
    public LForwardNavigator<PANode,MLabel> getLForwardNavigator() {
	return lFwdNav;
    }
    private final LForwardNavigator<PANode,MLabel> lFwdNav;



    private static class LinkedListFactory<E> implements Factory<List<E>> {
	public List<E> create() { return new LinkedList<E>(); }
	public List<E> create(List<E> list) { return new LinkedList<E>(list); }
    }


    public RegExp<MLabel> toRegExp() {
	RegExp<MLabel> regExp = this.simplify().toRegExp();
	//System.out.println("regExp = " + regExp);
	regExp = MutRegExpSimplifier.simplify(regExp);
	return regExp;
    }
}
