// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CALL</code> objects are expressions which stand for procedure calls.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CALL.java,v 1.1.2.1 1999-01-14 05:54:58 cananian Exp $
 */
public class CALL extends Exp {
    /** A subexpression which evaluates to the function reference to invoke.*/
    public Exp func;
    /** Subexpressions for the arguments to the function. */
    public ExpList args;
    /** Constructor. */
    public CALL(Exp func, ExpList args) { this.func=func; this.args=args;}
    public ExpList kids() {return new ExpList(func,args);}
    public Exp build(ExpList kids) {
	return new CALL(kids.head,kids.tail);
    }
}

