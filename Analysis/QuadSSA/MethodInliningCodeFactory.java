// MethodInliningCodeFactory.java, created Mon Feb  1 09:22:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Util.Util;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Analysis.UseMap;


import java.util.HashSet;
import java.util.Iterator;

import java.io.PrintWriter;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
/**
 * <code>MethodInliningCodeFactory</code> makes an <code>HCode</code>
 * from an <code>HMethod</code>, and inlines methods at call sites
 * registered with the <code>inline(CALL)</code> function.  
 * <P>
 * Recursive functions are legal, but this factory does not provide
 * facilities for specifying number of recursive inlinings.
 *
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: MethodInliningCodeFactory.java,v 1.1.2.2 1999-02-08 10:04:31 pnkfelix Exp $ */
public class MethodInliningCodeFactory implements HCodeFactory {
    
    HCodeFactory parent;

    // sites to be inlined
    HashSet  inlinedSites;

    // sites that have already been inlined (used to detect recursive
    // inlining)  
    HashSet currentlyInlining;

    /** Creates a <code>MethodInliningCodeFactory</code>, using the
	default code factory for QuadSSA. 
    */ 
    public MethodInliningCodeFactory() {
        parent = QuadSSA.codeFactory();
	inlinedSites = new HashSet();
    }

    /** Creates a <code>MethodInliningCodeFactory</code>, using
	<code>parentFactory</code>. 
        <BR> <B>requires:</B> <code>parentFactory</code> produces
	                      "quad-ssa" code. 
    */
    public MethodInliningCodeFactory(HCodeFactory parentFactory) {
        Util.assert(parentFactory.getCodeName().equals(QuadSSA.codename));
	parent = parentFactory;
    }
    
    /** Returns a string naming the type of the <code>HCode</code>
	that this factory produces.  <p>
	<code>this.getCodeName()</code> should equal
	<code>this.convert(m).getName()</code> for every 
	<code>HMethod m</code>. 
     */
    public String getCodeName() { return QuadSSA.codename; }

    /** Removes representation of method <code>m</code> from all caches
	in this factory and its parents.
     */
    public void clear(HMethod m) { parent.clear(m); }

    /** Make an <code>HCode</code> from an <code>HMethod</code>.
       <p>
       <code>convert</code> is allowed to return null if the requested
       conversion is impossible; typically this is because it's attempt
       to convert a source representation failed -- for
       example, because <code>m</code> is a native method.
     */
    public HCode convert(HMethod m) {  
	QuadSSA newCode = (QuadSSA) parent.convert(m);
	if (newCode == null) return null;
	
	Quad[] ql = (Quad[]) newCode.getElements();
	QuadVisitor qv;

	System.out.println( "Sites to inline are " +  inlinedSites );

	for (int i=0; i<ql.length; i++) {
	    
	    boolean inline = inlinedSites.contains( ql[i] );
	    // && !currentlyInlining.contains( ql[i] );
	    boolean found = false; Object o = null;
	    Iterator iter = inlinedSites.iterator();
	    while(iter.hasNext()) {
		o = iter.next();
		System.out.println("Checking " + ql[i] + 
				   " against " + o + 
				   ((o!=null)?""+o.equals(ql[i]):""));
		if (o != null && o.equals( ql[i] )) {
		    found = true;
		    break;
		}
	    }
	    
	    System.out.println(ql[i] + " yields " + found);
	    // inlinedSites.contains( ql[i] ));
	    
	    if (inline) {

		System.out.println("Hey, I'm supposed to inline " + ql[i]);

		// ql[i] is a CALL site that has been marked to be
		// inlined and isn't being inlined currently.  Inline it.
		CALL call = (CALL) ql[i];
		currentlyInlining.add( call ); // prevent recursive inlining.
		HCode calledCode = this.convert( call.method() );
		qv = new InliningVisitor( new UseMap( calledCode ),
					 call );
		
		Quad[] methodQuads = 
		    (Quad[]) calledCode.getElements();
		for (int j=0; j<methodQuads.length; j++) {
		    methodQuads[j].visit(qv);
		}
		
		PrintWriter pw = new PrintWriter(System.out);
		pw.println("Inlined Called Code START");
		calledCode.print(pw);
		pw.println("Inlined Called Code END");
		pw.println();
		pw.flush();
	    }
	}
	
	return newCode;
    }

