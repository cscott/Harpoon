// MOVE.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>MOVE</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment must be another temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MOVE.java,v 1.4 1998-09-09 23:39:31 cananian Exp $
 */

public class MOVE extends Quad {
    /** The destination temp. */
    public Temp dst;
    /** The source temp. */
    public Temp src;
    
    /** Creates a <code>MOVE</code> from a source and destination Temporary. */
    public MOVE(HCodeElement source,
	       Temp dst, Temp src) {
	super(source);
	this.dst = dst; this.src = src;
    }
    
    /** Returns the Temps used by this Quad. */
    public Temp[] use() { return new Temp[] { src }; }
    /** Returns the Temps defined by this Quad. */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Returns a human-readable representation of this Quad. */
    public String toString() { 
	return dst.toString() + " = MOVE " + src.toString();
    }
}
