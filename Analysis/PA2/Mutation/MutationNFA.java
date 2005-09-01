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
 * @version $Id: MutationNFA.java,v 1.1 2005-09-01 22:45:24 salcianu Exp $
 */
public class MutationNFA extends NFA<PANode,MutationNFA.MLabel> {
    
    public static class MLabel {}

    public static class FieldMLabel extends MLabel {
	FieldMLabel(HField hf) {
	    this.hf = hf;
	}
	public final HField hf;
	public String toString() {
	    if(hf.isStatic()) {
		return hf.getDeclaringClass().getName() + "." + hf.getName();
	    }
	    return hf.getName();
	}
    }

    public static class ReachMLabel extends MLabel {
	public String toString() {
	    return "REACH";
	}
    }

    private MLabel field2mlabel(HField hf) {
	// hf == null means that all reachable objects may be mutated
	// special label "REACH"
	if(hf == null) return reachMLabel;

	MLabel mlabel = hf2mlabel.get(hf);
	if(mlabel == null) {
	    mlabel = new FieldMLabel(hf);
	    hf2mlabel.put(hf, mlabel);
	}
	return mlabel;
    }
    private final MLabel reachMLabel = new ReachMLabel();
    private final Map<HField,MLabel> hf2mlabel = new HashMap<HField,MLabel>();


    public static class ParamMLabel extends MLabel {
	ParamMLabel(Temp temp, String name) {
	    this.temp = temp;
	    if(name == null) name = temp.toString();
	    this.name = name;
	}
	public final Temp temp;
	public final String name;
	public String toString() { return name; }
    }

    


    public MutationNFA(HMethod hm, InterProcAnalysisResult ipar, PointerAnalysis pa) {
	final Map<PANode,List<Pair<PANode,MLabel>>> state2trans = 
	    new MapWithDefault(new LinkedListFactory<Pair<PANode,MLabel>>(), true);

	addParamTrans(state2trans, hm, ipar, pa);

	addOutsideEdgesTrans(state2trans, ipar);

	addMutationTrans(state2trans, ipar);

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
	Iterator<HClass> itParamTypes = PAUtil.getParamTypes(hm).iterator();
	Iterator<Temp>   itParamTemps = PAUtil.getParamTemps(hm, pa.getCodeFactory()).iterator();
	Iterator<PANode> itParamNodes = pa.getNodeRep().getParamNodes(hm).iterator();
	Iterator<String> itParamNames = PAUtil.getParamNames(hm).iterator();

	while(itParamTypes.hasNext()) {
	    HClass paramType = itParamTypes.next();
	    Temp   paramTemp = itParamTemps.next();
	    String paramName = itParamNames.next();
	    if(paramType.isPrimitive()) continue;

	    PANode paramNode = itParamNodes.next();
	    state2trans.get(startState).add
		(new Pair<PANode,MLabel>(paramNode,
					 new ParamMLabel(paramTemp, paramName))); 
	}
    }


    private void addOutsideEdgesTrans(final Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				      InterProcAnalysisResult ipar) {
	ipar.eomO().forAllEdges(new PAEdgeSet.EdgeAction() {
	    public void action(PANode src, HField hf, PANode dst) {
		state2trans.get(src).add(new Pair<PANode,MLabel>(dst, field2mlabel(hf)));
	    }
	});
    }


    private void addMutationTrans(Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				  InterProcAnalysisResult ipar) {
	for(Pair<PANode,HField> abstrField : ipar.eomWrites()) {
	    PANode node = abstrField.left;
	    HField hf   = abstrField.right;
	    assert !((node == null) && (hf == null));

	    PANode state = 
		// for mutated static fields, the transition starts in the startState
		(node == null) ?
		startState :
		// for instance fields, it starts in the corresponding PANode
		node;

	    state2trans.get(state).add(new Pair<PANode,MLabel>(acceptState, field2mlabel(hf)));
	}
    }


    private void addGblEscTrans(Map<PANode,List<Pair<PANode,MLabel>>> state2trans,
				InterProcAnalysisResult ipar) {
	for(PANode node : ipar.eomAllGblEsc()) {
	    state2trans.get(node).add(new Pair<PANode,MLabel>(node, new ReachMLabel()));
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
}
