// CALL.java, created Wed Aug  5 06:48:50 1998
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>CALL</code> objects represent method invocations.<p>
 * The <code>objectref</code> field will be <code>null</code> for
 * static methods; the <code>retval</code> field will be <code>null</code>
 * for <code>void</code> methods.<p>
 *
 * <strong>It is a semantic error for the <code>objectref</code> Temp
 * of a non-static method <code>CALL</code> to be able to have the value
 * <code>null</code> at run-time.</strong> A separate null-pointer
 * test should always precede the <code>CALL</code> quad if
 * <code>objectref</code> may be null at run-time.  Standard java
 * invocation throws a <code>NullPointerException</code> if the object
 * reference is <code>null</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.23 1998-10-10 03:27:24 cananian Exp $ 
 */

public class CALL extends Quad {
    /** The object in which to invoke the method;
     *  <code>null</code> for static methods.  */
    public Temp objectref;
    /** The method to invoke. */
    public HMethod method;
    /** Parameters to pass to the method. */
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
     *  exactly the number of parameters in the method descriptor,
     *  and <code>retval</code> should be <code>null</code> if the
     *  method returns no value. <code>objectref</code> should be
     *  <code>null</code> if the method is static. <code>retex</code>
     *  will always be a valid <code>Temp</code>.  If an exception is
     *  thrown by the called method, <code>retex</code> will be assigned
     *  a non-null value and <code>retval</code> will be null.  If
     *  no exception is thrown, <code>retex</code> will be null. */
    public CALL(HCodeElement source,
		HMethod method, Temp objectref, Temp[] params, 
		Temp retval, Temp retex, boolean isSpecial) {
	super(source);
	this.method = method;
	this.objectref = objectref;
	this.params = params;
	this.retval = retval;
	this.retex  = retex;
	this.isSpecial = isSpecial;
	// check static methods.
	if (objectref==null) Util.assert(isStatic());
	else Util.assert(!isStatic());
	// check params and retval here against method.
	if (method.getReturnType()==HClass.Void) Util.assert(retval==null);
	else Util.assert(retval!=null);
	Util.assert(method.getParameterTypes().length == params.length);
	// check miscellanea.
	Util.assert(retex!=null && params!=null && method!=null);
	// I guess it's legal, then.
    }

    /** Returns all the Temps used by this Quad. 
     * @return objectref (if objectref!=null) and params.
     */
    public Temp[] use() {
	if (objectref==null)
	    return (Temp[]) Util.copy(params);
	else {
	    Temp[] u = new Temp[params.length+1];
	    System.arraycopy(params,0,u,1,params.length);
	    u[0] = objectref;
	    return u;
	}
    }
    /** Returns all the Temps defined by this Quad. 
     * @return retval, if retval!=null; else a zero-length array.
     */
    public Temp[] def() {
	if (retval==null) return new Temp[] { retex };
	else return new Temp[] { retval, retex };
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	if (objectref!=null)
	    objectref = tm.tempMap(objectref);
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	if (retval!=null)
	    retval = tm.tempMap(retval);
	retex  = tm.tempMap(retex);
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
	if (objectref!=null)
	    sb.append(" of "+objectref);
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
