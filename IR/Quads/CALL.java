// CALL.java, created Wed Aug  5 06:48:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>CALL</code> objects represent method invocations. <p>
 * The <code>retval</code> field will be <code>null</code>
 * for <code>void</code> methods.  For non-static methods, the
 * method receiver (object reference on which to invoke the method)
 * is the first parameter in the <code>params</code> array.
 * <p>
 * <code>CALL</code> behaves like a conditional branch: if
 * no exception is thrown by the called method, the <code>Temp</code>
 * specified by <code>retval</code> will be assigned the return
 * value, if any, and execution will follow the first outgoing
 * edge, <code>nextEdge(0)</code>.  If an exception is thrown
 * then the <code>Temp</code> specified by <code>retex</code> will
 * be assigned the non-null reference to the thrown exception
 * and execution will follow the second outgoing edge,
 * <code>nextEdge(1)</code>.  Calls with explicit exception
 * handling always have exactly two outgoing edges.
 * <p>
 * In quad-with-try form, the <code>CALL</code> has only one
 * outgoing edge, and exceptions are handled by an implicit
 * control transfer to an appropriate <code>HANDLER</code> quad.
 * The <code>retex</code> field should be <code>null</code> in
 * this case (and only in this case).
 * <p>
 * Note that <b>exactly one</b> of { <code>retval</code>, <code>retex</code> }
 * will be defined after the execution of <code>CALL</code>; thus it is 
 * <b>perfectly
 * valid for <code>retval</code> and <code>retex</code> to be identical</b>.
 * Of course, for type-safety the return type cannot be primitive if this
 * is so.
 * <p>
 * The <code>Temp</code> not defined by the <code>CALL</code> 
 * (if the <code>retex</code> and <code>retval</code> <code>Temp</code>s
 *  are different) is <i>undefined</i> --- that is, it may have
 * <i>any value at all</i> after the <code>CALL</code>.  Both
 * <code>IR.LowQuad.PCALL</code> and <code>IR.Tree.CALL</code> also
 * behave this way.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.1.2.20 2001-01-11 19:47:45 cananian Exp $ 
 */
public class CALL extends SIGMA {
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
    /** Special flag for tail calls. */
    final protected boolean isTailCall;

