// OPER.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>OPER</code> objects are expressions which stand for the result
 * of applying some operator to subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: OPER.java,v 1.1.2.1 1999-01-14 05:55:00 cananian Exp $
 */
public abstract class OPER extends Exp {
    /** An enumerated type encoding the operator. */
    public final int op;

    public OPER(int op) { this.op = op; }
}
