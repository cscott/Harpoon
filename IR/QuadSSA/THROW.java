// THROW.java, created Sat Aug  8 11:10:56 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>THROW</code> represents a <Code>throw<code> statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: THROW.java,v 1.10 1998-09-11 18:28:23 cananian Exp $
 */

public class THROW extends Quad {
    /* The exception object to throw. */
    public Temp throwable;

    /** Creates a <code>THROW</code>. */
    public THROW(HCodeElement source, Temp throwable) {
        super(source, 1, 1 /* one successor, the footer node. */);
	this.throwable = throwable;
    }

    /** Returns all the Temps used by this Quad. 
     * @return the <code>throwable</code> field. */
    public Temp[] use() { return new Temp[] { throwable }; }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "THROW " + throwable; 
    }
}
