// MethodInliningCodeFactory.java, created Mon Feb  1 09:22:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.METHOD;
import harpoon.Util.Util;

import java.util.Hashtable;
/**
 * <code>MethodInliningCodeFactory</code> makes an <code>HCode</code>
 * from an <code>HMethod</code>, and inlines methods at call sites
 * registered with the <code>inline(CALL)</code> function.  
 * <P>
 * Recursive functions are legal, but this factory does not provide
 * facilities for specifying number of recursive inlinings.
 *
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: MethodInliningCodeFactory.java,v 1.1.2.1 1999-02-04 19:55:40 pnkfelix Exp $ */
public class MethodInliningCodeFactory implements HCodeFactory {
    
    HCodeFactory parent;

    // sites to be inlined
    Hashtable inlinedSites;

    // sites that have already been inlined (used to detect recursive
    // inlining)  
    Hashtable currentlyInlining;

    /** Creates a <code>MethodInliningCodeFactory</code>, using the
	default code factory for QuadSSA. 
    */ 
    public MethodInliningCodeFactory() {
        parent = QuadSSA.codeFactory();
	inlinedSites = new Hashtable();
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
	//QuadSSA newCode = (QuadSSA) parent.convert(m).clone(); 
	QuadSSA newCode = null; 
	
	return newCode;
    }

    /** Marks a call site to be inlined when code is generated.
     */
    public void inline(CALL site) {
	
    }

    /** Marks a call site to be inlined when code is generated.
     */
    public void unline(CALL site) {
	
    }


}
