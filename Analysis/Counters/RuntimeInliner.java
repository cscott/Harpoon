// RuntimeInliner.java, created Thu Nov  1 16:24:46 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Counters;

import harpoon.Analysis.Quads.MethodInliningCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The <code>RuntimeInliner</code> code factory inlines all the methods called
 * in methods of the <code>harpoon.Runtime.Counters</code> class.  This
 * way we can simply refuse to put counters in 
 * <code>harpoon.Runtime.Counters</code> and we don't have to worry about
 * our counter code counting itself (which is quite unpleasant).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RuntimeInliner.java,v 1.1.2.1 2001-11-01 22:46:40 cananian Exp $
 */
class RuntimeInliner {
    private CachingCodeFactory result;
    public RuntimeInliner(HCodeFactory hcf, Linker linker) {
	// this is the class whose methods we will remove all calls from.
	HClass hc = linker.forName("harpoon.Runtime.Counters");
	// this is a set of 'bad' (virtual) methods.
	Set badboys = new HashSet();
	// while there are static call sites in methods of our class,
	// inline them.
	CachingCodeFactory ccf = // note that we force ssa form.
	    new CachingCodeFactory(QuadSSA.codeFactory(hcf));
	MethodInliningCodeFactory micf = null; // created when needed
	int n=0; // keeps track of depth we have to go.
	while (true) {
	    int s=0;
	    // collect call sites!
	    // for each method of hc.
	    for (Iterator it=Arrays.asList(hc.getDeclaredMethods()).iterator();
		 it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		if (!hm.getName().equals("report")) continue;
		HCode c = ccf.convert(hm);
		// for all the quads in the code.
		for (Iterator it2 = c.getElementsI(); it2.hasNext(); ) {
		    Quad q = (Quad) it2.next();
		    // find CALLs.
		    if (!(q instanceof CALL)) continue;
		    CALL call = (CALL) q;
		    // we don't like virtual calls.
		    if (call.isVirtual()) {
			badboys.add(call.method());
			continue;
		    }
		    // and native methods are safe to skip too.
		    if (Modifier.isNative(call.method().getModifiers()))
			continue;
		    // let's assume exceptions never occur.
		    if (call.method().getDeclaringClass().isInstanceOf
			(linker.forName("java.lang.Throwable")))
			continue;
		    // all non-virtual stuff gets added to the inliner.
		    if (micf==null) // ooh, this is first needed!
			micf = new MethodInliningCodeFactory(ccf);
		    micf.inline(call);
		    s++;
		    /*
		    System.err.println("  inlining call to "+call.method()+
				       " in "+hm+" ("+call.getSourceFile()+
				       ":"+call.getLineNumber()+")");
		    */
		}
	    }
	    // bail if we didn't find any call sites (this means we're done)
	    if (micf==null) break;
	    // otherwise, inline these and try again.
	    ccf = new CachingCodeFactory(QuadSSA.codeFactory(micf));
	    micf = null;
	    n++;
	    System.err.println("PASS "+n+": "+s+" sites inlined.");
	}
	// yea, done.  the last caching code factory is sufficient.
	this.result = ccf;
	// print some messages.
	if (badboys.size()>0)
	    System.err.println("WARNING: could not inline calls in statistics"
			       +" code to "+badboys);
	System.err.println("REQUIRED "+n+" stages of inlining.");
    }
    public HCodeFactory codeFactory() { return result; }
}
