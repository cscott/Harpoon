// LowQuadSSA.java, created Wed May 31 16:18:58 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.SSIToSSA;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
/**
 * The <code>LowQuadSSA</code> codeview exposes a lowquad based
 * representation in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadSSA.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public class LowQuadSSA extends Code /*which extends harpoon.IR.Quads.Code*/ {
    /** The name of this code view. */
    public static final String codename = "low-quad-ssa";
    
    /** Creates a <code>LowQuadSSA</code> object from a
     *  <code>LowQuadSSI</code> object. */
    public LowQuadSSA(final LowQuadSSI code) {
        super(code.getMethod(), null);
	SSIToSSA ssi2ssa = new SSIToSSA(code, qf);
	quads = ssi2ssa.rootQuad;
	setDerivation(ssi2ssa.derivation);
	setAllocationInformation(ssi2ssa.allocInfo);
    }

    /**
     * Create a new code object given a quadruple representation of the
     * method instructions.
     */
    protected LowQuadSSA(HMethod method, Quad quads) {
        super(method, quads);
	setDerivation(null);
	setAllocationInformation(null);
    }

    /**
     * Clone this code representation.  The clone has its own copy of the
     * quad graph.
     * <p><b>WARNING: does not preserve derivation or allocation info.</b>
     */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new LowQuadSSA(newMethod, null));
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"low-quad-ssa"</code>
     */
    public String getName() { return codename; }

    /**
     * Return a code factory for <code>LowQuadSSA</code>, given a 
     * code factory for <code>LowQuadSSI</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>LowQuadSSI</code>, then creates and returns a code
     *      factory for <code>LowQuadSSA</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>LowQuadSSI.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>LowQuadSSA</code> from the
     *      code factory returned by <code>LowQuadSSI</code>.
     * @see LowQuadSSI#codeFactory(HCodeFactory)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
        if (hcf.getCodeName().equals(LowQuadSSI.codename)) {
            return new harpoon.ClassFile.SerializableCodeFactory() {
                public HCode convert(HMethod m) {
                    HCode c = hcf.convert(m);
                    return (c==null) ? null : new LowQuadSSA((LowQuadSSI)c);
                }
                public void clear(HMethod m) { hcf.clear(m); }
                public String getCodeName() { return codename; }
            };
        } else {
            return codeFactory(LowQuadSSI.codeFactory(hcf));
        }
    }
  
    /**
     * Return a code factory for <code>LowQuadSSA</code>, using the default
     * code factory for <code>LowQuadSSI</code>
     */
    public static HCodeFactory codeFactory() {  
        return codeFactory(LowQuadSSI.codeFactory());
    }
}
