// UnknownAllocationSiteError.java, created Tue Feb 11 11:30:08 2003 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

/** Error thrown if one tries to obtain the unique allocID for
    a site that <code>AllocationNUmbering</code> doesn't know
    about. 
    
    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: UnknownAllocationSiteError.java,v 1.1 2003-02-11 20:16:01 salcianu Exp $ */
public class UnknownAllocationSiteError extends Error {
    /** Creates an <code>UnknownAllocationSiteError</code>. */
    public UnknownAllocationSiteError(String message) {
	super(message);
    }
}


