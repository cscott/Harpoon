// ESEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>ESEQ</code> objects are expressions which chain a statement and
 * an expressions together.  The statement is evaluated for side effects, the
 * the expression is evaluated.  The value of the expression is the value of
 * the <code>ESEQ</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ESEQ.java,v 1.1.2.1 1999-01-14 05:54:58 cananian Exp $
 */
public class ESEQ extends Exp {
    /** The statement to evaluate for side-effects. */
    public Stm stm;
    /** The expression whose value is the the value of the <code>ESEQ</code>.*/
    public Exp exp;
    /** Constructor. */
    public ESEQ(Stm stm, Exp exp) { this.stm=stm; this.exp=exp; }
    public ExpList kids() {throw new Error("kids() not applicable to ESEQ");}
    public Exp build(ExpList kids) {throw new Error("build() not applicable to ESEQ");}
}

