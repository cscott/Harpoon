// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Util.Util;

/**
 * <code>MEM</code> objects are expressions which stand for the contents of
 * a value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEM</code> is used as the left child
 * of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEM.java,v 1.1.2.4 1999-02-05 10:40:42 cananian Exp $
 */
public class MEM extends Exp implements Typed {
    /** A subexpression evaluating to a memory reference. */
    public final Exp exp;
    /** The type of this memory reference expression. */
    public final int type;
    /** Constructor. */
    public MEM(int type, Exp exp) {
	this.type=type; this.exp=exp;
	Util.assert(type==INT||type==FLOAT||type==LONG||type==DOUBLE||
		    type==POINTER);
    }
    public ExpList kids() {return new ExpList(exp,null);}
    public Exp build(ExpList kids) {
	return new MEM(type, kids.head);
    }

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

