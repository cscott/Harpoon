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
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: INVOCATION.java,v 1.1.2.12 1999-10-23 05:59:34 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see NATIVECALL
 */
public abstract class INVOCATION extends Stm {
    /** A subexpression which evaluates to the function reference to invoke.*/
    public Exp func;
    /** Subexpressions for the arguments to the function. */
    public ExpList args;
    /** Expression indicating the destination of the return value.
     *  The <code>retval</code> is <code>null</code> for <code>void</code>
     *  functions. */
    public TEMP retval;


    /** Constructor. */
    protected INVOCATION(TreeFactory tf, HCodeElement source,
			 TEMP retval, Exp func, ExpList args) {
	this(tf, source, retval, func, args, 1);
    }

    protected INVOCATION(TreeFactory tf, HCodeElement source,
			 TEMP retval, Exp func, ExpList args, int next_arity) {
	super(tf, source, next_arity);
	this.retval=retval; this.func=func; this.args=args;
	Util.assert(func!=null);
	Util.assert(tf==func.tf, "This and Func must have same tree factory");
	Util.assert(retval==null || tf == retval.tf,
		    "This and Retval must have same tree factory");
    }

    protected Set defSet() { 
	Set def = new HashSet();
	if (retval.kind()==TreeKind.TEMP) def.add(((TEMP)retval).temp);
	return def;
    }

    protected Set useSet() {
	Set uses = new HashSet();
	uses.addAll(ExpList.useSet(args));
	uses.addAll(func.useSet());
	if (!(retval.kind()==TreeKind.TEMP)) uses.addAll(retval.useSet());
 	return uses;
    }
    
    abstract public boolean isNative();
    abstract public ExpList kids();
    abstract public Stm build(ExpList kids);
    abstract public void accept(TreeVisitor v);
    abstract public Tree rename(TreeFactory tf, CloningTempMap ctm);
}
 
