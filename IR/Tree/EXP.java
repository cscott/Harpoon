// EXP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;

/**
 * <code>EXP</code> objects evaluate an expression (for side-effects) and then
 * throw away the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: EXP.java,v 1.1.2.9 1999-08-03 21:12:57 duncan Exp $
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
	Util.assert(tf == exp.tf, "Dest and Src must have same tree factory");
    }

    public ExpList kids() {return new ExpList(exp,null);}

    public int kind() { return TreeKind.EXP; }

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	return new EXP(tf, this, kids.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new EXP(tf, this, (Exp)exp.rename(tf, ctm));
    }

    public String toString() {
        return "EXP(#" + exp.getID() + ")";
    }
}

