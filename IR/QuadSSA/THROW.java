// THROW.java, created Sat Aug  8 11:10:56 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>THROW</code> represents a <Code>throw<code> statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: THROW.java,v 1.3 1998-08-24 19:30:03 cananian Exp $
 */

public class THROW extends Quad {
    /* The exception object to throw. */
    public Temp temp;

    /** Creates a <code>THROW</code>. */
    public THROW(String sourcefile, int linenumber, Temp temp) {
        super(sourcefile, linenumber);
	this.temp = temp;
    }
    THROW(HCodeElement hce, Temp temp) {
	this(hce.getSourceFile(), hce.getLineNumber(), temp);
    }
    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "THROW " + temp; 
    }
}
