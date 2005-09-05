// MutationAnalysis.java, created Fri Sep  2 13:33:57 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

import jpaul.RegExps.RegExp;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

import harpoon.Temp.Temp;

import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.AnalysisPolicy;
import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.Flags;
import harpoon.Analysis.PA2.PAEdgeSet;

/**
 * <code>MutationAnalysis</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MutationAnalysis.java,v 1.2 2005-09-05 16:38:57 salcianu Exp $
 */
public class MutationAnalysis {

    /** Creates a <code>MutationAnalysis</code> query object.

	@param pa Underlying pointer analysis. */
    public MutationAnalysis(PointerAnalysis pa) {
	this.pa = pa;
    }

    // Underlying pointer analysis.
    private final PointerAnalysis pa;
    // Ideally, the pointer analaysis will support several analysis
    // policies.  For the time being, we ignore this and just extract
    // whatever best analysis result we currently have.
    private final AnalysisPolicy ap = null; // = the best currently available result

    /** Checks whether <code>hm</code> is a pure methods, according to
        the JML definition. */
    public boolean isPure(HMethod hm) throws NoAnalysisResultException {
	InterProcAnalysisResult ipar = getIPAR(hm);
	return ipar.eomWrites().isEmpty();
    }

    public RegExp<MLabel> getMutationRegExp(HMethod hm) throws NoAnalysisResultException {
	InterProcAnalysisResult ipar = getIPAR(hm);
	MutationNFA nfa = new MutationNFA(hm, ipar, pa);
	return nfa.simplify().toRegExp();
    }

    public Collection<Pair<PANode,HField>> getMutatedAbstrFields(HMethod hm) throws NoAnalysisResultException {
	InterProcAnalysisResult ipar = getIPAR(hm);
	return ipar.eomWrites();
    }


    private InterProcAnalysisResult getIPAR(HMethod hm) throws NoAnalysisResultException {
	InterProcAnalysisResult ipar = pa.getInterProcResult(hm, ap);
	if(ipar == null) throw new NoAnalysisResultException();

	boolean isConstructor = hm instanceof HConstructor;
	PANode thisNode = 
	    isConstructor ? 
	    pa.getNodeRep().getParamNodes(hm).get(0) :
	    null;
	
	final Set<Pair<PANode,HField>> adjustedWrites = new HashSet<Pair<PANode,HField>>();
	for(Pair<PANode,HField> af : ipar.eomWrites()) {
	    // for constructors, ignore mutations on the "this" parameter
	    if(Flags.IGNORE_CONSTR_MUTATION_ON_THIS && 
	       isConstructor && (af.left == thisNode)) continue;
	    // ignore mutation on certain fields
	    if(Flags.IGNORE_CERTAIN_MUTATIONS && canIgnore(af)) continue;
	    // o.w., we really need to consider the mutation
	    adjustedWrites.add(af);
	}

	return new InterProcAnalysisResult.Chained(ipar) {
	    public Set<Pair<PANode,HField>> eomWrites() {
		return adjustedWrites;
	    }
	};
    }


    private boolean canIgnore(Pair<PANode,HField> af) {
	HField hf = af.right;
	if(hf == null) return false;
	
	if(allowedMutatedFields == null) {
	    Linker linker = hf.getType().getLinker();
	    allowedMutatedFields = new HashSet<HField>();
	    for(int i = 0; i < amfArray.length; i++) {
		String className = amfArray[i][0];
		String fieldName = amfArray[i][1];
		allowedMutatedFields.add(linker.forName(className).getField(fieldName));
	    }
	}

	return allowedMutatedFields.contains(hf);
    }

    private Set<HField> allowedMutatedFields;
    private final String[][] amfArray = {
	{"java.lang.FloatingDecimal", "b5p"}
    };



    public List<ParamInfo> getSafeParams(HMethod hm) throws NoAnalysisResultException {
	InterProcAnalysisResult ipar = getIPAR(hm);

	// nonSafe is the set of nodes that (1) are mutated, (2)
	// escape globally, or (3) a new, externally visible, inside
	// edge is created toward them.
	final Set<PANode> nonSafe = new HashSet<PANode>();
	for(Pair<PANode,HField> af : ipar.eomWrites()) {
	    if(af.left != null) {
		nonSafe.add(af.left);
	    }
	}
	nonSafe.addAll(ipar.eomAllGblEsc());
	ipar.eomI().forAllEdges(new PAEdgeSet.EdgeAction() {
	    public void action(PANode src, HField hf, PANode dst) {
		nonSafe.add(dst);
	    }
	});
	// returning / throwing a node creates new externally visible aliasing to it
	nonSafe.addAll(ipar.ret());
	nonSafe.addAll(ipar.ex());

	List<ParamInfo> safeParams = new LinkedList<ParamInfo>();
	for(ParamInfo pi : MAUtil.getParamInfo(hm, pa)) {
	    if(pi.type().isPrimitive()) continue;
	
	    Set<PANode> reachable = ipar.eomI().transitiveSucc(pi.node());
	    if(DSUtil.disjoint(reachable, nonSafe)) {
		safeParams.add(pi);
	    }
	}

	return safeParams;
    }    

}
