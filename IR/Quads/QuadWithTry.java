// QuadWithTry.java, created Sat Dec 19 23:55:52 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>QuadWithTry</code> is a code view with explicit try-block
 * handlers.  <code>QuadWithTry</code> is not in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadWithTry.java,v 1.1.2.11 1999-08-19 04:06:57 bdemsky Exp $
 * @see QuadNoSSA
 * @see QuadSSA
 */
public class QuadWithTry extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-with-try";
    
    /** Creates a <code>QuadWithTry</code> object from a
     *  <code>harpoon.IR.Bytecode.Code</code> object. */
    QuadWithTry(harpoon.IR.Bytecode.Code bytecode) {
        super(bytecode.getMethod(), null);
	quads = Translate.trans(bytecode, this);
	Peephole.normalize(quads); // put variables where they belong.
	Peephole.optimize(quads,false); //don't disrupt the handlers too much
	// if we allow far moves, the state which the handlers expect is
	// destroyed.  not sure how to make the optimization handler-safe.
	// maybe don't allow moves past instructions that might throw
	// exceptions?
    }

    QuadWithTry(harpoon.IR.Quads.QuadSSA quad) {
        super(quad.getMethod(), null);
	quads = ReHandler.rehandler(this.qf, quad);
    }

    private QuadWithTry(HMethod parent, Quad quads) {
	super(parent, quads);
    }
    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. */
    public HCode clone(HMethod newMethod) {
	QuadWithTry qwt = new QuadWithTry(newMethod, null);
	qwt.quads = Quad.clone(qwt.qf, quads);
	return qwt;
    }
    /**
     * Return the name of this code view.
     * @return the string <code>"quad-with-try"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>QuadWithTry</code>, given a
     *  code factory for <code>Bytecode</code> or <code>QuadNoSSA</code>.
     *  Given a code factory for <code>QuadSSA</code>, chain through 
     *  <code>QuadNoSSA.codeFactory()</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadWithTry((harpoon.IR.Bytecode.Code)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSA.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadWithTry((harpoon.IR.Quads.QuadSSA)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename)) {
	    return codeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadWithTry, using the default
     *  code factory for Bytecode. */
    public static HCodeFactory codeFactory() {
	return codeFactory(harpoon.IR.Bytecode.Code.codeFactory());
    }
    // obsolete.
    public static void register() {
	HMethod.register(codeFactory());
    }
}
