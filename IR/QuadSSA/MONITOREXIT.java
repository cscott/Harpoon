// MONITOREXIT.java, created Thu Sep 18  1:18:42 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>MONITOREXIT</code> releases the monitor lock of a particular
 * object.  Note that these java "monitors" are really a flavor of
 * recursive lock; see the 
 * <A HREF="http://www.cs.arizona.edu/sumatra/hallofshame/">Java
 * hall of shame</A> for more details.  The <code>MONITOREXIT</code>
 * quad works the same way as the java <code>monitorexit</code> bytecode.
 * See the JVM spec for details.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MONITOREXIT.java,v 1.1 1998-09-18 07:34:16 cananian Exp $
 */

public class MONITOREXIT extends Quad {
    /** The object containing the monitor to be released. */
    public Temp lock;

    /** Creates a <code>MONITOREXIT</code>. Code after a
     *  <code>MONITORENTER</code> referencing the same object 
     *  and before this point is protected by the monitor. */
    public MONITOREXIT(HCodeElement source, Temp lock) {
        super(source);
	this.lock = lock;
    }
    /** Returns the Temp used by this Quad.
     *  @return the <code>lock</code> field. */
    public Temp[] use() { return new Temp[] { lock }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	lock = tm.tempMap(lock);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation. */
    public String toString() {
	return "MONITOREXIT " + lock;
    }
}
