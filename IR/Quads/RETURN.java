// RETURN.java, created Wed Aug  5 06:46:49 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>RETURN</code> objects indicate a method return, with an
 * optional return value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RETURN.java,v 1.2 2002-02-25 21:05:13 cananian Exp $
 */
public class RETURN extends Quad {
    /** Return value. <code>null</code> if there is no return value. */
    protected Temp retval;

    /** Creates a <code>RETURN</code> representing a method return.
     * @param retval
     *        the <code>Temp</code> holding the return value for the
     *        method.  The <code>retval</code> field should be 
     *        <code>null</code> if the method does not return a value.
     */
    public RETURN(QuadFactory qf, HCodeElement source, Temp retval) {
	super(qf, source, 1, 1 /* one successor, the footer node. */);
	this.retval = retval;
	// nothing to verify.
    }
    /** Returns the <code>Temp</code> which holds the method return value,
     *  or returns <code>null</code> if the method returns no value. */
    public Temp retval() { return retval; }

    /** Returns all the Temps used by this Quad. */
    public Temp[] use() {
	if (retval==null) return new Temp[0];
	else return new Temp[] { retval }; 
    }

    public int kind() { return QuadKind.RETURN; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new RETURN(qqf, this, map(useMap,retval));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	if (retval!=null)
	    retval = tm.tempMap(retval);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	if (retval==null) return "RETURN";
	return "RETURN " + retval.toString();
    }
}
