// MethodInlining.java, created Mon Jan 25 16:14:12 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.METHOD;
import harpoon.Analysis.UseMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

// This should be replaced by a MethodInliningCodeFactory (or
// SOMETHING) that fits in the new framework of code generation for
// methods.

/**
 * <code>MethodInlining</code> inlines method bodys at particular call sites.
 * 
 * @deprecated This will be replaced by MethodInliningCodeFactory.  Do not use.
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: MethodInlining.java,v 1.1.2.6 1999-09-08 16:35:19 cananian Exp $
 */
public abstract class MethodInlining {

    public static HCodeFactory codeFactory() {
	return new MethodInliningCodeFactory();
    }
    
    public static HCodeFactory codeFactory(HCodeFactory fact) {
	return new MethodInliningCodeFactory(fact);
    }

    

    /** Retrieves the code for method called at <code>site</code> and 
	returns an <code>HCode</code> represented the code to be
	inserted in place of the call.
	<BR> requires: code called by <code>site</code> is in quad-ssi
	               form. 
    */
    public static QuadSSI getInlinedCode(CALL site) { 
	return getInlinedCode(site, false);
    }

    /** Retrieves the code for method called at <code>site</code> and 
	returns an <code>HCode</code> represented the code to be
	inserted in place of the call.
	<BR> requires: code called by <code>site</code> is in quad-ssi
	               form. 
	<BR> modifies: code contained by <code>HCode</code> called by
	               <code>site</code> 
	<BR> effects: if <code>modifyCode</code> is false, retrieves
	              the code for <code>HMethod</code> called at
		      <code>site</code> and iterates over its
		      elements, copying them into a new <code>QuadSSI</code> and
		      replacing uses of the <code
    */    
    private static QuadSSI getInlinedCode(CALL site, boolean modifyCode) {

	// replace below getCode() call with HCodeFactory.convert() call
	QuadSSI code = (QuadSSI) site.method().getCode("quad-ssi");
	
	Util.assert( code != null, 
		     "Code body for " + site.method() +
		     " was not produced when requested in QuadSSI form");

	HCodeElement[] codeElems = code.getElements();

	Temp[] bodyParams = null;
	
	// get Method Header for this site.
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
	// them with the passed parameters.
	for (int i=0; i<callParams.length; i++) {
	    System.out.println(" Call Param: " + callParams[i] +
			       " Body Param: " + bodyParams[i] );
	    
	    HCodeElement[] tmpuses = 
		(new UseMap(code)).useMap(bodyParams[i]);
	    
		
	    Util.assert(tmpuses instanceof Quad[],
			"Uses array needs to be a Quad[] " + 
			"for MethodInlining to work"); 

	    Quad[] uses = (Quad[]) tmpuses;

	    for (int j=0; j<uses.length; j++) {
		System.out.println(uses[j]);
	    }
	}	
	
	// find all return statements, replace with an assignment to
	// retval for 'site' and a jump to the end-label

	// find all throw statements, replace with an assignment to
	// expval for 'site' and a jump to the end-label
	return code;
    }
}
