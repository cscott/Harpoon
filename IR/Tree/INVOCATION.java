// INVOCATION.java, created Thu Feb 18 17:43:19 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;


/**
 * <code>INVOCATION</code> objects are statements which stand for 
 * procedure calls.  The return value of the <code>CALL</code> is 
 * stored in <code>retval</code>.  If the call throws an exception, 
 * the exception object will be placed in <code>retex</code>.
 * The <code>retval</code> field will be <code>null</code> if the
 * called procedure has void return type.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: INVOCATION.java,v 1.1.2.15 2000-01-09 00:21:56 duncan Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see NATIVECALL
 */
public abstract class INVOCATION extends Stm {
    /** A subexpression which evaluates to the function reference to invoke.*/
    private Exp func;
    /** Subexpressions for the arguments to the function. */
    private ExpList args;
    /** Expression indicating the destination of the return value.
     *  The <code>retval</code> is <code>null</code> for <code>void</code>
     *  functions. */
    private TEMP retval;

    /** Constructor. */
    protected INVOCATION(TreeFactory tf, HCodeElement source,
			 TEMP retval, Exp func, ExpList args) {
	super(tf, source); 
	this.args = args; this.func = func; this.retval = retval; 
	Util.assert(func!=null);
	Util.assert(tf==func.tf, "This and Func must have same tree factory");
	Util.assert(retval==null || tf == retval.tf,
		    "This and Retval must have same tree factory");
    }

    public TEMP getRetval() { return this.retval; }
    public Exp getFunc() { return this.func; }
    public ExpList getArgs() { return this.args; } 

    protected Set defSet() { 
	Set def = new HashSet();
	if (retval != null && retval.kind() == TreeKind.TEMP) 
	    { def.add(((TEMP)retval).temp); }
	return def;
    }

    protected Set useSet() {
	Set uses = new HashSet();
	uses.addAll(ExpList.useSet(args));
	uses.addAll(func.useSet());
	if (retval != null && !(retval.kind()==TreeKind.TEMP))
	    { uses.addAll(retval.useSet()); } 
 	return uses;
    }
    
    abstract public boolean isNative();
    abstract public Stm build(ExpList kids);
    abstract public void accept(TreeVisitor v);
    abstract public Tree rename(TreeFactory tf, CloningTempMap ctm);
    abstract public Tree getFirstChild();

    public void setArgs(ExpList args) { this.args = args; }
    public void setFunc(Exp func) { this.func = func; }
    public void setRetval(TEMP retval) { this.retval = retval; }
}
 
