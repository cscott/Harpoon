// ContBuilder.java, created Wed Nov  3 20:06:51 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HClass;
//import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
//import harpoon.ClassFile.HConstructorSyn;
import harpoon.ClassFile.HMethod;
//import harpoon.ClassFile.HMethodSyn;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.CALL;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>ContBuilder</code> builds continuations for a given <code>CALL</code>
 * using the <code>CALL</code>'s <code>HCode</code>, storing the
 * <code>Code</code> for the continuation in the 
 * <code>UpdateCodeFactory</code>. The generated <code>Code</code> is in
 * <code>quad-no-ssa</code> form.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ContBuilder.java,v 1.1.2.6 2000-01-13 23:52:27 bdemsky Exp $
 */
public class ContBuilder {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final CALL c;
    protected final HClass env;
    protected final Temp[] live;
   
    /** Creates a <code>ContBuilder</code> for the designated 
     *  <code>CALL</code>, given the <code>HCode</code> from which it came.
     *  Requires that the <code>HCode</code> be <code>quad-no-ssa</code>.
     */
    public ContBuilder(UpdateCodeFactory ucf, HCode hc, CALL c, HClass env,
		       Temp[] live) {
        this.ucf = ucf;
	this.hc = hc;
	this.c = c;
	this.env = env;
	this.live = live;
    }
    
