// CALL.java, created Wed Aug  5 06:48:50 1998
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>CALL</code> objects represent method invocations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.20 1998-09-24 23:14:08 cananian Exp $
 */

public class CALL extends Quad {
    /** The object in which to invoke the method. <p>
     *  <code>null</code> for static methods.  */
    public Temp objectref;
    /** The method to invoke. */
    public HMethod method;
    /** Parameters to pass to the method. */
    public Temp[] params;
    /** Destination for the method's return value. */
    public Temp retval;
    /** Destination for any exception thrown by the method. */
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
