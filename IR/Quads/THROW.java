// THROW.java, created Sat Aug  8 11:10:56 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>THROW</code> represents a <Code>throw</code> statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: THROW.java,v 1.3.2.1 2002-02-27 08:36:33 cananian Exp $
 */
public class THROW extends Quad {
    /* The exception object to throw. */
    protected Temp throwable;

    /** Creates a <code>THROW</code> representing a exception 
     *  throw statement.
     * @param throwable
     *        the <code>Temp</code> containing the exception object
     *        to throw.  Should be a subclass of 
     *        <code>java.lang.Throwable</code>.
     */
    public THROW(QuadFactory qf, HCodeElement source, Temp throwable) {
        super(qf, source, 1, 1 /* one successor, the footer node. */);
	this.throwable = throwable;
	assert throwable!=null;
    }
    /** Returns the <code>Temp</code> containing the exception object to
     *  throw. */
    public Temp throwable() { return throwable; }

    /** Returns all the Temps used by this Quad. 
     * @return the <code>throwable</code> field. */
    public Temp[] use() { return new Temp[] { throwable }; }

    public int kind() { return QuadKind.THROW; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new THROW(qqf, this, map(useMap, throwable));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	throwable = tm.tempMap(throwable);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "THROW " + throwable; 
    }
}
