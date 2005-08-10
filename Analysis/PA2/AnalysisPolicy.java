// AnalysisPolicy.java, created Tue Jul  5 13:04:06 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

/**
 * <code>AnalysisPolicy</code> groups together different pointer
 * analysis options.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: AnalysisPolicy.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $ */
public class AnalysisPolicy {

    public AnalysisPolicy(boolean flowSensitivity, int staticCallDepth, int fpMaxIter) {
	this.flowSensitivity = flowSensitivity;
	assert staticCallDepth >= -1;
	this.staticCallDepth = staticCallDepth;
	assert fpMaxIter >= -1;
	this.fpMaxIter = fpMaxIter;
    }

    /** Selects flow sensitivity: if on, the analysis will compute one
        distinct set of inside edges for each program point; o.w., the
        analysis will compute one set of inside edges per method.  */
    public final boolean flowSensitivity;


    /** Lower bound of the static call depth.  When analyzing method
        <code>m</code>, any method that is not transitively called in
        at most "static call depth" steps is considered un-analyzable.

	-1 indicates infinite call depth: analyze all callees. */
    public final int staticCallDepth;


    /** Maximum numbers of iterations in each inter-proc. fixed-point
        equations, i.e., how many times a method may be re-examined
        before the fixed-point computations gives up and considers
        that all calls to that method are unanalyzable. 

	-1 indicates an infinite number of iterations, i.e., the most
	precise fixed-point. */
    public final int fpMaxIter;

    
    /** Checks whether <code>this</code> analysis policy is more
        precise than <code>ap2</code>, i.e., that each of the three
        precision parameters are better. */
    public boolean morePrecise(AnalysisPolicy ap2) {
	assert ap2 != null;
	return 
	    implies(ap2.flowSensitivity, this.flowSensitivity) &&
	    (ap2.staticCallDepth <= this.staticCallDepth) &&
	    (ap2.fpMaxIter <= this.fpMaxIter);	    
    }

    private static boolean implies(boolean a, boolean b) {
	return !a || b;
    }


    public String toString() {
	return 
	    "<" + 
	    (flowSensitivity ? "FS" : "FI") + "," +
	    staticCallDepth + "," +
	    fpMaxIter + 
	    ">";
    }


    public int hashCode() {
	return 
	    (flowSensitivity ? 1 : 0) + 
	    staticCallDepth +
	    fpMaxIter;
    }

    public boolean equals(Object o) {
	if(! (o instanceof AnalysisPolicy)) return false;
	AnalysisPolicy ap2 = (AnalysisPolicy) o;
	return 
	    this.morePrecise(ap2) && 
	    ap2.morePrecise(this);
    }
    
}
