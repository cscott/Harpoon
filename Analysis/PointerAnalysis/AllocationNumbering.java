// AllocationNumbering.java, created Wed Nov  8 19:06:08 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.Code;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * An <code>AllocationNumbering</code> object assigns unique numbers
 * to each allocations site from a program and (possibly) to each call
 * sites.  Later, these numbers can be used in the instrumenting code
 * (e.g. <code>InstrumentAllocs</code>).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationNumbering.java,v 1.8 2002-12-03 04:04:24 salcianu Exp $ */
public class AllocationNumbering implements java.io.Serializable {
    private final CachingCodeFactory ccf;
    public  final Map alloc2int;
    public  final Map call2int;
    
    /** Creates an <code>AllocationNumbering</code> object.

	@param hcf <code>CodeFactory</code> giving the code to instrument
	@param ch  <code>ClassHierarchy</code> for the code from hcf
	@param callSites if true, instrument the call sites too  */
    public AllocationNumbering(HCodeFactory hcf, ClassHierarchy ch,
			       boolean callSites) {
	this.alloc2int = new HashMap();
	this.call2int  = callSites ? new HashMap() : null;
        this.ccf       = new CachingCodeFactory(hcf, true);
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); )
	    number(this.ccf.convert((HMethod) it.next()), callSites);

	System.out.println("alloc_count = " + alloc_count +
			   "\tcall_count = " + call_count);
    }

    /** Return the (caching) code factory this numbering was created on. */
    public HCodeFactory codeFactory() { return ccf; }
    

    /** Return an integer identifying the allocation site <code>q</code>. */
    public int allocID(Quad q) {
	if (!alloc2int.containsKey(q)) throw new Error("Quad unknown: "+q);
	return ((Integer) alloc2int.get(q)).intValue();
    }

    /** Return the set of instrumented allocation sites. */
    public Set getAllocs() {
	return alloc2int.keySet();
    }


    /** Return an integer identifying the CALL quad <code>q</code>. */
    public int callID(Quad q) {
	if (!call2int.containsKey(q)) throw new Error("Quad unknown: "+q);
	return ((Integer) call2int.get(q)).intValue();
    }

    /** Return the set of instrumented CALLs. */
    public Set getCalls() {
	return call2int.keySet();
    }


    /* hard part: the numbering */
    private void number(HCode hc, boolean callSites) {
	if (hc != null)
	    for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if ((q instanceof ANEW) || (q instanceof NEW))
		    alloc2int.put(q, new Integer(alloc_count++));
		else if (callSites && (q instanceof CALL))
		    call2int.put(q, new Integer(call_count++));
	    }
    }

    private int alloc_count = 0;
    private int call_count  = 0;


    ////////////////// MINI SERIALIZATON //////////////////////////////////

    public void writeToFile(String filename) throws IOException {
	PrintWriter pw =
	    new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	Collection methods = get_methods();
	writeInt(pw, methods.size());
	for(Iterator it = methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    writeMethodSignature(pw, hm);
	    writeAllocs(pw, ((Code) ccf.convert(hm)).selectAllocations());
	}
	pw.close();
    }

    // returns a collection of all the methods with instrumented
    // allocations in their code
    private Collection get_methods() {
	Set methods = new HashSet();
	for(Iterator it = getAllocs().iterator(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    methods.add(quad.getFactory().getMethod());
	}
	return methods;
    }

    private void writeMethodSignature(PrintWriter pw, HMethod hm) {
	writeString(pw, hm.getDeclaringClass().getName());
	writeString(pw, hm.getName());
	HClass[] ptypes = hm.getParameterTypes();
	writeInt(pw, ptypes.length);
	for(int i = 0; i < ptypes.length; i++)
	    writeString(pw, ptypes[i].getName());
    }

    private void writeAllocs(PrintWriter pw, Collection allocs) {
	writeInt(pw, allocs.size());
	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    writeInt(pw, quad.getID());
	    writeInt(pw, allocID(quad));
	}
    }

    private static void writeString(PrintWriter pw, String str) {
	try {
	    pw.println("// " + str); // added for comment
	    byte[] bytes = str.getBytes("UTF8");
	    writeInt(pw, bytes.length);
	    for(int i = 0; i < bytes.length; i++)
		writeInt(pw, bytes[i]);
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    private static void writeInt(PrintWriter pw, int i) {
	pw.println(i);
    }
}
