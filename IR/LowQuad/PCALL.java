// PCALL.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PCALL</code> objects represent a method pointer dereference and
 * invocation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PCALL.java,v 1.1.2.2 1999-09-09 21:42:59 cananian Exp $
 */
public class PCALL extends LowQuad {
    /** The method pointer to dereference. */
    protected final Temp ptr;
    /** Parameters to pass to the method. */
    protected Temp[] params;
    /** Destination for the method's return value;
     *  <code>null</code> for <code>void</code> methods. */
    protected Temp retval;
    /** Destination for any exception thrown by the method.
     *  May not be <code>null</code>. */
    protected Temp retex;

    /** Creates a <code>PCALL</code> representing a method pointer dereference
     *  and method invocation. Interpretation is similar to that of
     *  <code>harpoon.IR.Quads.CALL.<br>
     *  If an exception is thrown by the called method, the <code>Temp</code>
     *  specified by <code>retex</code> will be assigned the non-null 
     *  reference to the thrown exception, and the <code>Temp</code>
     *  specified by the <code>retval</code> field (if any) will have
     *  an indeterminate value.  If no exception is thrown, the 
     *  <code>Temp</code> specified by <code>retex</code> will be assigned
     *  <code>null</code>, and the return value will be assigned to the
     *  <code>Temp</code> specified by <code>retval</code> (if any).
     * @param ptr
     *        the method pointer to dereference and invoke.
     * @param params
     *        an array of <code>Temp</code>s containing the parameters
     *        to pass to the method.  The object on which to invoke the 
     *        method is the first element in the parameter list of a
     *        virtual method; non-virtual methods do not need to specify a
     *        receiver.  For non-virtual methods, <code>params</code>
     *        should match exactly the number and types of parameters in 
     *        the method descriptor.  For virtual methods, the receiver
     *        object (which is not included in the descriptor) is element
     *        zero of the <code>params</code> array.
     * @param retval
     *        the destination <code>Temp</code> for the method's return
     *        value, or <code>null</code> if the method returns no
     *        value (return type is <code>void</code>.
     * @param retex
     *        the destination <code>Temp</code> for any exception thrown
     *        by the called method.  May not be <code>null</code>.
     */
    public PCALL(LowQuadFactory qf, HCodeElement source,
		 Temp ptr, Temp[] params, Temp retval, Temp retex) {
	super(qf, source);
	this.ptr = ptr;
	this.params = params;
	this.retval = retval;
	this.retex = retex;
	Util.assert(ptr!=null && params!=null && retex !=null);
	// hm.  can't check much else without knowing the method identity.
    }
    // ACCESSOR METHODS:
    /** Returns the <code>POINTER</code> which is to be dereferenced by this
     *  <code>PCALL</code>. */
    public Temp ptr() { return ptr; }
    /** Returns the parameters of this method invocation. */
    public Temp[] params()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, params); }
    /** Returns a specified parameter in the <code>params</code> array. */
    public Temp params(int i) { return params[i]; }
    /** Returns the number of parameters in the <code>params</code> array. */
    public int paramsLength() { return params.length; }
    /** Returns the <code>Temp</code> which will hold the return value of
     *  the method, or the value <code>null</code> if the method returns
     *  no value. */
    public Temp retval() { return retval; }
    /** Returns the <code>Temp</code> which will get any exception thrown
     *  by the called method, or <code>null</code> if exceptions are
     *  not caught. */
    public Temp retex() { return retex; }

    public int kind() { return LowQuadKind.PCALL; }

    public Temp[] use() {
	return (Temp[]) Util.grow(Temp.arrayFactory, params, ptr, 0);
    }
    public Temp[] def() {
	if (retval==null) return new Temp[] { retex };
	else return new Temp[] { retval, retex };
    }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PCALL((LowQuadFactory)qf, this,
			 map(useMap, ptr), map(useMap, params),
			 map(defMap, retval), map(defMap, retex));
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	if (retval != null)
	    sb.append(retval.toString() + " = ");
	sb.append("PCALL *"+ptr.toString());

	sb.append('(');
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(')');

	sb.append(" exceptions in "+retex.toString());
	return sb.toString();
    }
}
