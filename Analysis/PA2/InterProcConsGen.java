// InterProcConsGen.java, created Sun Jul 10 07:35:16 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import jpaul.Constraints.Constraint;
import jpaul.Constraints.LtConstraint;
import jpaul.Constraints.CtConstraint;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.Linker;

import harpoon.Temp.Temp;
import harpoon.Util.Util;

import harpoon.IR.Quads.CALL;

import harpoon.Analysis.Quads.CallGraph;

/**
 * <code>InterProcConsGen</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: InterProcConsGen.java,v 1.8 2005-09-20 20:01:43 salcianu Exp $
 */
class InterProcConsGen {

    InterProcConsGen(IntraProc intraProc, PointerAnalysis pa) {
	this.intraProc = intraProc;
	this.pa = pa;
    }

    private final IntraProc intraProc;
    private final PointerAnalysis pa;


    Collection<Constraint> treatCALL(CALL cs) {
	// System.out.println("treatCALL(" + cs + ")");

	newCons = new LinkedList<Constraint>();

	LinkedList<LVar> paramVars = new LinkedList<LVar>();
	int k = 0;
	if(!cs.isStatic()) {
	    // add variable for receiver
	    paramVars.addLast(intraProc.lVar(cs.params()[k]));
	    k++;
	}

	for(HClass paramType : cs.method().getParameterTypes()) {
	    // add variables from non-primitive arguments
	    if(!paramType.isPrimitive()) {
		LVar argVar = intraProc.lVar(cs.params()[k]);
		LVar filteredVar = new LVar();
		newCons.add(new TypeFilterConstraint(argVar, paramType, filteredVar));
		paramVars.addLast(filteredVar);
	    }
	    k++;
	}
	
	HMethod[] callees = pa.getCallGraph().calls(PAUtil.getMethod(cs), cs);

	if(SpecialInterProc.modelCALL(cs, paramVars, intraProc, newCons)) {
	    return newCons;
	}

	// Deal with calls to unanalyzable calls
	if(unanalyzableCALL(cs, callees)) {
	    treatUnanalyzableCALL(cs, paramVars);
	    return newCons;
	}

	newCons.add(new LtConstraint(intraProc.preIVar(cs), intraProc.postIVar(cs)));
	newCons.add(new LtConstraint(intraProc.preFVar(cs), intraProc.postFVar(cs)));

	for(HMethod callee : callees) {
	    // System.out.println("callee = " + callee);
	    if(SpecialInterProc.model(cs, callee, paramVars, intraProc, newCons)) {
		continue;
	    }
	    // notify the underlying pointer analysis that we really analyze the call cs to callee
	    pa.notifyAnalyzedCALL(cs, callee);
	    generateConstraints(cs, callee, paramVars);
	}

	return newCons;
    }

    private Collection<Constraint> newCons;


    /** Checks whether the pointer analysis will always consider
        unanalyzable a certain call site.  During its execution, the
        analysis may decide to skip even more call sites (e.g., if the
        fixed-point solver does not finish for a certain set of
        mutually recursive methods in a certain # of iterations, it
        may decide to consider unanalyzable all calls to same-SCC
        methods).  */
    static boolean clearlyUnanalyzableCALL(CALL cs, HMethod[] callees) {
	// almost all exceptions escape -> do not waste any precision on them.
	if(PAUtil.exceptionInitializerCall(cs))
	    return true;

	// pretty intuitive: that's what unanalyzable CALL usually means!
	if((callees != null) && containsUnanalyzable(callees))
	    return true;

	for(int i = 0; i < callees.length; i++) {
	    if(PAUtil.methodTooBig(callees[i]))
		return true;
	}

	// Refuse to analyze CALLs with too many callees
	if((callees != null) && (callees.length > Flags.MAX_CALLEES_PER_ANALYZABLE_SITE)) {
	    if(Flags.VERBOSE_CALL)
		System.out.println("Too many callees (" + callees.length + ") for cs = " + Util.code2str(cs));
	    return true;
	}

	return false;
    }


