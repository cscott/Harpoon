// QuadRSSI.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>Quads.QuadRSSI</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an QuadNoSSA form; but <code>PHI</code> and <code>SIGMA</code> functions
 * are allowed, and defined as having implicit <code>MOVE</code>s on the edges.
 * 
 * @author  Brian Demsky
 * @version $Id: QuadRSSI.java,v 1.1.2.1 2000-02-13 04:38:09 bdemsky Exp $
 */
public class QuadRSSI extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "relaxed-quad-ssi";

    /** Creates a <code>Code</code> object from a bytecode object. */
    
    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.
     */
    protected QuadRSSI(HMethod parent, Quad quads) {
	super(parent, quads);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCode clone(HMethod newMethod) {
	QuadRSSI qs = new QuadRSSI(newMethod, null);
	qs.quads = Quad.clone(qs.qf, quads);
	return qs;
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"relaxed-quad-ssi"</code>.
     */
    public String getName() { return codename; }
}
