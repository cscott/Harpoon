// CALL.java, created Wed Aug  5 06:48:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>CALL</code> objects represent method invocations.<p>
 * The <code>retval</code> field will be <code>null</code>
 * for <code>void</code> methods.  For non-static methods, the
 * method receiver (object reference on which to invoke the method)
 * is the first parameter in the <code>params</code> array.<p>
 *
 * <strong>It is a semantic error for the receiver
 * of a non-static method <code>CALL</code> (first parameter of a
 * virtual/non-static method) to be <i>able to have</i> the value
 * <code>null</code> at run-time.</strong> A separate null-pointer
 * test should always precede the <code>CALL</code> quad if the
 * receiver may be null at run-time.  Standard java
 * invocation throws a <code>NullPointerException</code> if the 
 * receiver reference is <code>null</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.27.2.2 1998-11-30 21:21:02 cananian Exp $ 
 */

public class CALL extends Quad {
    /** The method to invoke. */
    public HMethod method;
    /** Parameters to pass to the method. 
     *  The object on which to invoke the method is the first element in 
     *  the parameter list of a virtual method. */
    public Temp[] params;
    /** Destination for the method's return value; 
     *  <code>null</code> for <code>void</code> methods. */
    public Temp retval;
    /** Destination for any exception thrown by the method. 
     *  Must be non-<code>null</code>. */
    public Temp retex;
    /** Special flag for INVOKESPECIAL (different invoke semantics) */
    public boolean isSpecial;

    /** Creates a <code>CALL</code>. <code>params</code> should match
     *  exactly the number of parameters in the method descriptor for a
     *  static method, and/or contain an extra receiver object reference
     *  as element 0 of <code>params</code> for a virtual method.
     *  The <code>retval</code> field should be <code>null</code> if the
     *  method returns no value.  The <code>retex</code> field
     *  will always be a valid <code>Temp</code>.  If an exception is
     *  thrown by the called method, <code>retex</code> will be assigned
     *  a non-null value and <code>retval</code> will be null.  If
     *  no exception is thrown, <code>retex</code> will be null. */
    public CALL(HCodeElement source, HMethod method, Temp[] params, 
		Temp retval, Temp retex, boolean isSpecial) {
	super(source);
	this.method = method;
	this.params = params;
	this.retval = retval;
	this.retex  = retex;
	this.isSpecial = isSpecial;
	// check miscellanea.
	Util.assert(retex!=null && params!=null && method!=null);
	// check params and retval here against method.
	if (method.getReturnType()==HClass.Void) Util.assert(retval==null);
	else Util.assert(retval!=null);
	Util.assert((method.getParameterTypes().length + (isStatic()?0:1)) ==
		    params.length);
	// I guess it's legal, then.
    }

    /** Returns all the Temps used by this Quad. 
     * @return the <code>params</code> array.
     */
    public Temp[] use() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, params);
    }
    /** Returns all the Temps defined by this Quad. 
     * @return { retval, retex }, if retval!=null; else { retex }
     */
    public Temp[] def() {
	if (retval==null) return new Temp[] { retex };
	else return new Temp[] { retval, retex };
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	if (retval!=null)
	    retval = tm.tempMap(retval);
	retex  = tm.tempMap(retex);
    }

    /** Properly clone <code>params[]</code> array. */
    public Object clone() {
	CALL q = (CALL) super.clone();
	q.params = (Temp[]) params.clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	if (retval!=null)
	    sb.append(retval.toString() + " = ");
	sb.append("CALL ");
	if (isSpecial)
	    sb.append("(special) ");
	if (isStatic())
	    sb.append("static ");
	sb.append(method.getDeclaringClass().getName()+"."+method.getName());
	sb.append('(');
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(')');
	sb.append(" exceptions in "+retex);
	return sb.toString();
    }
    // Other information that might be useful.  Or might not.  Who knows?
    /** Determines whether this <code>CALL</code> is to an interface method. */
    public boolean isInterfaceMethod() { return method.isInterfaceMethod(); }
    /** Determines whether this <code>CALL</code> is to a static method. */
    public boolean isStatic() 
    { return method.isStatic(); }
    /** Determine whether this <code>CALL</code> uses INVOKESPECIAL
     *  semantics. */
    public boolean isSpecial()
    { return isSpecial; }
}
