// EXP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>EXP</code> objects evaluate an expression (for side-effects) and then
 * throw away the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: EXP.java,v 1.1.2.1 1999-01-14 05:54:59 cananian Exp $
 */
public class EXP extends Stm {
    /** The expression to evaluate. */
    public Exp exp; 
    /** Constructor. */
    public EXP(Exp exp) { this.exp=exp; }
    public ExpList kids() {return new ExpList(exp,null);}
    public Stm build(ExpList kids) {
	return new EXP(kids.head);
    }
}

