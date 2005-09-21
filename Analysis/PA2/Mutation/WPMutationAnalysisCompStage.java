// WPMutationAnalysisCompStage.java, created Wed Aug 31 13:23:51 2005 by salcianu
// Copyright (C) 2005 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Set;
import java.util.Collection;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;
import jpaul.Misc.BoolMCell;
import jpaul.Misc.Predicate;

import harpoon.Main.CompilerStageEZ;
import harpoon.Util.Options.Option;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.AnalysisPolicy;
import harpoon.Analysis.PA2.PANode;
import harpoon.Analysis.PA2.Flags;
import harpoon.Analysis.PA2.PAUtil;

import harpoon.Analysis.IOEffectAnalysis;

import harpoon.Util.Util;

/**
 * <code>WPMutationAnalysisCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WPMutationAnalysisCompStage.java,v 1.11 2005-09-21 23:03:33 salcianu Exp $
 */
public class WPMutationAnalysisCompStage extends CompilerStageEZ {

    public WPMutationAnalysisCompStage(BoolMCell paEnabler) {
	super("wp-mutation");
	this.paEnabler = paEnabler;
    }

    private final BoolMCell paEnabler;

    public List<Option> getOptions() {
	List<Option> opts = new LinkedList<Option>();
	opts.add(new Option("wp-mutation", "", "regexp", "Whole Program Mutation Analysis.  By default, presents the purity status of each method, and the safe parameters; if the optional arg \"regexp\" is present also present the regular expressions that cover all mutated heap locations.") {
	    public void action() {
		WP_MUTATION_ANALYSIS = true;
		Flags.RECORD_WRITES  = true;
		// turn on the pointer analysis
		paEnabler.value = true;
		if(getOptionalArg(0) != null) {
		    assert 
			getOptionalArg(0).equals("regexp") : 
			"Unknown wp-mutation optional argument \"" + getOptionalArg(0) + "\"";
		    if(getOptionalArg(0).equals("regexp")) {
			SHOW_REGEXP = true;
		    }
		}
	    }
	});
	return opts;
    }

    private boolean WP_MUTATION_ANALYSIS = false;
    private boolean SHOW_REGEXP = false;

    public boolean enabled() {
	return WP_MUTATION_ANALYSIS;
    }

    private PointerAnalysis pa;
    private MutationAnalysis ma;
    private IOEffectAnalysis io;
    
    public void real_action() {
	pa = (PointerAnalysis) attribs.get("pa");
	assert pa != null : "cannot find the pointer analysis";
	ma = new MutationAnalysis(pa);
	io = new IOEffectAnalysis(pa.getCallGraph());

	Collection<HMethod> methodsOfInterest = methodsOfInterest();

	// time pointer analysis (before doing any regexps etc, such
	// that we can time it accurately).
	AnalysisPolicy ap = new AnalysisPolicy(Flags.FLOW_SENSITIVITY, -1, Flags.MAX_INTRA_SCC_ITER);
	PAUtil.timePointerAnalysis(mainM, methodsOfInterest, pa, ap, "");

	Relation<HClass,HMethod> class2methods = new MapSetRelation<HClass,HMethod>();
	
	for(HMethod hm : methodsOfInterest()) {
	    if(hcf.convert(hm) == null) continue;
	    if(hm.getDeclaringClass().isArray()) continue;
	    class2methods.add(hm.getDeclaringClass(), hm);
	}
	
	Stat sLib  = displayInfo("LIBRARY", class2methods, isLibClass);
	Stat sUser = displayInfo("USER", class2methods, Predicate.NOT(isLibClass));

	System.out.println("\nMUTATION STATISTICS:");
	System.out.println("LIB. CODE: " + sLib);
	System.out.println("USER CODE: " + sUser);
	System.out.println("TOTAL:     " + Stat.sum(sLib, sUser));
	System.out.println();

	// enable some GC
	pa = null;
	ma = null;
	io = null;
    }


