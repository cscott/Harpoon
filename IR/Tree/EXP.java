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
 * @version $Id: EXP.java,v 1.1.2.7 1999-06-28 18:49:16 duncan Exp $
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

    public int kind() { return TreeKind.EXP; }

    public Stm build(ExpList kids) {
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

