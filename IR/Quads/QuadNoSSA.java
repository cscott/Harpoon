// QuadNoSSA.java, created Sat Dec 26 01:42:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

import java.util.Hashtable;

/**
 * <code>QuadNoSSA</code> is a code view with explicit exception handling.
 * It does not have <code>HANDLER</code> quads, and is not in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadNoSSA.java,v 1.1.2.14 1999-09-08 16:35:33 cananian Exp $
 * @see QuadWithTry
 * @see QuadSSI
 */
public class QuadNoSSA extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-no-ssa";

    /** Creates a <code>QuadNoSSA</code> object from a
     *  <code>QuadWithTry</code> object. */
    QuadNoSSA(QuadWithTry qwt, boolean coalesce) {
        super(qwt.getMethod(), null);
	this.quads = UnHandler.unhandler(this.qf, qwt, coalesce);
	Peephole.optimize(this.quads, true);
    }
    QuadNoSSA(QuadSSI qsa) {
	super(qsa.getMethod(), null);
	ToNoSSA translator = new ToNoSSA(this.qf, qsa);
	this.quads = translator.getQuads();
    }
    private QuadNoSSA(HMethod parent, Quad quads) {
	super(parent, quads);
    }
    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. */
    public HCode clone(HMethod newMethod) {
	QuadNoSSA qns = new QuadNoSSA(newMethod, null);
	qns.quads = Quad.clone(qns.qf, quads);
	return qns;
    }
    /**
     * Return the name of this code view.
     * @return the string <code>"quad-no-ssa"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>QuadNoSSA</code>, given a code
     *  factory for <code>QuadWithTry</code> or <code>QuadSSI</code>.
     *  Given a code factory for <code>Bytecode</code>, chain through
     *  <code>QuadWithTry.codeFactory()</code>.  */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadWithTry.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((QuadWithTry)c, true);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((QuadSSI)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)){
	    // implicit chaining
	    return codeFactory(QuadWithTry.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadNoSSA, using the default code
     *  factory for QuadWithTry. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadWithTry.codeFactory());
    }
    
    // obsolete.
    public static void register() {
	HMethod.register(codeFactory());
    }
}