    /** Returns the methods we want to analyze.  We completely ignore
        certain big and otherwise uninteresting methods.  */
    private Collection<HMethod> methodsOfInterest() {
	//return pa.getCallGraph().transitiveSucc(Collections.<HMethod>singleton(mainM));

	return 
	    PAUtil.interestingMethods
	    (Collections.<HMethod>singleton(mainM),
	     pa.getCallGraph(),
	     // undesirable methods wqe do not want to consider
	     new Predicate<HMethod>() {
		public boolean check(HMethod hm) {
		    return PAUtil.methodTooBig(hm);
		}
	    });
    }


    private static final String[] libPackageNames = new String[] {
	"java",
	"sun",
	"gnu"
    };
    Predicate<HClass> isLibClass = new Predicate<HClass>() {
	public boolean check(HClass hclass) {
	    String className = hclass.getName();
	    for(String libPackageName : libPackageNames) {
		if(className.startsWith(libPackageName)) {
		    return true;
		}
	    }
	    return false;
	}
    };


    private static class Stat {
	int nbMethods = 0;
	int nbPureMethods = 0;
	int nbParams = 0;
	int nbObjParams = 0;
	int nbSafeParams = 0;

	public String toString() {
	    return 
		"Pure methods: " + prop(nbPureMethods, nbMethods) +
		" ;\tSafe obj. params: " + prop(nbSafeParams, nbObjParams) +
		" (" + nbParams + " total params)";
	}

	private String prop(int nbProp, int nbAll) {
	    return 
		nbProp + " / " + nbAll + " = " +
		Util.percentage((double) nbProp, (double) nbAll);
	}

	static Stat sum(Stat s1, Stat s2) {
	    Stat sum = new Stat();
	    sum.nbMethods = s1.nbMethods + s2.nbMethods;
	    sum.nbPureMethods = s1.nbPureMethods + s2.nbPureMethods;
	    sum.nbParams  = s1.nbParams + s2.nbParams;
	    sum.nbObjParams  = s1.nbObjParams  + s2.nbObjParams;
	    sum.nbSafeParams = s1.nbSafeParams + s2.nbSafeParams;
	    return sum;
	}
    }


    private Stat displayInfo(String tag, Relation<HClass,HMethod> class2methods, Predicate<HClass> pred) {
	Stat stat = new Stat();
	System.out.println("\n\n" + tag + " CLASSES");
	for(HClass hClass : class2methods.keys()) {
	    if(!pred.check(hClass)) continue;

	    System.out.println(hClass.getName() + " ");
	    for(HMethod hm : class2methods.getValues(hClass)) {
		displayInfo(hm, "  ", stat);
		System.out.println();
	    }
	    System.out.println("}\n");
	}
	return stat;
    }


    private void displayInfo(HMethod hm, String indent, Stat stat) {
	stat.nbMethods++;
	try {
	    List<ParamInfo> safeParams = ma.getSafeParams(hm);
	    for(ParamInfo pi : MAUtil.getParamInfo(hm, pa)) {
		stat.nbParams++;
		if(pi.type().isPrimitive()) continue;
		stat.nbObjParams++;
		if(safeParams.contains(pi)) {
		    stat.nbSafeParams++;
		}
	    }

	    System.out.println(indent + MAUtil.methodNameWithSafeAnnot(hm, safeParams, pa));

	    if(ma.isPure(hm)) {
		if(!io.doesIO(hm)) {
		    System.out.println(indent + "PURE");
		    stat.nbPureMethods++;
		}
		else {
		    System.out.println(indent + "HEAP PURE BUT DOES IO");
		}
	    }
	    else {
		System.out.print(indent + "NOT HEAP PURE");
		if(io.doesIO(hm)) {
		    System.out.print("; ALSO DOES IO");
		}
		System.out.println();
		//System.out.println(indent + "Mutated fields = " + ma.getMutatedAbstrFields(hm));
		if(SHOW_REGEXP) {
		    System.out.println(indent + "MutRegExp = " + ma.getMutationRegExp(hm));
		}
	    }
	}
	catch(NoAnalysisResultException e) {
	    System.out.println(indent + "WARNING: UNANALYZED");
	    for(HClass hClass : PAUtil.getParamTypes(hm)) {
		stat.nbParams++;
		if(!hClass.isPrimitive()) stat.nbObjParams++;
	    }
	}
    }

}
