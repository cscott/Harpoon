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
 * @author  Duncan Bryce, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: INVOCATION.java,v 1.1.2.5 1999-07-07 09:47:24 duncan Exp $
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
	Set def = new HashSet();
	if (retval.kind()==TreeKind.TEMP) def.add(((TEMP)retval).temp);
	if (retex.kind()==TreeKind.TEMP)  def.add(((TEMP)retex).temp);
	return def;
    }

    protected Set useSet() {
	Set uses = new HashSet();
	uses.addAll(ExpList.useSet(args));
	uses.addAll(func.useSet());
	if (!(retval.kind()==TreeKind.TEMP)) uses.addAll(retval.useSet());
	if (!(retex.kind()==TreeKind.TEMP))  uses.addAll(retex.useSet());
	return uses;
    }
    
    abstract public boolean isNative();
    abstract public ExpList kids();
    abstract public Stm build(ExpList kids);
    abstract public void visit(TreeVisitor v);
    abstract public Tree rename(TreeFactory tf, CloningTempMap ctm);
}
 
