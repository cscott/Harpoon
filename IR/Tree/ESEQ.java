// ESEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>ESEQ</code> objects are expressions which chain a statement and
 * an expressions together.  The statement is evaluated for side effects, the
 * the expression is evaluated.  The value of the expression is the value of
 * the <code>ESEQ</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ESEQ.java,v 1.1.2.10 1999-07-30 20:20:15 pnkfelix Exp $
 */
public class ESEQ extends Exp {
    /** The statement to evaluate for side-effects. */
    public Stm stm;
    /** The expression whose value is the the value of the <code>ESEQ</code>.*/
    public Exp exp;
    /** Constructor. */
    public ESEQ(TreeFactory tf, HCodeElement source,
		Stm stm, Exp exp) {
	super(tf, source);
	this.stm=stm; this.exp=exp;
	Util.assert(stm!=null && exp!=null);
	Util.assert(tf == exp.tf, "Dest and Src must have same tree factory");
    }

    protected Set defSet() { 
	throw new Error
	    ("DEF and USE can only be extracted from canonical trees");
    }

    protected Set useSet() { 
	throw new Error
	    ("DEF and USE can only be extracted from canonical trees");
    }

    public ExpList kids() {throw new Error("kids() not applicable to ESEQ");}

    public int kind() { return TreeKind.ESEQ; }

    public Exp build(ExpList kids) {throw new Error("build() not applicable to ESEQ");}
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new ESEQ(tf, this, 
			(Stm)stm.rename(tf, ctm), 
			(Exp)exp.rename(tf, ctm));
    }

    public int type() { return exp.type(); }

    public String toString() {
        return "ESEQ(#" + stm.getID() + ", #" + exp.getID() + ")";
    }
}

