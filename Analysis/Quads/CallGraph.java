// CallGraph.java, created Tue Mar 21 16:13:06 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;

/**
 * <code>CallGraph</code> is a general interface that should be
 implemented by a call graph.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: CallGraph.java,v 1.1.2.6 2000-03-22 05:23:09 salcianu Exp $
 */
public interface CallGraph {
    
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public HMethod[] calls(final HMethod hm);

    /** Returns an array containing  all possible methods called by 
	method <code>m</code> at the call site <code>cs</code>.
	If there is no known callee for the call site <code>cs>/code>, or if 
	<code>cs</code> doesn't belong to the code of <code>hm</code>,
	return an array pof length <code>0</code>. */
    public HMethod[] calls(final HMethod hm, final CALL cs);

    /** Returns a list of all the <code>CALL</code>s quads in the code 
	of <code>hm</code>. */
    public CALL[] getCallSites(final HMethod hm);

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public Set callableMethods();
}
