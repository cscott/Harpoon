// QuadSSI.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>Quads.QuadSSI</code> is a code view in SSI form.
 * Quad form exposes the details of
 * the java classfile bytecodes in a pseudo-quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSI form; that is, every variable has exactly one definition,
 * and <code>PHI</code> and <code>SIGMA</code> functions are used where
 * control flow merges or splits, respectively.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadSSI.java,v 1.1.2.8 2000-02-25 17:32:07 cananian Exp $
 */
public class QuadSSI extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-ssi";

    /** Creates a <code>Code</code> object from a bytecode object. */
    public QuadSSI(QuadNoSSA qns) 
    {
	super(qns.getMethod(), null);
	quads = SSIRename.rename(qns, qf);
	DeadCode.optimize(this); // get rid of unused phi/sigmas.
    }

    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.
     */
    protected QuadSSI(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCode clone(HMethod newMethod) {
	QuadSSI qs = new QuadSSI(newMethod, null);
	qs.quads = Quad.clone(qs.qf, quads);
	return qs;
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"quad-ssi"</code>.
     */
    public String getName() { return codename; }
    
    /** Return a code factory for <code>QuadSSI</code>, given a code
     *  factory for <code>QuadNoSSA</code>.  Given a code factory for
     *  <code>Bytecode</code> or <code>QuadWithTry</code>, chain
     *  through <code>QuadNoSSA.codeFactory()</code>.
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadNoSSA.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null)?null:new QuadSSI((QuadNoSSA)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)
		   || hcf.getCodeName().equals(QuadWithTry.codename)||
		   hcf.getCodeName().equals(QuadRSSI.codename)) {
	    // do some implicit chaining.
	    return codeFactory(QuadNoSSA.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename + 
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadSSI, using the default code factory
     *  for QuadNoSSA. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadNoSSA.codeFactory());
    }
}
