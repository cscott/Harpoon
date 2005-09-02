// WPMutationAnalysisCompStage.java, created Wed Aug 31 13:23:51 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Set;

import jpaul.DataStructs.Pair;
import jpaul.Misc.BoolMCell;

import harpoon.Main.CompilerStageEZ;
import harpoon.Util.Options.Option;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;

import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.AnalysisPolicy;
import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.Flags;

/**
 * <code>WPMutationAnalysisCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WPMutationAnalysisCompStage.java,v 1.2 2005-09-02 19:54:58 salcianu Exp $
 */
public class WPMutationAnalysisCompStage extends CompilerStageEZ {

    public WPMutationAnalysisCompStage(BoolMCell paEnabler) {
	super("wp-mutation");
	this.paEnabler = paEnabler;
    }

    private final BoolMCell paEnabler;

    public List<Option> getOptions() {
	List<Option> opts = new LinkedList<Option>();
	opts.add(new Option("wp-mutation", "Whole Program Mutation Analysis") {
	    public void action() {
		WP_MUTATION_ANALYSIS = true;
		Flags.RECORD_WRITES  = true;
	    }
	});
	return opts;
    }

    private boolean WP_MUTATION_ANALYSIS = false;

    public boolean enabled() {
	return WP_MUTATION_ANALYSIS;
    }

    private PointerAnalysis pa;
    
    public void real_action() {
	pa = (PointerAnalysis) attribs.get("pa");
	AnalysisPolicy  ap = null;  // = the best currently available result
	for(HMethod hm : pa.getCallGraph().transitiveSucc(Collections.<HMethod>singleton(mainM))) {
	    InterProcAnalysisResult ipar = pa.getInterProcResult(hm, ap);
	    if(ipar == null) {
		//System.out.println("WARNING: no analysis result for " + hm);
		//System.out.println();
		continue;
	    }
	    
	    displayInfo(hm, ipar);
	}
    }


    private void displayInfo(HMethod hm, InterProcAnalysisResult ipar) {
	Set<Pair<PANode,HField>> mutatedAbstrFields = ipar.eomWrites();

	System.out.println(hm);
	if(mutatedAbstrFields.isEmpty()) {
	    System.out.println("PURE");
	}
	else {
	    System.out.println(mutatedAbstrFields);
	    System.out.print("RegExp = ");
	    MutationNFA nfa = new MutationNFA(hm, ipar, pa);
	    //System.out.println("nfa = " + nfa);
	    //System.out.println("nfa states = " + nfa.states());
	    //System.out.println("Unsimplified = " + nfa.toRegExp());
	    //System.out.println("  Simplified = " + nfa.simplify().toRegExp());
	    System.out.println(nfa.simplify().toRegExp());
	}
	
	System.out.println();
    }

}
