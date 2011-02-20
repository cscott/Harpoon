// QuadRSSx.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>Quads.QuadRSSx</code> is a code view in relaxed SSI form.
 * Quad form exposes the details of
 * the java classfile bytecodes in a pseudo-quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an QuadNoSSA form (that is, there are no contraints on variable
 * definitions/re-definitions); but <code>PHI</code> and <code>SIGMA</code>
 * functions are allowed.  <code>PHI</code> and <code>SIGMA</code> nodes
 * behave semantically as their no-ssa conversion; that is, execution
 * proceeds as if there were implicit <code>MOVE</code>s on the edges
 * leading to/coming from the <code>PHI</code>/<code>SIGMA</code> nodes.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: QuadRSSx.java,v 1.3 2002-09-01 07:47:20 cananian Exp $
 */
public class QuadRSSx extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "relaxed-quad-ssx";

    /** Creates a <code>Code</code> object from a bytecode object. */
    
    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.
     */
    protected QuadRSSx(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCodeAndMaps<Quad> clone(HMethod newMethod) {
	return cloneHelper(new QuadRSSx(newMethod, null));
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"relaxed-quad-ssi"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>QuadRSSx</code>, given a code
     *  factory for <code>QuadNoSSA</code> or <code>QuadSSI</code>.
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(QuadNoSSA.codename) ||
	    hcf.getCodeName().equals(QuadSSA.codename) ||
	    hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    Code c = (Code) hcf.convert(m);
		    return (c==null) ? null :
			c.cloneHelper(new QuadRSSx(m, null)).hcode();
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
}
