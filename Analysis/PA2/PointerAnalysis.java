// PointerAnalysis.java, created Wed Jul  6 10:48:23 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.CallGraph;

import harpoon.IR.Quads.CALL;

/**
 * <code>PointerAnalysis</code> attemps to give a specification for
 * the different pointer analysis implementations.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: PointerAnalysis.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $ */
public abstract class PointerAnalysis {

    /** The simplified analysis result for the end of the method
        <code>hm</code>.  This result is used in the inter-procedural
        analysis.  In addition, it seems to be enough for certain
        optimizations (i.e., stack-allocation).

	@param hm Method to analyze.
	
	@param ap Analysis policy (e.g., flow sensitivity or not?)
	This is only a HINT: different analysis implementations may
	ignore certain elements of the analysis policy.  If
	<code>null</code>, the analysis will return whatever is the
	best analysis result it already has computed
	(<code>null</code> if no such result).

	@see InterProcAnalysisResult */
    public abstract InterProcAnalysisResult getInterProcResult(HMethod hm, AnalysisPolicy ap);


    /** Similar to <code>getInterProcResult</code> but returns all
        information the pointer analysis is able to infer about
        <code>hm</code>. 

	@see PointerAnalysis#InterProcAnalysisResult */
    public abstract FullAnalysisResult getFullResult(HMethod hm, AnalysisPolicy ap);


    /** @return Call graph used by <code>this</code>
        <code>PointerAnalysis</code> to interpret the virtual calls in
        the inter-procedural analysis.  */
    public abstract CallGraph getCallGraph();

    /** @return Code factory used to generate the code of the analyzed methods. */
    public abstract CachingCodeFactory   getCodeFactory();


    /** @return <code>NodeRepository</code> object that maps between
        nodes and the instructions they were introduced for: e.g.,
        will give the inside node for a specific NEW instruction. */
    public abstract NodeRepository getNodeRep();


    /** @return CURRENT inter-procedural analysis result for
        <code>hm</code> CURRENT = at the current iteration of the
        inter-procedural fixed-point solver.  This method is intended
        to be called only from the <code>CallConstraint</code>; it
        should NOT be called from an analysis client (hence, the
        package protection level). */
    abstract InterProcAnalysisResult getCurrentInterProcResult(HMethod hm);


    /** Checks whether the call <code>cs</code> should be analyzed or
        not.  This method is used by the constraints for the
        inter-procedural analysis: if a fixed-point cannot be reached
        in a certain number of iterations, all calls to methods from
        the same strongly-connected component of the call graph will
        be treated as unanalyzable calls.  The constraint for
        <code>CALL</code> will interrogate the
        <code>PointerAnalysis</code> (responsible with the overall
        fixed-point) whether a call should be analyzed or not. */
    abstract boolean shouldSkipDueToFPLimit(CALL cs, HMethod callee);


    /** Checks whether <code>hm</code> can be analyzed.  The default
        implementation returns <code>true</code> unless the method is
        native or abstract.  */
    public boolean isAnalyzable(HMethod hm) {
	return !PAUtil.isNative(hm) && !PAUtil.isAbstract(hm);
    }


    /** Checks whether the call <code>cs</code> from
        <code>caller</code> to <code>callee</code> has been analyzed
        or not.  Why is this important?  For speed reasons, the
        pointer analysis may have decided to ignore certain calls.
        However, analysis clients must be aware of this; e.g., this
        information is important if we perform inlining in order to
        enhance stack-allocation.  */
    public abstract boolean hasAnalyzedCALL(HMethod caller, CALL cs, HMethod callee);

}
