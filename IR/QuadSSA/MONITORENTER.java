// MONITORENTER.java, created Thu Sep 17 21:04:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>MONITORENTER</code> acquires the monitor lock of a particular
 * object.  Note that these java "monitors" are really a flavor of
 * recursive lock; see the 
 * <A HREF="http://www.cs.arizona.edu/sumatra/hallofshame/">Java
 * hall of shame</A> for more details.  The <code>MONITORENTER</code>
 * quad works the same way as the java <code>monitorenter</code> bytecode.
 * See the JVM spec for details.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MONITORENTER.java,v 1.2 1998-10-11 02:37:57 cananian Exp $
 */

public class MONITORENTER extends Quad {
    /** The object containing the monitor to be locked. */
    public Temp lock;

    /** Creates a <code>MONITORENTER</code>. Code after this point and
     *  before a <code>MONITOREXIT</code> with the same <code>lock</code>
     *  reference is protected by a monitor. */
    public MONITORENTER(HCodeElement source, Temp lock) {
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
	return "MONITORENTER " + lock;
    }
}
