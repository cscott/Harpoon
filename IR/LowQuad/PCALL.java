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
 * invocation.  Interpretation is similar to that of
 * <code>harpoon.IR.Quads.CALL</code>.<p>
 * If an exception is thrown by the called method, the <code>Temp</code>
 * specified by <code>retex</code> will be assigned the non-null 
 * reference to the thrown exception, and the <code>Temp</code>
 * specified by the <code>retval</code> field will be undefined
 * (that is, it may have any value at all).
 * Execution will proceed along the second outgoing edge,
 * <code>nextEdge(1)</code>.  If no exception is thrown, the 
 * return value will be assigned to the <code>Temp</code> specified
 * by <code>retval</code> (if any), and <code>retex</code> will
 * be undefined.  Execution will proceed along the first outgoing
 * edge, <code>nextEdge(0)</code>.
 * <p>
 * See also <code>IR.Quads.CALL</code> and <code>IR.Tree.CALL</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PCALL.java,v 1.3.2.1 2002-02-27 08:36:25 cananian Exp $
 */
public class PCALL extends harpoon.IR.Quads.SIGMA {
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
    /** Whether this is a virtual or non-virtual method invocation. */
    protected boolean isVirtual;
    /** Whether this should be treated as a tail call. */
    protected boolean isTailCall;

    /** Creates a <code>PCALL</code> representing a method pointer dereference
     *  and method invocation. Interpretation is similar to that of
     *  <code>harpoon.IR.Quads.CALL</code>.<p>
     *  If an exception is thrown by the called method, the <code>Temp</code>
     *  specified by <code>retex</code> will be assigned the non-null 
     *  reference to the thrown exception, and the <code>Temp</code>
     *  specified by the <code>retval</code> field will be undefined
     *  (that is, it may have any value at all).
     *  Execution will proceed along the second outgoing edge,
     *  <code>nextEdge(1)</code>.  If no exception is thrown, the 
     *  return value will be assigned to the <code>Temp</code> specified
     *  by <code>retval</code> (if any), and <code>retex</code> will
     *  be undefined.  Execution will proceed along the first outgoing
     *  edge, <code>nextEdge(0)</code>.
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
     * @param dst
     *        the elements of the pairs on the left-hand side of
     *        the sigma function assignment block associated with
     *        this <code>PCALL</code>.
     * @param src
     *        the arguments to the sigma functions associated with
     *        this <code>PCALL</code>.
     * @param isVirtual
     *        <code>true</code> if this is a virtual method invocation,
     *        in which case <code>ptr</code> <i>points to</i> the address of
     *        the method to invoke, or <code>false</code> if this is a
     *        non-virtual invocation, in which case <code>ptr</code> is
     *        the <i>actual address</i> of the method to invoke.
     * @param isTailCall
     *        <code>true</code> if this method should return the same
     *        value the callee returns or throw whatever exception the
     *        callee throws (in which case we can get rid of our
     *        stack and let the callee return directly to our caller).
     *        Usually <code>false</code>.
     */
    public PCALL(LowQuadFactory qf, HCodeElement source,
		 Temp ptr, Temp[] params, Temp retval, Temp retex,
		 Temp[][] dst, Temp[] src,
		 boolean isVirtual, boolean isTailCall) {
	super(qf, source, dst, src, 2/* always arity two */);
	this.ptr = ptr;
	this.params = params;
	this.retval = retval;
	this.retex = retex;
	this.isVirtual = isVirtual;
	this.isTailCall = isTailCall;
	assert ptr!=null && params!=null && retex !=null;
	// hm.  can't check much else without knowing the method identity.
    }
    // convenience constructor.
    /** Creates a <code>PCALL</code> with an empty <code>dst</code> array
     *  of the proper size and arity.  Other arguments as above. */
    public PCALL(LowQuadFactory qf, HCodeElement source,
		 Temp ptr, Temp[] params, Temp retval, Temp retex,
		 Temp[] src, boolean isVirtual, boolean isTailCall){
	this(qf, source, ptr, params, retval, retex,
	     new Temp[src.length][2], src, isVirtual, isTailCall);
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
    /** Returns <code>true</code> if <code>ptr</code> <i>points to</i>
     *  the address of the method to invoke; or <code>false</code> if
     *  <code>ptr</code> contains the direct address of the method to
     *  invoke. */
    public boolean isVirtual() { return isVirtual; }
    /** Return <code>true</code> if this method should return the same
     *  value the callee returns or throw whatever exception the
     *  callee throws (in which case we can get rid of our
     *  stack and let the callee return directly to our caller).
     *  Usually <code>false</code>. */
    public boolean isTailCall() { return isTailCall; }

    public int kind() { return LowQuadKind.PCALL; }

    public Temp[] use() {
	Temp[] u = super.use();
	Temp[] r = new Temp[u.length+params.length+1];
	System.arraycopy(u,      0, r, 0,        u.length);
	System.arraycopy(params, 0, r, u.length, params.length);
	r[u.length+params.length] = ptr;
	return r;
    }
    public Temp[] def() {
	Temp[] d = super.def();
	Temp[] r = new Temp[d.length+(retval==null?1:2)];
	System.arraycopy(d, 0, r, 0, d.length);
	if (retval!=null) r[r.length-2] = retval;
	r[r.length-1] = retex;
	return r;
    }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PCALL((LowQuadFactory)qf, this,
			 map(useMap, ptr), map(useMap, params),
			 map(defMap, retval), map(defMap, retex),
			 map(defMap, dst), map(useMap, src),
			 isVirtual, isTailCall);
    }

    public void accept(harpoon.IR.Quads.QuadVisitor v) {
	((LowQuadVisitor)v).visit(this);
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	if (retval != null)
	    sb.append(retval.toString() + " = ");
	sb.append("PCALL ");
	if (isVirtual) sb.append("*");
	sb.append(ptr.toString());

	sb.append('(');
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(')');

	if (isTailCall) sb.append(" [tail call]");
	sb.append(" exceptions in "+retex.toString());
	sb.append(" / "); sb.append(super.toString());
	return sb.toString();
    }
}
