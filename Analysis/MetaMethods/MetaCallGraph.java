// MetaCallGraph.java, created Mon Mar 13 15:53:31 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Set;

import java.io.PrintWriter;

import harpoon.IR.Quads.CALL;
import harpoon.Analysis.PointerAnalysis.Relation;

/**
 * <code>MetaCallGraph</code> is for meta methods what <code>callGraph</code>
 is for &quot;normal&quot; methods. It provides information on what meta
 methods are called by a given meta method [at a specific call site].
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: MetaCallGraph.java,v 1.1.2.3 2000-03-28 23:51:33 salcianu Exp $
 */

public interface MetaCallGraph {
    
    /** Returns the meta methods that can be called by <code>mm</code>. */
    public MetaMethod[] getCallees(MetaMethod mm);
    
    /** Returns the meta methods that can be called by <code>mm</code>
	at the call site <code>q</code>. */
    public MetaMethod[] getCallees(MetaMethod mm, CALL cs);

    /** Returns the set of all the call sites in the code of the meta-method
	<code>mm</code>. */
    public Set getCallSites(MetaMethod mm);
    
    /** Returns the set of all the meta methods that might be called during the
	execution of the program. */
    public Set getAllMetaMethods();

    /** Returns the set of all the meta methods that might be called, directly
	or indirectly, by the meta method <code>mm</code>. It's just the
	transitive closure of the <code>getCallees</code> method. */
    public Set getTransCallees(MetaMethod mm);

    /** Computes the <i>split</i> relation. This is a <code>Relation</code>
	that associates to each <code>HMethod</code> the set of
	<code>MetaMethod</code>s specialized from it. */
    public Relation getSplitRelation();

    /** Nice pretty-printer for debug purposes. */
    public void print(PrintWriter pw, boolean detailed_view);
}
