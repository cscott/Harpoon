// JUMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Label;
import harpoon.Temp.LabelList;

/**
 * <code>JUMP</code> objects are statements which stand for unconditional
 * computed branches.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: JUMP.java,v 1.1.2.2 1999-01-15 17:56:40 duncan Exp $
 */
public class JUMP extends Stm {
    /** An expression giving the address to jump to. */
    public Exp exp;
    /** A list of possible branch targets. */
    public /*final*/ LabelList targets;
    /** Full constructor. */
    public JUMP(Exp exp, LabelList targets)
    { this.exp=exp; this.targets=targets; }
    /** Abbreviated constructor for a non-computed branch. */
    public JUMP(Label target) {
	this(new NAME(target), new LabelList(target,null));
    }
    public ExpList kids() { return new ExpList(exp,null); }
    public Stm build(ExpList kids) {
	return new JUMP(kids.head,targets);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