    /** Creates a <code>CALL</code> quad representing a method invocation
     *  with explicit exception handling.
     * @param method
     *        the method to invoke.
     * @param params
     *        an array of <code>Temp</code>s containing the parameters
     *        to pass to the method.  The object on which to invoke the 
     *        method is the first element in the parameter list of a
     *        non-static method; static methods do not need to specify a
     *        receiver.  For static methods, <code>params</code>
     *        should match exactly the number and types of parameters in 
     *        the method descriptor.  For non-static methods, the receiver
     *        object (which is not included in the descriptor) is element
     *        zero of the <code>params</code> array.
     * @param retval
     *        the destination <code>Temp</code> for the method's return
     *        value, or <code>null</code> if the method returns no
     *        value (return type is <code>void</code>.
     * @param retex
     *        the destination <code>Temp</code> for any exception thrown
     *        by the called method.  If <code>null</code> then this
     *        <code>CALL</code> has arity one and handles exceptions
     *        implicitly; else it has arity two and exception handling
     *        is explicit.
     * @param isVirtual
     *        <code>true</code> if invocation semantics are that of a
     *        virtual method; <code>false</code> for constructors,
     *        private methods, and static initializers, which have
     *        non-virtual invocation semantics.
     *        Value doesn't matter for static methods; the
     *        <code>isVirtual()</code> method will always return 
     *        <code>false</code> in this case.
     * @param isTailCall
     *        <code>true</code> if this method should return the same
     *        value the callee returns or throw whatever exception the
     *        callee throws (in which case we can get rid of our stack
     *        and let the callee return directly to our caller.
     *        Usually <code>false</code>.
     * @param dst
     *        the elements of the pairs on the left-hand side of
     *        the sigma function assignment block associated with
     *        this <code>CALL</code>.
     * @param src
     *        the arguments to the sigma functions associated with
     *        this <code>CALL</code>.
     */
    public CALL(QuadFactory qf, HCodeElement source,
		HMethod method, Temp[] params, Temp retval, Temp retex,
		boolean isVirtual, boolean isTailCall,
		Temp[][] dst, Temp[] src) {
	super(qf, source, dst, src, retex==null?1:2);
	Util.assert(method!=null); // assert early, before call to isStatic()
	this.method = method;
	this.params = params;
	this.retval = retval;
	this.retex  = retex;
	// static methods are not virtual.
	this.isVirtual = isStatic()?false:isVirtual;
	this.isTailCall = isTailCall;

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
	Util.assert(method.getReturnType()==HClass.Void
		    ? retval==null : retval!=null,
		    "retval not consistent with return type.");
	Util.assert((method.getParameterTypes().length + (isStatic()?0:1)) ==
		    params.length);
	Util.assert(!isStatic() || isVirtual()==false,
		    "method can't be both static and virtual");
	// check that retval and retex are different if return val is primitive
	Util.assert(!(retval==retex && retex!=null &&
		      method.getReturnType().isPrimitive()),
		    "can't merge a primitive and a Throwable w/o violating "+
		    "type safety.");
	// I guess it's legal, then.
    }
    // convenience constructor.
    /** Creates a <code>CALL</code> with an empty <code>dst</code> array
     *  of the proper size.  Other arguments as above. */
    public CALL(QuadFactory qf, HCodeElement source,
		HMethod method, Temp[] params, Temp retval, Temp retex,
		boolean isVirtual, boolean isTailCall, Temp[] src) {
	this(qf, source, method, params, retval, retex, isVirtual, isTailCall,
	     new Temp[src.length][retex==null?1:2], src);
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
    /** Returns the type of the specified parameter. */
    public HClass paramType(int i) {
	if (isStatic()) return method.getParameterTypes()[i];
	else if (i==0) return method.getDeclaringClass();
	else return method.getParameterTypes()[i-1];
    }
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
    /** Returns <code>true</code> if this method should return the
     *  same value the callee returns or throw whatever
     *  exception the callee throws (in which case we can get
     *  rid of our stack and let the callee return directly to
     *  our caller.  Usually <code>false</code>. */
    public boolean isTailCall() { return isTailCall; }

    /** Returns all the Temps used by this Quad. 
     * @return the <code>params</code> array.
     */
    public Temp[] use() {
	Temp[] u = super.use();
	Temp[] r = new Temp[u.length+params.length];
	System.arraycopy(u,      0, r, 0,        u.length);
	System.arraycopy(params, 0, r, u.length, params.length);
	return r;
    }
    /** Returns all the Temps defined by this Quad. 
     * @return The non-null members of <code>{ retval, retex }</code>.
     */
    public Temp[] def() {
	Temp[] d = super.def();
	int len = d.length;
	if (retval!=null) len++;
	if (retex !=null) len++;
	Temp[] r = new Temp[len];
	System.arraycopy(d, 0, r, 0, d.length);
	if (retval!=null) r[--len]=retval;
	if (retex !=null) r[--len]=retex;
	return r;
    }

    public int kind() { return QuadKind.CALL; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new CALL(qqf, this, method, map(useMap, params),
			map(defMap,retval),map(defMap, retex),
			isVirtual, isTailCall,
			map(defMap, dst), map(useMap, src));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	super.renameUses(tm);
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	super.renameDefs(tm);
	if (retval!=null)
	    retval = tm.tempMap(retval);
	if (retex!=null)
	    retex  = tm.tempMap(retex);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	if (retval!=null)
	    sb.append(retval.toString() + " = ");
	sb.append("CALL ");
	if (!isVirtual)
	    sb.append("(non-virtual) ");
	if (isTailCall)
	    sb.append("[tail call] ");
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
	    sb.append(" exceptions THROWN DIRECTLY");
	sb.append(" / "); sb.append(super.toString());
	return sb.toString();
    }
    // Other information that might be useful.  Or might not.  Who knows?
    /** Determines whether this <code>CALL</code> is to an interface method. */
    public boolean isInterfaceMethod() { return method.isInterfaceMethod(); }
    /** Determines whether this <code>CALL</code> is to a static method. */
    public boolean isStatic() { return method.isStatic(); }
}
