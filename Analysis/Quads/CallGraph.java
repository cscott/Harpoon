// CallGraph.java, created Tue Mar 21 16:13:06 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;

/**
 * <code>CallGraph</code> is a general interface that should be
 implemented by a call graph.
 * 
 * @author  Alexandru SALCIANU <salcianu@mit.edu>
 * @version $Id: CallGraph.java,v 1.3 2002-04-11 04:28:49 salcianu Exp $
 */
public interface CallGraph extends harpoon.Analysis.CallGraph {
    // XXX: to talk about a CALL quad, you really need to have the HCode
    //      for context.  An HCode should be a parameter in these methods;
    //      the only reason implementations of this interface generally work
    //      is that the HCode is generally coming from a CachedCodeFactory
    //      so is consistent over the various CallGraph method calls. [CSA]

    /** Returns an array containing  all possible methods called by 
	method <code>m</code> at the call site <code>cs</code>.
	If there is no known callee for the call site <code>cs</code>, or if 
	<code>cs</code> doesn't belong to the code of <code>hm</code>,
	return an array of length <code>0</code>. */
    public HMethod[] calls(final HMethod hm, final CALL cs);

    /** Returns a list of all the <code>CALL</code>s quads in the code 
	of <code>hm</code>. */
    public CALL[] getCallSites(final HMethod hm);

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public Set callableMethods();
}
