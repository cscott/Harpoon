// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.TempMap;
/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.10 1998-09-13 23:57:25 cananian Exp $
 * @see FOOTER
 */

public class HEADER extends Quad {
    public FOOTER footer;

    /** Creates a <code>HEADER</code>. */
    public HEADER(HCodeElement source, FOOTER footer) {
        super(source, 0 /* no predecessors */, 1);
	this.footer = footer;
    }

    /** Rename all variables in a Quad according to a mapping. */
    public void rename(TempMap tm) { }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() { 
	return "HEADER: footer is #"+footer.getID();
    }
}
