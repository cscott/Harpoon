// LowQuadSSI.java, created Wed Feb  3 16:19:45 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.ToNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>LowQuadSSI</code> codeview exposes a lowquad based 
 * representation in SSI form. 

 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadSSI.java,v 1.1.2.2 2000-02-25 17:32:02 cananian Exp $
 */
public class LowQuadSSI extends Code { /*which extends harpoon.IR.Quads.Code*/
    private Derivation  m_derivation;

    /** The name of this code view. */
    public static final String codename  = "low-quad-ssa";

    /** Creates a <code>LowQuadSSI</code> object from a <code>QuadSSI</code>
     *  object. */
    LowQuadSSI(final QuadSSI code) {
	super(code.getMethod(), null);
	final Map dT = new HashMap();
	final Map tT = new HashMap();
	final TypeMap tym = new harpoon.Analysis.Quads.TypeInfo(code);
	FinalMap fm = new harpoon.Backend.Maps.DefaultFinalMap();
	quads = Translate.translate((LowQuadFactory)qf, code, tym, fm, dT, tT);
      
	final LowQuadFactory lqf =  // javac bug workaround to let qf be
	    (LowQuadFactory) qf;    // visible in anonymous Derivation below.
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		Util.assert(hce!=null && t!=null);
		if (dT.get(t)==null && tT.get(t)==null)
		    throw new TypeNotKnownException(hce, t);
		return (DList)dT.get(t);
	    }
	    public HClass typeMap(HCodeElement hce, Temp t) { 
		Util.assert(lqf.tempFactory()==t.tempFactory());
		if (dT.get(t)==null && tT.get(t)==null)
		    throw new TypeNotKnownException(hce, t);
		return (HClass)tT.get(t);
	    }
	};
    }

    /**
     * Create a new code object given a quadruple representation of the
     * method instructions.
     */
    protected LowQuadSSI(HMethod method, Quad quads) {
	super(method, quads);
    }

    /**
     * Clone this code representation.  The clone has its own copy of the
     * quad graph.
     */
    public HCode clone(HMethod newMethod) {
	LowQuadSSI lqs = new LowQuadSSI(newMethod, null);
	lqs.quads      = Quad.clone(lqs.qf, quads);
	return lqs;
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"low-quad-ssa"</code>
     */
    public String getName() { return codename; }

    /**
     * Return a code factory for <code>LowQuadSSI</code>, given a 
     * code factory for <code>QuadSSI</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>QuadSSI</code>, then creates and returns a code
     *      factory for <code>LowQuadSSI</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>QuadSSI.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>LowQuadSSI</code> from the
     *      code factory returned by <code>QuadSSI</code>.
     * @see QuadSSI#codeFactory(HCodeFactory)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null : new LowQuadSSI((QuadSSI)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	}
	else {
	    //throw new Error("don't know how to make " + codename +
	    //	" from " + hcf.getCodeName());
	    return codeFactory(QuadSSI.codeFactory(hcf));
	}
    }
  
    /**
     * Return a code factory for <code>LowQuadNoSSA</code>, using the default
     * code factory for <code>harpoon.IR.LowQuad.Code</code>
     */
    public static HCodeFactory codeFactory() {  
	return codeFactory(QuadSSI.codeFactory());
    }

    // implement derivation.
    public DList derivation(HCodeElement hce, Temp t) {
	return m_derivation.derivation(hce, t);
    }

    public HClass typeMap(HCodeElement hce, Temp t) {
	return m_derivation.typeMap(hce, t);
    }
}
