// MethodInlining.java, created Mon Jan 25 16:14:12 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.METHOD;
import harpoon.Analysis.UseDef;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>MethodInlining</code> inlines method bodys at particular call sites.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: MethodInlining.java,v 1.1.2.1 1999-01-30 23:30:36 pnkfelix Exp $
 */
public abstract class MethodInlining {

    /** Retrieves the code for method called at <code>site</code> and 
	returns an <code>HCode</code> represented the code to be
	inserted in place of the call.
    */
    public static QuadSSA getInlinedCode(CALL site) { 
	

	QuadSSA code = (QuadSSA) site.method().getCode("quad-ssa");
	
	Util.assert( code != null, 
		     "Code body for " + site.method() +
		     " was not produced when requested in quadSSA form");

	HCodeElement[] codeElems = code.getElements();

	Temp[] bodyParams = null;
	
	for (int i=0; i<codeElems.length; i++) {
	    if (codeElems[i] instanceof METHOD) {
		METHOD method = (METHOD) codeElems[i];
		bodyParams = method.params();
		break;
	    }
	}
	
	Util.assert( bodyParams != null, 
		     "Code body for " + site.method() +
		     " doesn't contain a METHOD quad");
	
    
	Temp[] callParams = site.params();
	
	
	Util.assert(callParams.length == bodyParams.length, 
		    "Caller/Callee parameter mismatch " +
		    callParams.length + "/" + bodyParams.length);
	

	// use UseDef chains to find all uses of parameters, replace
	// them with the passed paramters.
	for (int i=0; i<callParams.length; i++) {
	    System.out.println(" Call Param: " + callParams[i] +
			       " Body Param: " + bodyParams[i] );
	    
	    HCodeElement[] uses = 
		(new UseDef()).useMap(code, bodyParams[i]);
	    
	    for (int j=0; j<uses.length; j++) {
		System.out.println(uses[j]);
		
		Util.assert(uses[j] instanceof Quad,
			    "" + uses[j] + " needs to be a Quad " + 
			    "for MethodInlining to work"); 
			    
	    }
	}	
	
	// find all return statements, replace with an assignment to
	// retval for 'site' and a jump to the end-label

	// find all throw statements, replace with an assignment to
	// expval for 'site' and a jump to the end-label
	return code;
    }
}
