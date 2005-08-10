// Debug.java, created Wed Aug  3 07:15:01 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import harpoon.ClassFile.HCode;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>Debug</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Debug.java,v 1.1 2005-08-10 03:03:16 salcianu Exp $
 */
public abstract class Debug {

    public static boolean ENABLED = false;

    public static void check(HCode hc1, HCode hc2) {
	check((Code) hc1, (Code) hc2);
    }

    public static void check(Code hc1, Code hc2) {
	if(!ENABLED) return;

	int k1 = countSA(hc1);
	int k2 = countSA(hc2);
	assert k1 == k2 : "# of stack allocatable sites varies from " + k1 + " to " + k2;
	if(k1 != 0)
	    System.out.println("PRESERVED #sa " + k1);
    }
    
    public static int countSA(Code hc1) {
	AllocationInformation<Quad> ai = hc1.getAllocationInformation();
	if(ai == null) return 0;

	int count = 0;
	for(Quad q : hc1.getElements()) {
	    if(q instanceof NEW || q instanceof ANEW) {
		AllocationProperties ap = ai.query(q);
		if(ap.canBeStackAllocated()) 
		    count++;		
	    }
	}

	return count;	
    }

}
