// THROW.java, created Sat Aug  8 11:10:56 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>THROW</code> represents a <Code>throw<code> statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: THROW.java,v 1.2 1998-08-20 22:43:25 cananian Exp $
 */

public class THROW extends Quad {
    /* The exception object to throw. */
    public Temp temp;

    /** Creates a <code>THROW</code>. */
    public THROW(String sourcefile, int linenumber, Temp temp) {
        super(sourcefile, linenumber);
	this.temp = temp;
    }
    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "THROW " + temp; 
    }
}
