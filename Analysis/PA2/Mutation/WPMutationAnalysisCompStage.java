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
 * @version $Id: WPMutationAnalysisCompStage.java,v 1.4 2005-09-05 16:38:57 salcianu Exp $
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
    private MutationAnalysis ma;
    
    public void real_action() {
	pa = (PointerAnalysis) attribs.get("pa");
	assert pa != null : "cannot find the pointer analysis";
	ma = new MutationAnalysis(pa);

	for(HMethod hm : pa.getCallGraph().transitiveSucc(Collections.<HMethod>singleton(mainM))) {
	    if(hcf.convert(hm) == null) continue;
	    displayInfo(hm);
	    System.out.println();
	}
    }


    private void displayInfo(HMethod hm) {
	System.out.println(hm);
	try {
	    if(ma.isPure(hm)) {
		System.out.println("PURE");
	    }
	    else {
		System.out.println("Mutated fields = " + ma.getMutatedAbstrFields(hm));
		System.out.println("RegExp = " + ma.getMutationRegExp(hm));
	    }

	    List<ParamInfo> safeParams = ma.getSafeParams(hm);
	    //if(!safeParams.isEmpty()) {
		System.out.print("PARAMS: ");
		boolean first = true;
		for(ParamInfo pi : MAUtil.getParamInfo(hm, pa)) {
		    if(!first) {
			System.out.print(", ");
		    }
		    if(safeParams.contains(pi)) {
			System.out.print("[safe] ");
		    }
		    System.out.print(pi.type().getName() + " " + pi.declName());
		    first = false;
		}
		System.out.println();
		//}
	}
	catch(NoAnalysisResultException e) {
	    System.out.println("Unanalyzed");
	}
    }

}
