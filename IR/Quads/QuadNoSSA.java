// QuadNoSSA.java, created Sat Dec 26 01:42:53 1998 by cananian
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
 * @version $Id: QuadNoSSA.java,v 1.1.2.9 1999-02-08 17:24:30 duncan Exp $
 * @see QuadWithTry
 * @see QuadSSA
 */
public class QuadNoSSA extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-no-ssa";

    /** Creates a <code>QuadNoSSA</code> object from a
     *  <code>QuadWithTry</code> object. */
    QuadNoSSA(QuadWithTry qwt) {
        super(qwt.getMethod(), null);
	this.quads = UnHandler.unhandler(this.qf, qwt);
	Peephole.optimize(this.quads, true);
    }
    QuadNoSSA(QuadSSA qsa) {
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

    /** Return a code factory for QuadNoSSA, given a code factory for
     *  QuadWithTry. */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadWithTry.codename)) {
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((QuadWithTry)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(QuadSSA.codename)) {
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((QuadSSA)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
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