    private boolean unanalyzableCALL(CALL cs, HMethod[] callees) {
	if(clearlyUnanalyzableCALL(cs, callees))
	    return true;

	if(pa.shouldSkipDueToFPLimit(cs, null)) {
	    if(Flags.VERBOSE_CALL) {
		System.out.println("Should skip due to FPLimit cs = " + Util.code2str(cs));
	    }
	    return true;
	}

	return false;
    }



    /** generate constraints for an unanalyzable call */
    private void treatUnanalyzableCALL(CALL cs, List<LVar> paramVars) {
	if(Flags.SHOW_SKIPPED_UNANALYZABLE) {
	    System.out.println("SKIP UNANALYZABLE " + Util.code2str(cs));
	}

	// all inside edges continue to exist
	newCons.add(new LtConstraint(intraProc.preIVar(cs), intraProc.postIVar(cs)));
	// all nodes that escape before the call, still escape
	newCons.add(new LtConstraint(intraProc.preFVar(cs), intraProc.postFVar(cs)));

	// This FVar collects all nodes that escape directly into this call.
	// NOTE: for technical reasons we use an LVar (it's anyway a NodeSetVar like FVar)
	// (o.w., the call to the constructor of StaticStoreConstraintF below would not typecheck)
	LVar dirGblEscHere = new LVar();
	
	// in addition, all nodes pointed-to by parameters escape irremediably
	for(LVar v : paramVars) {
	    newCons.add(new LtConstraint(v, dirGblEscHere));
	    newCons.add(new LtConstraint(v, intraProc.postFVar(cs)));
	}

	// dirGblEscHere nodes are added to intraProc.
	newCons.add(new LtConstraint(dirGblEscHere, intraProc.eVar()));

	// in addition, all nodes reachable from them are marked as escaped
	// (we shamelessly use the StaticStoreConstraintF that does the same thing)
	newCons.add(new StaticStoreConstraintF(dirGblEscHere,
					       intraProc.preIVar(cs),
					       intraProc.preFVar(cs),
					       intraProc.postFVar(cs)));
	
	// an unanalyzable method may return anything
	makeItPoint(cs.retval(), intraProc.getNodeRep().getGlobalNode());
	makeItPoint(cs.retex(),  intraProc.getNodeRep().getGlobalNode());
    }


    private void makeItPoint(Temp t, PANode node) {
	if(t != null) {
	    LVar v = intraProc.lVar(t);
	    newCons.add(new CtConstraint(Collections.<PANode>singleton(node),
					 v));
	}
    }
    

    private static boolean containsUnanalyzable(HMethod[] callees) {
	for(HMethod callee : callees) {
	    if(PAUtil.isNative(callee) && !SpecialInterProc.canModel(callee)) {
		//System.out.println("Unanalyzable " + callee + " desc = " + callee.getDescriptor());
		return true;
	    }
	}
	return false;
    }


    private void generateConstraints(CALL cs, HMethod callee, LinkedList<LVar> paramVars) {
	if(!cs.isStatic()) {
	    LVar lReceiver = paramVars.get(0);
	    LVar filteredReceiver = new LVar();

	    // Filter the set of possible receiver nodes, according to
	    // the class that declares callee.
	    // This filtering has to be done for each individual callee
	    newCons.add(new TypeFilterConstraint(lReceiver,
						 callee.getDeclaringClass(),
						 filteredReceiver));

	    paramVars = (LinkedList<LVar>) paramVars.clone();
	    paramVars.set(0, filteredReceiver);
	}

	newCons.add(new CallConstraint(cs,
				       callee,
				       intraProc.lVar(cs.retval()),
				       intraProc.lVar(cs.retex()),
				       paramVars,
				       intraProc.preIVar(cs),
				       intraProc.preFVar(cs),
				       intraProc.postIVar(cs),
				       intraProc.postFVar(cs),
				       intraProc.oVar(),
				       intraProc.eVar(),
				       pa,
				       intraProc.vWrites()));
    }

}
