// MustParamOracle.java, created Wed Nov  7 15:39:11 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SIGMA;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A <code>MustParamOracle</code> tells you what method variables
 * *must* contain the values passed in as parameters to the method.
 * This can tell you which values must be the 'this' object, for
 * non-static methods, or which values in (say) a field-set
 * operation directly correspond to constructor parameters.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MustParamOracle.java,v 1.1.2.2 2001-11-07 22:25:07 cananian Exp $
 */
public class MustParamOracle {
    // could make this an invertibleMap if you want sets.
    final Map results = new HashMap();

    /** Returns <code>true</code> iff the given <code>Temp</code> *must*
     *  contain the value of some parameter. */
    boolean isMustParam(Temp t) { return results.containsKey(t); }
    /** Returns the parameter which the given <code>Temp</code> *must*
     *  contain the value of. */
    int whichMustParam(Temp t) { return ((Integer)results.get(t)).intValue(); }
    
    /** Creates a <code>MustParamOracle</code> which gives you information
     *  on variables in <code>HCode</code> <code>hc</code> (which should
     *  be in SSI or SSA form, for best results). */
    public MustParamOracle(HCode hc) {
	// compute # of method parameters
	METHOD method = (METHOD) ((HEADER)hc.getRootElement()).next(1);
	int nparam = method.paramsLength();
        // for each parameter...
	for (int i=0; i<nparam; i++) {
	    // do analysis.
	    OracleVisitor ov = new OracleVisitor(hc, i);
	    // transfer results into more compact form.
	    Integer paramNum = new Integer(i);
	    for (Iterator it=ov.paramvars.iterator(); it.hasNext(); ) {
		Object old = results.put((Temp)it.next(), paramNum);
		Util.assert(old==null, "temp can't be multiple params!");
	    }
	    // and do next.
	}
	// done!
    }

    static class OracleVisitor extends QuadVisitor {
	final int which_param; // which parameter are we interested in?
	/** an array of sets: one for each method parameter.  the sets
	 *  contain all variables which 'may' contain the method parameter. */
	final Set paramvars = new HashSet();
	/** an array of sets, as above.  the sets in this case contain
	 *  all variables which *may not* contain the method parameter. */
	final Set notparamvars = new HashSet();
	// the paramvars set minus the notparamvars set is the
	// set of variables which *must* contain the method parameter.

	/** The 'relevant' set consists of those quads which change the
	 *  paramvars or notparamvars sets.  We can save time in our
	 *  dataflow analysis by only iterating over these. */
	final Set relevant = new HashSet();

	OracleVisitor(HCode hc, int which) {
	    this.which_param = which;
	    // once through all elements.
	    for (Iterator it=hc.getElementsI(); it.hasNext(); )
		((Quad)it.next()).accept(this);
	    // iterate through relevant elements until the sets stop
	    // changing size.
	    int oldsize = 0, size = paramvars.size() + notparamvars.size();
	    while (size > oldsize) {
		for (Iterator it=relevant.iterator(); it.hasNext(); )
		    ((Quad)it.next()).accept(this);
		oldsize = size;
		size = paramvars.size() + notparamvars.size();
	    }
	    // clean up paramvars set by removing not-param variables.
	    paramvars.removeAll(notparamvars);
	    // done!
	}
	// lattice: don't know, this, not-this.  -->move-->
	// presence in 'not-this' overrides presence in 'this'.
	// sets can only grow.
	public void visit(Quad q) {
	    /* look for overwrites, which are always not 'this' */
	    notparamvars.addAll(q.defC());
	}
	public void visit(METHOD q) {
	    // param 'which' is 'param'; all others are 'not-param'
	    for (int i=0; i<q.paramsLength(); i++)
		if (i==which_param)
		    paramvars.add(q.params(i));
		else
		    notparamvars.add(q.params(i));
	}
	public void visit(MOVE q) {
	    relevant.add(q);
	    if (paramvars.contains(q.src()) &&
		!notparamvars.contains(q.src()))
		paramvars.add(q.dst());
	    else
		notparamvars.add(q.dst());
	}
	public void visit(SIGMA q) {
	    relevant.add(q);
	    // get rid of overwrites.
	    Set s = new HashSet(q.defC());
	    for (int i=0; i<q.numSigmas(); i++)
		s.removeAll(Arrays.asList(q.dst(i)));
	    notparamvars.addAll(s);
	    // now look for src==this.
	    for (int i=0; i<q.numSigmas(); i++)
		if (notparamvars.contains(q.src(i)))
		    notparamvars.addAll(Arrays.asList(q.dst(i)));
		else if (paramvars.contains(q.src(i)))
		    // found! add all dst to paramvars.
		    paramvars.addAll(Arrays.asList(q.dst(i)));
	}
	public void visit(PHI q) {
	    relevant.add(q);
	    // phi(x,y) is 'param' iff x *and* y are 'param' and
	    // neither x nor y is *not* 'param'
	    for (int i=0; i<q.numPhis(); i++)
		for (int j=0; j<q.arity(); j++)
		    if (notparamvars.contains(q.src(i, j)))
			notparamvars.add(q.dst(i));
		    else if (paramvars.contains(q.src(i, j)))
			paramvars.add(q.dst(i));
	}
    }
}
