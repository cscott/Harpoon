// METHODHEADER.java, created Thu Aug 20 17:34:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>METHODHEADER</code> is a header node used for methods to 
 * keep track of the temporary variable names used for method parameters.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: METHODHEADER.java,v 1.10 1998-09-16 06:32:48 cananian Exp $
 */

public class METHODHEADER extends HEADER {
    public Temp[] params;
    /** Creates a <code>METHODHEADER</code> from the given parameter
     *  list of the method. */
    public METHODHEADER(HCodeElement source, FOOTER footer, Temp[] params) {
        super(source, footer);
	this.params = params;
    }

    /** Returns the temps defined by this Quad. */
    public Temp[] def() { return (Temp[]) Util.copy(params); }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation. */
    public String toString() {
	StringBuffer sb = new StringBuffer("METHODHEADER(");
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(")");
	sb.append(": footer is #"+footer.getID());
	return sb.toString();
    }
}
