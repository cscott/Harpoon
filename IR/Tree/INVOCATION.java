// INVOCATION.java, created Thu Feb 18 17:43:19 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
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
 * @version $Id: INVOCATION.java,v 1.3 2002-02-26 22:46:10 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see NATIVECALL
 */
public abstract class INVOCATION extends Stm {
    private final int argsLength;
    private final int kidsStart;
    private final boolean isVoid;

    /** Constructor.
     * @param retval Expression indicating the destination of the return
     *  value. The <code>retval</code> is <code>null</code> for 
     *  <code>void</code> functions.
     * @param func A subexpression which evaluates to the function
     *  reference to invoke.
     * @param args Subexpressions for the arguments to the function.
     */
    protected INVOCATION(TreeFactory tf, HCodeElement source,
			 TEMP retval, Exp func, ExpList args, int addlArgs) {
	super(tf, source, 1+ExpList.size(args)+(retval==null?0:1)+addlArgs); 
	Util.ASSERT(func!=null);
	this.isVoid = (retval==null);
	this.kidsStart = (isVoid?0:1)+addlArgs;
	this.argsLength = ExpList.size(args);
	setRetval(retval); setFunc(func); setArgs(args);
	Util.ASSERT(tf==func.tf, "This and Func must have same tree factory");
	Util.ASSERT(retval==null || tf == retval.tf,
		    "This and Retval must have same tree factory");
    }

    /** Returns the expression indicating the destination of the return value.
     *  Returns <code>null</code> for <code>void</code>
     *  functions. */
    public TEMP getRetval() {
	return (isVoid) ? null : (TEMP) getChild(kidsStart-1);
    }
    /** Returns an expression for the function reference to invoke.*/
    public Exp getFunc() { return (Exp) getChild(kidsStart); }
    /** Returns a list of subexpressions for the function arguments. */
    public ExpList getArgs() { return kids().tail; } 

    // kids start with func expression and go from there.
    public ExpList kids() { return _kids(getFunc()); }
    private ExpList _kids(Exp e) {
	if (e==null) return null;
	else return new ExpList(e, _kids((Exp)e.getSibling()));
    }

    /** Sets the expression indicating the destination of the return value.
     * @param retval <code>null</code> for <code>void</code> functions,
     *        non-<code>null</code> otherwise.
     */
    public void setRetval(TEMP retval) {
	if (isVoid) {
	    Util.ASSERT(retval==null, "Can't make a void function non-void");
	} else {
	    Util.ASSERT(retval!=null, "Can't make a non-void function void");
	    setChild(kidsStart-1, retval);
	}
    }
    /** Sets the function reference expression. */
    public void setFunc(Exp func) { setChild(kidsStart, func); }
    /** Sets the function argument list. */
    public void setArgs(ExpList args) {
	Util.ASSERT(argsLength==ExpList.size(args),
		    "Can't change the number of arguments to the function");
	int i=kidsStart+1;
	for (ExpList ep = args; ep!=null; ep=ep.tail)
	    setChild(i++, ep.head);
    }

    abstract public boolean isNative();
    abstract public void accept(TreeVisitor v);
}
 
