// MethodInliningCodeFactory.java, created Mon Feb  1 09:22:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.Util.Util;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.CloningTempMap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @version $Id: MethodInliningCodeFactory.java,v 1.2 2002-02-25 20:59:23 cananian Exp $ */
public class MethodInliningCodeFactory implements HCodeFactory {

    HCodeFactory parent;
    private final Map h = new HashMap(); // code cache.

    // sites to be inlined
    HashSet  inlinedSites;

    // sites that have already been inlined (used to detect recursive
    // inlining)  
    private HashSet currentlyInlining = new HashSet();

    /** Creates a <code>MethodInliningCodeFactory</code>, using
	<code>parentFactory</code>. 
        <BR> <B>requires:</B> <code>parentFactory</code> produces
	                      "quad-ssi" code. 
    */
    public MethodInliningCodeFactory(HCodeFactory parentFactory) {
        Util.assert(parentFactory.getCodeName().equals(QuadSSA.codename));
	parent = parentFactory;
	inlinedSites = new HashSet();
    }
    
    /** Returns a string naming the type of the <code>HCode</code>
	that this factory produces.  <p>
	<code>this.getCodeName()</code> should equal
	<code>this.convert(m).getName()</code> for every 
	<code>HMethod m</code>. 
     */
    public String getCodeName() { return MyRSSx.codename; }

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
	if (h.containsKey(m)) return (HCode) h.get(m); // even if get()==null.
	harpoon.IR.Quads.Code newCode = (QuadSSA) parent.convert(m);
	if (newCode == null) { h.put(m, null); return null; }
	
	// The below was added in response to Scott's request that the
	// HCode be cloned prior to being fuddled with by the Inliner
	HCodeAndMaps hcam = MyRSSx.cloneToRSSx(newCode, m);
	newCode = (MyRSSx) hcam.hcode();
	Map aem = hcam.ancestorElementMap();

	Quad[] ql = (Quad[]) newCode.getElements();

	// pw.println( "Sites to inline are " +  inlinedSites );

	for (int i=0; i<ql.length; i++) {
	    
	    boolean inline = inlinedSites.contains( aem.get(ql[i]) ) && 
		! currentlyInlining.contains( ((CALL)ql[i]).method() );
	    if (inline) {
		
		// pw.out.println("Hey, I'm supposed to inline " + ql[i]);
		
		// ql[i] is a CALL site that has been marked to be
		// inlined and isn't being inlined currently.  Inline it.
		CALL call = (CALL) ql[i];
		currentlyInlining.add( call.method() );// no recursive inlining
		HCode calledCode = this.convert( call.method() );
		currentlyInlining.remove( call.method() );
		
		
		Quad header = (HEADER) calledCode.getRootElement();
		Quad newQ = Quad.clone(call.getFactory(), header);

		class QuadArrayBuilder {
		    Set s;
		    QuadArrayBuilder(HEADER q) {
			s = new HashSet();
			recurse(q);
		    }
		    
		    private void recurse(Quad q) {
			if (s.contains(q)) return;
			s.add(q);
			Quad[] next = q.next();
			for (int j=0; j<next.length; j++) {
			    recurse(next[j]);
			}
		    }

		    Quad[] getElements() {
			return (Quad[]) s.toArray( new Quad[s.size()] );
		    }
		}
		

		QuadArrayBuilder build = new QuadArrayBuilder((HEADER) newQ);
		Quad[] newElems = build.getElements();
		// make method-renaming map.
		TempMap renameMap = makeMap((METHOD)newQ.next(1), call);

		InliningVisitor qv = new InliningVisitor( call );
		for(int j=0; j<newElems.length; j++) {
		    if (newElems[j] instanceof HEADER) continue;
		    Quad nq = newElems[j].rename(renameMap, renameMap);
		    Quad.replace(newElems[j], nq);
		    nq.accept(qv);
		}
		// now make phis for throw/return merge.
		PHI retphi = new PHI(newQ.getFactory(), call,
				     new Temp[0], qv.return_sites.size());
		PHI thrphi = new PHI(newQ.getFactory(), call,
				     new Temp[0], qv.throw_sites.size());
		Edge ret = call.nextEdge(0), thr = call.nextEdge(1);
		Quad.addEdge(retphi, 0, (Quad)ret.to(), ret.which_pred());
		Quad.addEdge(thrphi, 0, (Quad)thr.to(), thr.which_pred());
		int j=0;
		for (Iterator it=qv.return_sites.iterator(); it.hasNext(); )
		    Quad.addEdge((Quad)it.next(), 0, retphi, j++);
		j=0;
		for (Iterator it=qv.throw_sites.iterator(); it.hasNext(); )
		    Quad.addEdge((Quad)it.next(), 0, thrphi, j++);
	    }
	}
	// remove useless/unreachable phis
	// (methods which don't return/don't throw exceptions, etc)
	Unreachable.prune(newCode);
	// done!
	h.put(m, newCode); // cache this puppy.
	return newCode;
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
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

    private static class MapTempMap extends HashMap implements TempMap {
	public Temp tempMap(Temp t) {
	    return containsKey(t)?(Temp)get(t):t;
	}
    }
    private TempMap makeMap(METHOD q, CALL site) {
	Temp[] passedParams = site.params();
	MapTempMap mtm = new MapTempMap();
	// METHOD has temp var parameters for this sequence of
	// quads...make a temp map to substitute in the passed
	// arguments.
	Temp[] params = q.params();
	for (int i=0; i<params.length; i++)
	    mtm.put(params[i], passedParams[i]);
	return mtm;
    }

    static class InliningVisitor extends QuadVisitor {
	
	CALL site;
	MapTempMap renameMap = new MapTempMap();
	List return_sites = new ArrayList();
	List throw_sites = new ArrayList();
	
	
	InliningVisitor( CALL site ) {
	    this.site = site;
	}

	public void visit(Quad q) {
	    // not sure what to do in the general case...
	    
	}
	
	public void visit(METHOD q) {
	    // METHOD succ[0] is the first bit of executable bytecode;
	    // make the code prior to the CALL point to it.
	    Edge frm = site.prevEdge(0), to = q.nextEdge(0);
	    Quad.addEdge((Quad) frm.from(), frm.which_succ(),
			 (Quad) to.to(), to.which_pred());
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
	    Util.assert(false);
	  }
	  // make the successor(replace) be the 1-successor of the
	  // calling site (the succesor for the "exception thrown" case).
	  Edge frm = q.prevEdge(0);
	  Quad.addEdge((Quad)frm.from(), frm.which_succ(), replace, 0);
	  throw_sites.add(replace);
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
	    Edge frm = q.prevEdge(0);
	    Quad.addEdge((Quad)frm.from(), frm.which_succ(), replace, 0);
	    return_sites.add(replace);
	}
    }

}
