// THROW.java, created Sat Aug  8 11:10:56 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>THROW</code> represents a <Code>throw<code> statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: THROW.java,v 1.5 1998-09-03 01:21:57 cananian Exp $
 */

public class THROW extends Quad {
    /* The exception object to throw. */
    public Temp throwable;

    /** Creates a <code>THROW</code>. */
    public THROW(String sourcefile, int linenumber, Temp throwable) {
        super(sourcefile, linenumber, 1, 0 /* does not return */);
	this.throwable = throwable;
    }
    THROW(HCodeElement hce, Temp throwable) {
	this(hce.getSourceFile(), hce.getLineNumber(), throwable);
    }
    /** Returns all the Temps used by this Quad. 
     * @return the <code>throwable</code> field. */
    public Temp[] use() { return new Temp[] { throwable }; }

    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "THROW " + throwable; 
    }
}
