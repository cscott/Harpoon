// MONITORENTER.java, created Thu Sep 17 21:04:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

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
 * @version $Id: MONITORENTER.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
public class MONITORENTER extends Quad {
    /** The object containing the monitor to be locked. */
    protected Temp lock;

    /** Creates a <code>MONITORENTER</code>. Code after this point and
     *  before a <code>MONITOREXIT</code> with the same <code>lock</code>
     *  reference is protected by a monitor.
     * @param lock 
     *        the <code>Temp</code> referencing the object monitor to lock.
     */
    public MONITORENTER(QuadFactory qf, HCodeElement source, Temp lock) {
        super(qf, source);
	this.lock = lock;
	Util.assert(lock!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> specifying the object to be locked. */
    public Temp lock() { return lock; }

    /** Returns the Temp used by this Quad.
     *  @return the <code>lock</code> field. */
    public Temp[] use() { return new Temp[] { lock }; }

    public int kind() { return QuadKind.MONITORENTER; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new MONITORENTER(qqf, this, map(useMap, lock));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	lock = tm.tempMap(lock);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "MONITORENTER " + lock;
    }
}
