// LET.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
/**
 * <code>LET</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment can be either a constant or another
 * temporary.  It is roughly equivalent to the standard <code>MOVE</code>
 * pseudo-instruction.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LET.java,v 1.2 1998-08-07 09:56:38 cananian Exp $
 */

public class LET extends Quad {
    public Temp dst; // LeafTemp?
    public Leaf src;
    /** Creates a <code>LET</code> from a Leaf and a Temporary. */
    public LET(Temp dst, Leaf src) {
	this.dst = dst;
	this.src = src;
    }
}
