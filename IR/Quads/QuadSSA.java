// QuadSSA.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Map;
/**
 * <code>Quads.QuadSSA</code> is a code view in SSA form.
 * Quad form exposes the details of
 * the java classfile bytecodes in a pseudo-quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where
 * control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadSSA.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
public class QuadSSA extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-ssa";

    /** Creates a <code>Code</code> object from a bytecode object. */
    public QuadSSA(QuadSSI code) 
    {
	super(code.getMethod(), null);
	SSIToSSA ssi2ssa = new SSIToSSA(code, qf);
	quads = ssi2ssa.rootQuad;
	setAllocationInformation(ssi2ssa.allocInfo);
	// no derivation in high-quad form.
    }

    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.
     */
    protected QuadSSA(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new QuadSSA(newMethod, null));
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"quad-ssa"</code>.
     */
    public String getName() { return codename; }
    
    /** Return a code factory for <code>QuadSSA</code>, given a code
     *  factory for <code>QuadNoSSA</code>.  Given a code factory for
     *  <code>Bytecode</code> or <code>QuadWithTry</code>, chain
     *  through <code>QuadNoSSA.codeFactory()</code>.
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null)?null:new QuadSSA((QuadSSI)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)
		   || hcf.getCodeName().equals(QuadWithTry.codename)
		   || hcf.getCodeName().equals(QuadNoSSA.codename)
		   || hcf.getCodeName().equals(QuadRSSx.codename)) {
	    // do some implicit chaining.
	    return codeFactory(QuadSSI.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename + 
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadSSA, using the default code factory
     *  for QuadSSI. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadSSI.codeFactory());
    }
}
