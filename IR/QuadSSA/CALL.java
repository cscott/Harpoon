// CALL.java, created Wed Aug  5 06:48:50 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>CALL</code> objects represent method invocations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CALL.java,v 1.3 1998-08-08 00:43:22 cananian Exp $
 */

public class CALL extends Quad {
    /** The method to invoke. */
    public HMethod method;
    /** Parameters to pass to the method. */
    public Temp[] params;
    /** Destination for the method's return value. */
    public Temp retval;
    /** Creates a <code>CALL</code>. <code>params</code> should match
     *  exactly the number of parameters in the method descriptor,
     *  and <code>retval</code> should be <code>null</code> if the
     *  method returns no value. */
    public CALL(String sourcefile, int linenumber,
		HMethod method, Temp[] params, Temp retval) {
	super(sourcefile, linenumber);
	this.method = method;
	this.params = params;
	this.retval = retval;
	// check params and retval here against method.
	if ((method.getReturnType()==HClass.Void &&
	     retval!=null) ||
	    (method.getReturnType()!=HClass.Void &&
	     retval==null))
	    throw new Error("Return value doesn't match descriptor.");
	HClass[] pt = method.getParameterTypes();
	if (pt.length != params.length)
	    throw new Error("Parameters do not match method descriptor.");
	// I guess it's legal, then.
    }
    /** Create a <Code>CALL</code> to a method with a <code>void</code>
     *  return-value descriptor. */
    public CALL(String sourcefile, int linenumber,
		HMethod method, Temp[] params) {
	this(sourcefile, linenumber, method, params, null);
    }
}
