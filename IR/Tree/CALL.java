// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>CALL</code> objects are statements which stand for procedure calls.
 * The return value of the <code>CALL</code> is stored in <code>retval</code>.
 * If the call throws an exception, the exception object will be placed in
 * <code>retex</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CALL.java,v 1.1.2.5 1999-02-09 21:54:22 duncan Exp $
 * @see harpoon.IR.Quads.CALL
 */
public class CALL extends Stm {
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
    public CALL(TreeFactory tf, HCodeElement source,
		Exp retval, Exp retex, Exp func, ExpList args) {
	super(tf, source);
	this.retval=retval; this.retex=retex; this.func=func; this.args=args;
	Util.assert(retval!=null && retex!=null && func!=null);
    }
    public ExpList kids()
    {return new ExpList(retval, new ExpList(retex, new ExpList(func, args))); }
    public Stm build(ExpList kids) {
	return new CALL(tf, this,
			kids.head, // retval
			kids.tail.head, // retex
			kids.tail.tail.head, // func
			kids.tail.tail.tail); // args
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CALL(tf, this, 
			(Exp)retval.rename(tf, ctm),
			(Exp)retex.rename(tf, ctm), 
			(Exp)func.rename(tf, ctm),
			args.rename(tf, ctm));
  }
    
}



