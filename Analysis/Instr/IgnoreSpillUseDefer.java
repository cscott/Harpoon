// IgnoreSpillUDr.java, created Fri Jun 30 19:14:06 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

/**
 * <code>IgnoreSpillUseDefer</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: IgnoreSpillUseDefer.java,v 1.1.2.1 2000-06-30 23:19:54 pnkfelix Exp $
 */
public class IgnoreSpillUseDefer extends UseDefer {
    
    /** Creates a <code>IgnoreSpillUDr</code>. */
    public IgnoreSpillUseDefer() {
        
    }
    
    public Collection useC(HCodeElement hce) {
	if (hce instanceof RegAlloc.SpillStore) 
	    return defC(hce);
	else 
	    return super.useC(hce);
    }

    public Collection defC(HCodeElement hce) {
	if (hce instanceof RegAlloc.SpillLoad) 
	    return useC(hce);
	else 
	    return super.defC(hce);
    }
}
