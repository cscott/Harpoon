// CallGraph.java, created Thu Aug 24 17:06:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Set;
import harpoon.ClassFile.HMethod;

/**
 * <code>CallGraph</code> is a general IR-independant interface that
 * for a call graph.  IR-specific subclasses (see
 * <code>harpoon.Analysis.Quads.CallGraph</code>) can provide
 * call-site information.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.2 2002-02-25 20:56:09 cananian Exp $
 */
public interface CallGraph {
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public HMethod[] calls(final HMethod hm);
}
