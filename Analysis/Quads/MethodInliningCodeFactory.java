// MethodInliningCodeFactory.java, created Mon Feb  1 09:22:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Util.Util;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.CloningTempMap;


import java.util.Set;
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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: MethodInliningCodeFactory.java,v 1.1.2.6 2001-06-17 22:31:27 cananian Exp $ */
public class MethodInliningCodeFactory implements HCodeFactory {

    static PrintWriter pw = new PrintWriter(System.out);
    
    HCodeFactory parent;

    // sites to be inlined
    HashSet  inlinedSites;

    // sites that have already been inlined (used to detect recursive
    // inlining)  
    private HashSet currentlyInlining;

    /** Creates a <code>MethodInliningCodeFactory</code>, using the
	default code factory for QuadSSI. 
    */ 
    public MethodInliningCodeFactory() {
        parent = new CachingCodeFactory(QuadSSI.codeFactory());
	inlinedSites = new HashSet();
    }

    /** Creates a <code>MethodInliningCodeFactory</code>, using
	<code>parentFactory</code>. 
        <BR> <B>requires:</B> <code>parentFactory</code> produces
	                      "quad-ssi" code. 
    */
    public MethodInliningCodeFactory(HCodeFactory parentFactory) {
        Util.assert(parentFactory.getCodeName().equals(QuadSSI.codename));
	parent = parentFactory;
	inlinedSites = new HashSet();
    }
    
    /** Returns a string naming the type of the <code>HCode</code>
	that this factory produces.  <p>
	<code>this.getCodeName()</code> should equal
	<code>this.convert(m).getName()</code> for every 
	<code>HMethod m</code>. 
     */
    public String getCodeName() { return QuadSSI.codename; }

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
       <p>
       MethodInliningCodeFactory also inlines any call sites within
       <code>m</code> marked by the <code>this.inline(CALL)</code>
       method. 
     */
    public HCode convert(HMethod m) {  
	QuadSSI newCode = (QuadSSI) parent.convert(m);
	if (newCode == null) return null;
	
	// The below was added in response to Scott's request that the
	// HCode be cloned prior to being fuddled with by the Inliner
	newCode = (QuadSSI) newCode.clone( m ).hcode();

	Quad[] ql = (Quad[]) newCode.getElements();
	QuadVisitor qv;
	currentlyInlining = new HashSet();

	// pw.println( "Sites to inline are " +  inlinedSites );

	for (int i=0; i<ql.length; i++) {
	    
	    boolean inline = inlinedSites.contains( ql[i] ) && 
		! currentlyInlining.contains( ql[i] );
	    if (inline) {
		
		// pw.println("Hey, I'm supposed to inline " + ql[i]);
		
		// ql[i] is a CALL site that has been marked to be
		// inlined and isn't being inlined currently.  Inline it.
		CALL call = (CALL) ql[i];
		currentlyInlining.add( call ); // prevent recursive inlining.
		HCode calledCode = this.convert( call.method() );
		
		
		Quad[] methodQuads = 
		    (Quad[]) calledCode.getElements();
		Quad header = methodQuads[0];

		Quad newQ = Quad.clone(call.getFactory(), header);
		

		class QuadArrayBuilder {
		    Set s;
		    QuadArrayBuilder(HEADER q) {
			s = new HashSet();
			recurse(q);
		    }
		    
		    private void recurse(Quad q) {
			s.add(q);
			Quad[] next = q.next();
			for (int j=0; j<next.length; j++) {
			    recurse(next[j]);
			}
		    }

		    Quad[] getElements() {
			return (Quad[]) s.toArray( new Quad[0] );
		    }
		}
		

		Quad[] newElems;
		QuadArrayBuilder build = new QuadArrayBuilder((HEADER) newQ);
		newElems = build.getElements();
		UseDefMap map = new UseDef( /*Quad.arrayFactory, newElems*/ );

		qv = new InliningVisitor( map, call );
		for(int j=0; j<newElems.length; j++) {
		    newElems[j].accept(qv);
		}
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

    /** Marks a call site to not be inlined when code is generated.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>site</code> is in the set of
	                     call sites to be inlined, takes
			     <code>site</code> out of the set.  Else
			     does nothing.
    */
    public void uninline(CALL site) {
	inlinedSites.remove(site);
    }

    static class InliningVisitor extends QuadVisitor {
	
	UseDefMap uses;
	Temp[] passedParams;
	CALL site;
	
	
	InliningVisitor( UseDefMap uses, CALL site ) {
	    this.uses = uses;
	    this.site = site;
	    passedParams = site.params();
	}

	public void visit(Quad q) {
	    // not sure what to do in the general case...
	    
	}
	
	// A single <key, value> pairing map
	static class SingleTempMap implements TempMap {
	    Temp key, value;
	    public SingleTempMap(Temp key, Temp value) { 
		this.key = key; this.value = value; 
	    }
	    public Temp tempMap(Temp t) { return (t.equals(key))?value:t; }
	}

	public void visit(METHOD q) {
	    
	    // METHOD succ[0] is the first bit of executable bytecode;
	    // make the code prior to the CALL point to it.
	    Quad[] qArray = { site.prev()[0], q.next()[0] };
	    Quad.addEdges( qArray );
	    
	    // METHOD has temp var parameters for this sequence of
	    // quads...use the UseDefMap to find their corresponding
	    // uses, and then substitute in the passed arguments.
	    Temp[] params = q.params();
	    for (int i=0; i<params.length; i++) {
		HCode hc = q.getFactory().getParent();
		Quad[] useArray = (Quad[]) uses.useMap(hc, params[i] );
		TempMap useMap = new SingleTempMap(params[i], passedParams[i]);
		
		for (int j=0; j<useArray.length; j++) {
		    Quad useQ = useArray[j];
		    Quad.replace(useQ, useQ.rename(useMap, useMap));
		}
	    }
	}
	
	public void visit(THROW q) { 
	  // do something similar to RETURN here 
	  Temp retEx = site.retex();
	  Quad replace;
	  if (retEx != null) {
	    replace = new MOVE(site.getFactory(), null,
			       retEx, q.throwable());
	  } else {
	    // no place to put the exception...don't know under what
	    // situation this would occur...perhaps put in an
	    // assertion to ensure that it doesn't?
	    replace = new NOP(site.getFactory(), null);
	  }
	  // make the successor(replace) be the 1-successor of the
	  // calling site (the succesor for the "exception thrown" case).
	  Quad[] qArray = { q.prev()[0], replace, site.next()[1] };
	  Quad.addEdges( qArray );
	}

	public void visit(RETURN q) {
	    // replace with a MOVE to the target value, and a branch to
	    // the footer.
	    Temp retVal = site.retval(); 
	    Quad replace; 
	    if (retVal != null) {
		// put in the MOVE to target val and replace := MOVE
		replace = new MOVE(site.getFactory(), null,
				   retVal, q.retval());
	    } else {
		// there is no value to return...ummm...for now put in
		// a NOP and replace := NOP
		replace = new NOP(site.getFactory(), null);
	    }
	    // make the successor(replace) be the 0-successor of the
	    // calling site (the succesor for the normal case).
	    Quad[] qArray = { q.prev()[0], replace, site.next()[0] };
	    Quad.addEdges( qArray );
	}
    }

}
