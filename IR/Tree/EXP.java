// EXP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>EXP</code> objects evaluate an expression (for side-effects) and then
 * throw away the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: EXP.java,v 1.1.2.3 1999-02-05 11:48:46 cananian Exp $
 */
public class EXP extends Stm {
    /** The expression to evaluate. */
    public Exp exp; 
    /** Constructor. */
    public EXP(TreeFactory tf, HCodeElement source,
	       Exp exp) {
	super(tf, source);
	this.exp=exp;
	Util.assert(exp!=null);
    }
    public ExpList kids() {return new ExpList(exp,null);}
    public Stm build(ExpList kids) {
	return new EXP(tf, this, kids.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

