// LowQuadNoSSA.java, created Wed Feb  3  1:02:14 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.ToNoSSA;

/**
 * The <code>LowQuadNoSSA</code> codeview exposes a lowquad-based 
 * representation.  The distinguishing characteristic of this codeview
 * is that is not in SSA form.  What this means is that, although there
 * are still PHI and SIGMA quads in this codeview, they do not actually
 * assign values to temporaries.  In other words, PHI and SIGMA nodes
 * exist solely to indicate flow of control in a program, but they will
 * always be empty.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadNoSSA.java,v 1.3 2003-03-10 22:19:14 cananian Exp $
 */
public class LowQuadNoSSA extends Code {/*which extends harpoon.IR.Quads.Code*/
    /** The name of this code view. */
    public static final String codename  = "low-quad-no-ssa";

    /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
    LowQuadNoSSA(LowQuadSSI code) {
	super(code.getMethod(), null);
      
	ToNoSSA translator;
      
	translator   = new ToNoSSA(qf, code, (Derivation)code.getDerivation());
	quads        = translator.getQuads();
	setDerivation(translator.getDerivation());
	setAllocationInformation(translator.getAllocationInformation());
    }
  
    /**
     * Create a new code object given a quadruple representation of the
     * method instructions.
     */
    protected LowQuadNoSSA(HMethod method, Quad quads) {
	super(method, quads);
    }

    /**
     * Clone this code representation.  The clone has its own copy of the
     * quad graph.
     */
    public HCodeAndMaps<Quad> clone(HMethod newMethod) {
	return cloneHelper(new LowQuadNoSSA(newMethod, null));
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"low-quad-no-ssa"</code>
     */
    public String getName() { return codename; }

    /**
     * Return a code factory for <code>LowQuadNoSSA</code>, given a 
     * code factory for either <code>LowQuadSSI</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>LowQuadSSI</code>, then creates and returns a code
     *      factory for <code>LowQuadNoSSA</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>LowQuadSSI.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>LowQuadNoSSA</code> from the
     *      code factory returned by <code>LowQuadSSI</code>.
     * @see LowQuadSSI#codeFactory(HCodeFactory)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(LowQuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() { 
		public HCode convert(HMethod m) { 
		    HCode c = hcf.convert(m);
		    return (c==null) ? null : new LowQuadNoSSA((LowQuadSSI)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	}
	else {
	    //throw new Error("don't know how to make " + codename +
	    //	" from " + hcf.getCodeName());
	    return codeFactory(LowQuadSSI.codeFactory(hcf));
	}
    }
  
    /**
     * Return a code factory for <code>LowQuadNoSSA</code>, using the default
     * code factory for <code>LowQuadSSI</code>
     */
    public static HCodeFactory codeFactory() {  
	return codeFactory(LowQuadSSI.codeFactory());
    }
}
