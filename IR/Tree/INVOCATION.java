package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;

import java.util.Enumeration;

/**
 * <code>INVOCATION</code> objects are statements which stand for 
 * procedure calls.  The return value of the <code>CALL</code> is 
 * stored in <code>retval</code>.  If the call throws an exception, 
 * the exception object will be placed in <code>retex</code>.
 * 
 * @author  Duncan Bryce, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: INVOCATION.java,v 1.1.2.2 1999-04-05 21:50:44 duncan Exp $
 * @see harpoon.IR.Quads.CALL, CALL, NATIVECALL
 */
public abstract class INVOCATION extends Stm {
    /** A subexpression which evaluates to the function reference to invoke.*/
    public Exp func;
    /** Subexpressions for the arguments to the function. */
    public ExpList args;
    /** Expression indicating the destination of the return value.
     *  Always non-null, even for <code>void</code> functions. */
    public Exp retval;
    /** Expression indicating the destination of the exception value. */
    public Exp retex;


    /** Constructor. */
    protected INVOCATION(TreeFactory tf, HCodeElement source,
			 Exp retval, Exp retex, Exp func, ExpList args) {
	super(tf, source);
	this.retval=retval; this.retex=retex; this.func=func; this.args=args;
	Util.assert(retval!=null && retex!=null && func!=null);
    }

    protected Set defSet() { 
	HashSet def = new HashSet();
	if (retval instanceof TEMP) { 
	    def.union(((TEMP)retval).temp);
	}
	if (retex instanceof TEMP) { 
	    def.union(((TEMP)retex).temp);
	}
	return def;
    }

    protected Set useSet() {
	HashSet use = new HashSet();
	Set argsUse = ExpList.useSet(args);
	for (Enumeration e = argsUse.elements(); e.hasMoreElements();) { 
	    use.union(e.nextElement());
	}
	Set funcUse = func.useSet();
	for (Enumeration e = funcUse.elements(); e.hasMoreElements();) { 
	    use.union(e.nextElement());
	}

	if (!(retval instanceof TEMP)) { 
	    Set retvalUse = retval.useSet();
	    for (Enumeration e = retvalUse.elements(); e.hasMoreElements();) {
		use.union(e.nextElement());
	    }
	}
	if (!(retex instanceof TEMP)) { 
	    Set retexUse = retex.useSet();
	    for (Enumeration e = retexUse.elements(); e.hasMoreElements();) { 
		use.union(e.nextElement());
	    }
	}
	return use;
    }
    
    abstract public boolean isNative();
    abstract public ExpList kids();
    abstract public Stm build(ExpList kids);
    abstract public void visit(TreeVisitor v);
    abstract public Tree rename(TreeFactory tf, CloningTempMap ctm);
}
 


