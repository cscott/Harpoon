// ContBuilder.java, created Wed Nov  3 20:06:51 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

import harpoon.Analysis.EnvBuilder.EnvBuilder;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.CALL;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>ContBuilder</code> builds continuations for a given <code>CALL</code>
 * using the <code>CALL</code>'s <code>HCode</code>, storing the
 * <code>Code</code> for the continuation in the 
 * <code>CachingCodeFactory</code>. The generated <code>Code</code> is in
 * <code>quad-no-ssa</code> form.
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: ContBuilder.java,v 1.1.2.9 2001-06-18 20:45:48 cananian Exp $
 */
public class ContBuilder {
    protected final CachingCodeFactory ucf;
    protected final HCode hc;
    protected final CALL c;
    protected final HClass env;
    protected final Temp[] live;
   
    /** Creates a <code>ContBuilder</code> for the designated 
     *  <code>CALL</code>, given the <code>HCode</code> from which it came.
     *  Requires that the <code>HCode</code> be <code>quad-no-ssa</code>.
     */
    public ContBuilder(CachingCodeFactory ucf, HCode hc, CALL c, HClass env,
		       Temp[] live) {
        this.ucf = ucf;
	this.hc = hc;
	this.c = c;
	this.env = env;
	this.live = live;
    }
    
    /** Builds the continuation. Returns the <code>HClass</code>.
     *  @return the continuation <code>HClass</code>
     */

    // Given a type, returns the prefix for
    // a continuation of that type.
    public static String getPrefix(HClass t) {
	if (t == HClass.Boolean) {
	    return "Boolean";
	} else if (t == HClass.Byte) {
	    return "Byte";
	} else if (t == HClass.Char) {
	    return "Char";
	} else if (t == HClass.Double) {
	    return "Double";
	} else if (t == HClass.Float) {
	    return "Float";
	} else if (t == HClass.Int) {
	    return "Int";
	} else if (t == HClass.Long) {
	    return "Long";
	} else if (t == HClass.Short) {
	    return "Short";
	} else if (t == HClass.Void) {
	    return "Void";
	} else {
	    return "Object";
	}
    }

    private void reportException(Throwable t, String methodname, String msg) {
	System.err.println("Caught exception " + t.toString() + 
			   " in harpoon.Analysis.ContBuilder.ContBuilder." +
			   methodname);
	System.err.println(msg);
    }
}
