// METHODHEADER.java, created Thu Aug 20 17:34:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>METHODHEADER</code> is a header node used for methods to 
 * keep track of the temporary variable names used for method parameters.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: METHODHEADER.java,v 1.4 1998-09-09 23:31:40 cananian Exp $
 */

public class METHODHEADER extends HEADER {
    public Temp[] params;
    /** Creates a <code>METHODHEADER</code> from the given parameter
     *  list of the method. */
    public METHODHEADER(HCodeElement source, FOOTER footer, Temp[] params) {
        super(source, footer);
	this.params = params;
    }

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
