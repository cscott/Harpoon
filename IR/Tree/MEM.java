// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
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
 * @version $Id: MEM.java,v 1.1.2.6 1999-02-05 12:02:45 cananian Exp $
 */
public class MEM extends Exp implements Typed {
    /** A subexpression evaluating to a memory reference. */
    public final Exp exp;
    /** The type of this memory reference expression. */
    public final int type;
    /** Constructor. */
    public MEM(TreeFactory tf, HCodeElement source,
	       int type, Exp exp) {
	super(tf, source);
	this.type=type; this.exp=exp;
	Util.assert(Type.isValid(type) && exp!=null);
    }
    public ExpList kids() {return new ExpList(exp,null);}
    public Exp build(ExpList kids) {
	return new MEM(tf, this, type, kids.head);
    }

    // Typed interface:
    public int type() { return type; }
    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, type); }
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint() { return Type.isFloatingPoint(type); }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

