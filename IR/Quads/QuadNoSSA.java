// QuadNoSSA.java, created Sat Dec 26 01:42:53 1998 by cananian
package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>QuadNoSSA</code> is a code view with explicit exception handling.
 * It does not have <code>HANDLER</code> quads, and is not in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadNoSSA.java,v 1.1.2.3 1999-01-22 23:06:00 cananian Exp $
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

    public static void register() {
	HCodeFactory f = new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode c = m.getCode(QuadWithTry.codename);
		return (c==null) ? null :
		    new QuadNoSSA((QuadWithTry)c);
	    }
	    public String getCodeName() { return codename; }
	};
	HMethod.register(f);
    }
}