    /** Marks a call site to be inlined when code is generated.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Marks <code>site</code> as a location to
	                     inline if <code>this</code> is ever asked
			     to generate code for the
			     <code>HMethod</code> containing
			     <code>site</code>.
     */
    public void inline(CALL site) {
	inlinedSites.add(site);
    }

    /** Marks a call site to be inlined when code is generated.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>site</code> is in the set of
	                     call sites to be inlined, takes
			     <code>site</code> out of the set.  Else
			     does nothing.
    */
    public void unline(CALL site) {
	inlinedSites.remove(site);
    }

    static class InliningVisitor extends QuadVisitor {
	
	UseMap uses;
	Temp[] passedParams;

	InliningVisitor( UseMap uses, CALL site ) {
	    this.uses = uses;
	    passedParams = site.params();
	}

	public void visit(Quad q) {
	    // not sure what to do in the general case...
	}
	
	static class IdentityTempMap implements TempMap {
	    public IdentityTempMap() {
	    }
	    
	    public Temp tempMap(Temp t) {
		return t;
	    }
	}
	
	static class SingleTempMap implements TempMap {
	    Temp key, value;
	    
	    public SingleTempMap(Temp key, Temp value) {
		this.key = key;
		this.value = value;
	    }

	    public Temp tempMap(Temp t) {
		if (t.equals(key)) 
		    return value;
		else 
		    return t;
	    }
	}

	public void visit(METHOD q) {
	    // METHOD has temp var parameters for this sequence of
	    // quads... 
	    // now use the UseMap to find their corresponding uses,
	    // and then substitute in the passed arguments.
	    Temp[] params = q.params();
	    for (int i=0; i<params.length; i++) {
		Quad[] useArray = (Quad[]) uses.useMap( params[i] );

		TempMap defMap = new IdentityTempMap();

		TempMap useMap = new SingleTempMap(params[i], passedParams[i]);

		for (int j=0; j<useArray.length; j++) {
		    Quad.replace( useArray[j],
				  useArray[j].rename( defMap, useMap ));
		}
	    }
	}
	
	public void visit(RETURN q) {
	    // replace with a SET to the target value, and a branch to
	    // the footer.
	}
	

    }




    // temporary main method to try some ideas out on...is supposed to
    // be removed before I commit to repository
    public static void main(String[] args) {
	HClass hc = HClass.forName(args[0]);
	HMethod[] methods = hc.getMethods();
	for (int i=0; i<methods.length; i++) {
	    HMethod method = methods[i];
	    // System.out.println(method.getName());
	    if (method.getName().equals("main")) {
		performTest(method);
	    }
	}
    }

    // scans method for all CALL sites, and inlines each one,
    // nonrecursively (thus a breadth-first traversal of the call
    // graph)
    public static void performTest(HMethod method) {
	MethodInliningCodeFactory factory = 
	    new MethodInliningCodeFactory();
	PrintWriter pw = new PrintWriter(System.out);
	HCode code;
	code = factory.convert(method);

	if (code == null) return;

	pw.println("Code prior to inlining");	
	code.print(pw);
	pw.println();

	
	HCodeElement[] hces = code.getElements();

	for (int i=0; i<hces.length; i++) {
	    HCodeElement q = hces[i];
	    if (q instanceof CALL) {
		pw.println("INLINING " + q);
	        factory.inline( (CALL) q);
	    }
	}
	
	
	code = factory.convert(method);

	pw.println("---------------------------");
	pw.println("Code after inlining");
	code.print(pw);
	pw.println();
	
	pw.flush();
	
    }


}
