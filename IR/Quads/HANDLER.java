// HANDLER.java, created Tue Dec 15 17:06:51 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Enumeration;

/**
 * A <code>HANDLER</code> quad marks an entry to an exception handler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HANDLER.java,v 1.1.2.8 1999-06-24 00:03:11 cananian Exp $
 * @see METHOD
 */
public class HANDLER extends Quad {
    /** The <code>Temp</code> holding the caught exception on invocation of
     *  this <code>HANDLER</code>. */
    protected Temp exceptionTemp;
    /** The exception caught by this <code>HANDLER</code>,
     *  or <code>null</code> for any exception. */
    protected HClass caughtException;
    /** The set of <code>Quad</code>s protected by this <code>HANDLER</code>.*/
    protected ProtectedSet protectedSet;

    /** Creates a <code>HANDLER</code>.
     * @param exceptionTemp
     *        the <code>Temp</code> holding the caught exception on
     *        invocation of this <code>HANDLER</code>.
     * @param caughtException
     *        the exception type caught by this <code>HANDLER</code>,
     *        or <code>null</code> for any exception.
     * @param protectedSet
     *        the set of <code>Quad</code>s protected by this
     *        <code>HANDLER</code>.
     */
    public HANDLER(QuadFactory qf, HCodeElement source, Temp exceptionTemp,
		   HClass caughtException, ProtectedSet protectedSet) {
        super(qf, source);
	this.exceptionTemp = exceptionTemp;
	this.caughtException = caughtException;
	this.protectedSet = protectedSet;
	Util.assert(exceptionTemp!=null && protectedSet!=null);
	Util.assert(caughtException==null ||
		    HClass.forName("java.lang.Throwable")
		    .isSuperclassOf(caughtException));
    }
    /** Returns the <code>Temp</code> which will hold the exception on
     *  the invocation of this <code>HANDLER</code>. */
    public Temp exceptionTemp() { return exceptionTemp; }
    /** Returns the superclass of the exceptions caught by this
     *  <code>HANDLER</code>, or <code>null</code> if any exception
     *  is caught. */
    public HClass caughtException() { return caughtException; }
    /** Returns <code>true</code> if the given exception <code>HClass</code>
     *  is caught by this <code>HANDLER</code>. */
    public boolean isCaught(HClass hc) {
	if (caughtException==null) return true; // any exception is caught.
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

    public Temp[] def() { return new Temp[] { exceptionTemp }; }

    public int kind() { return QuadKind.HANDLER; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new HANDLER(qqf, this, map(defMap, exceptionTemp),
			   caughtException, protectedSet);
    }
    /* Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	exceptionTemp = tm.tempMap(exceptionTemp);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this <Code>Quad</code>. */
    public String toString() {
	return exceptionTemp.toString() + " = HANDLER for " +
	    ((caughtException==null)?"any":caughtException.toString());
    }

    /** An interface to specify which <code>Quad</code>s a particular
     *  <code>HANDLER</code> protects. */
    public interface ProtectedSet {
	/** Determines whether the specified <code>Quad</code> is a
	 *  member of the protected set. */
	public boolean isProtected(Quad q);
	/** Enumerate all the elements of the protected set. */
	public Enumeration elements();
	/** Remove a quad from the protected set. */
	void remove(Quad q);
	/** Add a quad into the protected set. */
	void insert(Quad q);
    }
}
