// HANDLER.java, created Tue Dec 15 17:06:51 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Enumeration;

/**
 * A <code>HANDLER</code> quad marks an entry to an exception handler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HANDLER.java,v 1.1.2.1 1998-12-17 21:38:36 cananian Exp $
 * @see METHOD
 */
public class HANDLER extends Quad {
    /** The exception caught by this <code>HANDLER</code>. */
    protected HClass caughtException;
    /** The set of <code>Quad</code>s protected by this <code>HANDLER</code>.*/
    protected ProtectedSet protectedSet;

    /** Creates a <code>HANDLER</code>.
     * @param caughtException
     *        the exception type caught by this <code>HANDLER</code>.
     * @param protectedSet
     *        the set of <code>Quad</code>s protected by this
     *        <code>HANDLER</code>.
     */
    public HANDLER(QuadFactory qf, HCodeElement source,
		   HClass caughtException, ProtectedSet protectedSet) {
        super(qf, source);
	this.caughtException = caughtException;
	this.protectedSet = protectedSet;
    }
    /** Returns the superclass of the exceptions caught by this
     *  <code>HANDLER</code>. */
    public HClass caughtException() { return caughtException; }
    /** Returns <code>true</code> if the given exception <code>HClass</code>
     *  is caught by this <code>HANDLER</code>. */
    public boolean isCaught(HClass hc) {
	return caughtException.isSuperclassOf(hc);
    }
    /** Returns <code>true</code> if the given <code>Quad</code> is
     *  protected by this <code>HANDLER</code>; <code>false</code> otherwise.*/
    public boolean isProtected(Quad q) {
	return protectedSet.isProtected(q);
    }
    /** Returns an <code>Enumeration</code> of the <code>Quad</code>s
     *  protected by this <code>HANDLER</code>. */
    public Enumeration protectedQuads() {
	return protectedSet.elements();
    }

    public int kind() { return QuadKind.HANDLER; }

    public Quad rename(QuadFactory qqf, TempMap tm) {
	return new HANDLER(qqf, this, caughtException, protectedSet);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this <Code>Quad</code>. */
    public String toString() { return "HANDLER for "+caughtException; }

    public interface ProtectedSet {
	public boolean isProtected(Quad q);
	public Enumeration elements();
    }
}
