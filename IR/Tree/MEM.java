// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
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
 * @version $Id: MEM.java,v 1.1.2.12 1999-08-03 21:12:57 duncan Exp $
 */
public class MEM extends Exp {
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
	Util.assert(tf == exp.tf, "This and Exp must have same tree factory");
    }
    public ExpList kids() {return new ExpList(exp,null);}

    public int kind() { return TreeKind.MEM; }

    public Exp build(ExpList kids) { return build(tf, kids); } 

    public Exp build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	return new MEM(tf, this, type, kids.head);
    }

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new MEM(tf, this, type, (Exp)exp.rename(tf, ctm));
    }

    public String toString() {
        return "MEM<" + Type.toString(type) + ">(#" + exp.getID() + ")";
    }
}

