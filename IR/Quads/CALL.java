// CALL.java, created Wed Aug  5 06:48:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

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
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.1.2.5 1998-12-20 07:11:59 cananian Exp $ 
 */
public class CALL extends Quad {
    /** The method to invoke. */
    final protected HMethod method;
    /** Parameters to pass to the method. 
     *  The object on which to invoke the method is the first element in 
     *  the parameter list of a virtual method. */
    protected Temp[] params;
    /** Destination for the method's return value; 
     *  <code>null</code> for <code>void</code> methods. */
    protected Temp retval;
    /** Destination for any exception thrown by the method. 
     *  If <code>null</code> exceptions are thrown, not caught. */
    protected Temp retex;
    /** Special flag for non-virtual methods.
     *  (INVOKESPECIAL has different invoke semantics) */
    final protected boolean isVirtual;

    /** Creates a <code>CALL</code> quad representing a method invocation.
     *  If an exception is thrown by the called method, the <code>Temp</code>
     *  specified by <code>retex</code> will be assigned the non-null 
     *  reference to the thrown exception, and the <code>Temp</code>
     *  specified by the <code>retval</code> field (if any) will have
     *  an indeterminate value.  If no exception is thrown, the 
     *  <code>Temp</code> specified by <code>retex</code> will be assigned
     *  <code>null</code>, and the return value will be assigned to the
     *  <code>Temp</code> specified by <code>retval</code> (if any).
     * @param method
     *        the method to invoke.
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
     *        by the called method.  If <code>null</code> exceptions are
     *        thrown, not caught.
     * @param isVirtual
     *        <code>true</code> if invocation semantics are that of a
     *        virtual method; <code>false</code> for constructors and
     *        static initializers with non-virtual invocation semantics.
     *        Value is unspecified for static methods, although the
     *        <code>isVirtual()</code> method will always return 
     *        <code>false</code> in this case.
     */
    public CALL(QuadFactory qf, HCodeElement source,
		HMethod method, Temp[] params, Temp retval, Temp retex,
		boolean isVirtual) {
	super(qf, source);
	this.method = method;
	this.params = params;
	this.retval = retval;
	this.retex  = retex;
	// static methods are not virtual.
	this.isVirtual = isStatic()?false:isVirtual;

	// VERIFY legality of this CALL.
	Util.assert(method!=null && params!=null);
	/* if !isVirtual, then the method is either static, a constructor,
	 * private, or a the declaring class of the method is a superclass
	 * of the current method.   This is hard to check, so we do it
	 * the other way round. */
	if (this.isVirtual) 
	    Util.assert(!method.isStatic() && 
			!(method instanceof HConstructor) &&
			!Modifier.isPrivate(method.getModifiers()),
			"meaning of final parameter to CALL constructor "+
			"has been inverted.  isVirtual should be true "+
			"unless the called method is a constructor, static, "+
			"private, or declared in a superclass of the "+
			"current method.");
	// check params and retval against method.
	if (method.getReturnType()==HClass.Void) Util.assert(retval==null);
	else Util.assert(retval!=null);
	Util.assert((method.getParameterTypes().length + (isStatic()?0:1)) ==
		    params.length);
	if (isStatic()) Util.assert(isVirtual()==false);
	// I guess it's legal, then.
    }
    // ACCESSOR METHODS:
    /** Returns the method invoked by this <code>CALL</code>. */
    public HMethod method() { return method; }
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
    /** Returns <code>true</code> if the method is dispatched virtually,
     *  or <code>false</code> otherwise.  Static methods return 
     *  <code>false</code>, constructors and static initializers return
     *  <code>false</code>, and all other method types return 
     *  <code>true</code>. */
    public boolean isVirtual() { return isVirtual; }

    /** Returns all the Temps used by this Quad. 
     * @return the <code>params</code> array.
     */
    public Temp[] use() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, params);
    }
    /** Returns all the Temps defined by this Quad. 
     * @return The non-null members of <code>{ retval, retex }</code>.
     */
    public Temp[] def() {
	if (retval==null)
	    return (retex==null)?new Temp[0]:new Temp[]{retex};
	else
	    return (retex==null)?new Temp[]{retval}:new Temp[]{retval,retex};
    }

    public int kind() { return QuadKind.CALL; }

    public Quad rename(QuadFactory qqf, TempMap tm) {
	return new CALL(qqf, this, method, map(tm, params), map(tm,retval),
			map(tm, retex), isVirtual);
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	if (retval!=null)
	    retval = tm.tempMap(retval);
	if (retex!=null)
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
	if (!isVirtual)
	    sb.append("(non-virtual) ");
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
	if (retex!=null)
	    sb.append(" exceptions in "+retex);
	else
	    sb.append(" exceptions THROWN DIRECTLY.");
	return sb.toString();
    }
    // Other information that might be useful.  Or might not.  Who knows?
    /** Determines whether this <code>CALL</code> is to an interface method. */
    public boolean isInterfaceMethod() { return method.isInterfaceMethod(); }
    /** Determines whether this <code>CALL</code> is to a static method. */
    public boolean isStatic() { return method.isStatic(); }
}
