// MONITOREXIT.java, created Thu Sep 18  1:18:42 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

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
 * @version $Id: MONITOREXIT.java,v 1.5 2002-04-11 04:00:34 cananian Exp $
 */
public class MONITOREXIT extends Quad {
    /** The object containing the monitor to be released. */
    protected Temp lock;

    /** Creates a <code>MONITOREXIT</code>. Code after a
     *  <code>MONITORENTER</code> referencing the same object 
     *  and before this point is protected by the monitor.
     * @param lock
     *        the <code>Temp</code> referencing the object monitor to release.
     */
    public MONITOREXIT(QuadFactory qf, HCodeElement source, Temp lock) {
        super(qf, source);
	this.lock = lock;
	assert lock!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> specifying the object to be released. */
    public Temp lock() { return lock; }

    /** Returns the Temp used by this Quad.
     *  @return the <code>lock</code> field. */
    public Temp[] use() { return new Temp[] { lock }; }

    public int kind() { return QuadKind.MONITOREXIT; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new MONITOREXIT(qqf, this, map(useMap, lock));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	lock = tm.tempMap(lock);
    }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "MONITOREXIT " + lock;
    }
}
