// MONITOR.java, created Wed Aug  5 07:07:51 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>MONITOR</code> represents a synchronization block.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MONITOR.java,v 1.3 1998-08-20 22:43:22 cananian Exp $
 */

public class MONITOR extends Quad {
    /** The object that this monitor should lock. */
    public Temp lock;
    /** The block to be protected by this monitor. */
    public Quad block;
    /** Creates a <code>MONITOR</code>.  The code in <Code>block</code>
     *  is protected by a monitor lock on the object referenced by
     *  <code>lock</code>. */
    public MONITOR(String sourcefile, int linenumber,
		   Temp lock, Quad block) {
        super(sourcefile, linenumber);
	this.lock = lock;
	this.block = block;
    }
    /** Returns a human-readable representation. */
    public String toString() {
	return "MONITOR " + lock + ":";
    }
}
