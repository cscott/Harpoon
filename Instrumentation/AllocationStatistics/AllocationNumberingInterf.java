// AllocationNumberingInterf.java, created Sat Feb  1 19:51:12 2003 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import java.util.Set;
import harpoon.IR.Quads.Quad;

/**
 * Objects that implement <code>AllocationNumberingInterf</code>
 * provide unique integer IDs for all allocation sites from a program.
 * Note: the getID method from the Quad class provides an integer that
 * is only locally unique (in the method of that quad).
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: AllocationNumberingInterf.java,v 1.1 2003-02-03 16:20:31 salcianu Exp $
 */
public interface AllocationNumberingInterf {
    /** Return a unique identifier for the allocation site
	<code>q</code>. */
    int allocID(Quad q);    
}