    /** Builds the continuation. Returns the <code>HClass</code>.
     *  @return the continuation <code>HClass</code>
     */
//      public HClass makeCont() {
//  	final String methodname = "makeCont()";
//  	HClass template = null;
//  	try {
//  	    template = 
//  		HClass.forName("harpoon.Analysis.ContBuilder.ContTemplate");
//  	} catch (NoClassDefFoundError e) {
//  	    reportException(e, methodname, "Cannot find " + 
//  			    "harpoon.Analysis.ContBuilder.ContTemplate");
//  	}

//  	// create environment from template
//  	HClassSyn cont = new HClassSyn(template);
//  	Util.assert(cont.getConstructors().length == 1, 
//  		    "There should be exactly one constructor in " +
//  		    "synthesized environment class. Found " + 
//  		    cont.getConstructors().length);

//  	HClass hcrettype = this.hc.getMethod().getReturnType();

//  	// if the return type of the HCode we're transforming
//  	// is of type <type>, then we want our continuation to
//  	// extend <type>Continuation.
//  	String s = "harpoon.Analysis.ContBuilder." + 
//  	    getPrefix(hcrettype) + "Continuation";
//  	//BCD start
//  	System.out.println("Setting superclass "+s);
//  	//BCD stop

//  	try {
//  	    cont.setSuperclass(HClass.forName(s));
//  	} catch (NoClassDefFoundError e) {
//  	    reportException(e, methodname, "Cannot find " + s);
//  	}

//  	Temp retval = this.c.retval();
//  	HClass rettype = this.c.method().getReturnType();

//  	// if the return type of the CALL we're transforming
//  	// is of type <type>, then we want our continuation to
//  	// implement <type>ResultContinuation.
//  	String r = "harpoon.Analysis.ContBuilder." + 
//  	    getPrefix(rettype) + "ResultContinuation";
//  	HClass cons_arg2 = null;
//  	try {
//  	    cons_arg2 = HClass.forName(r);
//  	    cont.addInterface(cons_arg2);
//  	} catch (NoClassDefFoundError e) {
//  	    reportException(e, methodname, "Cannot find " + r);
//  	}

//  	HClass environment = null;
//  	try {
//  	    environment = 
//  		HClass.forName("harpoon.Analysis.EnvBuilder.Environment");
//  	} catch (NoClassDefFoundError e) {
//  	    reportException(e, methodname, "Cannot find harpoon.Analysis." +
//  			    "EnvBuilder.Environment");
//  	}

//  	try {
//  	    HConstructor hc = 
//  		template.getConstructor(new HClass[] {environment});
//  	    HConstructor nhc =
//  		cont.getConstructor(new HClass[] {environment});
//  	    HCode hchc = ((Code)this.ucf.convert(hc)).clone(nhc);
//  	    this.ucf.update(nhc, hchc);
//  	    this.ucf.convert(nhc).print
//  		(new java.io.PrintWriter(System.out, true));
//  	} catch (NoSuchMethodError e) {
//  	    reportException(e, methodname, "Cannot find constructor for " +
//  			    "harpoon.Analysis.ContBuilder.ContTemplate");
//  	}

//  	HMethod hm = null;
//  	HMethodSyn nhm = null;
//  	try {
//  	    hm = cont.getDeclaredMethod("resume", new HClass[0]);
//  	    nhm = new HMethodSyn(cont, hm, true);
//  	    cont.removeDeclaredMethod(hm);
//  	} catch (NoSuchMethodError e) {
//  	    reportException(e, methodname, "Cannot find harpoon." +
//  			    "Analysis.ContBuilder.ContTemplate.resume()");
//  	}

//  	boolean hasParameter = false;
//  	if (retval != null) {
//  	    hasParameter = true;
//  	    String[] parameterNames = new String[1];
//  	    parameterNames[0] = retval.name();
//  	    HClass[] parameterTypes = new HClass[1];
//  	    if (rettype.isPrimitive())
//  		parameterTypes[0] = rettype;
//  	    else {
//  		try {
//  		    parameterTypes[0] = HClass.forName("java.lang.Object");
//  		} catch (NoClassDefFoundError e) {
//  		    reportException(e, methodname, 
//  				    "Cannot find java.lang.Object");
//  		}
//  	    }
//  	    nhm.setParameterNames(parameterNames);
//  	    nhm.setParameterTypes(parameterTypes);
//  	}

//  	HMethod next_resume;
//  	if (hcrettype != HClass.Void) {
//  	    next_resume = cont.getField("next").getType().getDeclaredMethod
//  		("resume", new HClass[] {hcrettype});
//  	} else {
//  	    next_resume = cont.getField("next").getType().getDeclaredMethod
//  		("resume", new HClass[0]);
//  	}
	    
//  	// create resume() method and register w/ codeFactory
//  	this.ucf.update(nhm, new ContCode(nhm, this.hc, this.c, hasParameter, 
//  					  next_resume, this.live, 
//  					  cont.getField("e"), 
//  					  this.env.getDeclaredFields(), 0));
//  	this.ucf.convert(nhm).print
//  		(new java.io.PrintWriter(System.out, true));
	
//  	HClass t = null;
//  	try {
//  	    t = HClass.forName("java.lang.Throwable");
//  	} catch (NoClassDefFoundError e) {
//  	    reportException(e, methodname, "Cannot find java.lang.Throwable");
//  	}

//  	HMethod exc = null;
//  	HMethod next_exception = null;
//  	try {
//  	    exc = cont.getDeclaredMethod("exception", new HClass[] {t});
//  	} catch (NoSuchMethodError e) {
//  	    reportException(e, methodname, "Cannot find harpoon.Analysis." +
//  			    "ContBuilder.ContTemplate.exception()");
//  	}
//  	try {
//  	    next_exception = cont.getField("next").getType().
//  		getMethod("exception", new HClass[] {t});
//  	} catch (NoSuchMethodError e) {
//  	    reportException(e, methodname, "Cannot find exception() method" +
//  			    " for next Continuation");
//  	}
	
//  	// create exception() method and register w/ codeFactory
//  	this.ucf.update(exc, new ContCode(exc, this.hc, this.c, hasParameter, 
//  					  next_exception, this.live, 
//  					  cont.getField("e"), 
//  					  this.env.getDeclaredFields(), 1));
//  	this.ucf.convert(exc).print
//  	    (new java.io.PrintWriter(System.out, true));
//  	return cont;
//      }

    // Given a type, returns the prefix for
    // a continuation of that type.
    public static String getPrefix(HClass t) {
	if (t == HClass.Boolean) {
	    return "Boolean";
	} else if (t == HClass.Byte) {
	    return "Byte";
	} else if (t == HClass.Char) {
	    return "Char";
	} else if (t == HClass.Double) {
	    return "Double";
	} else if (t == HClass.Float) {
	    return "Float";
	} else if (t == HClass.Int) {
	    return "Int";
	} else if (t == HClass.Long) {
	    return "Long";
	} else if (t == HClass.Short) {
	    return "Short";
	} else if (t == HClass.Void) {
	    return "Void";
	} else {
	    return "Object";
	}
    }

    private void reportException(Throwable t, String methodname, String msg) {
	System.err.println("Caught exception " + t.toString() + 
			   " in harpoon.Analysis.ContBuilder.ContBuilder." +
			   methodname);
	System.err.println(msg);
    }
}
