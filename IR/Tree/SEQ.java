// SEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>SEQ</code> evaluates the left statement followed by the right
 * statement.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: SEQ.java,v 1.1.2.4 1999-02-09 21:54:23 duncan Exp $
 */
public class SEQ extends Stm {
    /** The statement to evaluate first. */
    public Stm left;
    /** The statement to evaluate last. */
    public Stm right;
    /** Constructor. */
    public SEQ(TreeFactory tf, HCodeElement source,
	       Stm left, Stm right) {
	super(tf, source);
	this.left=left; this.right=right;
	Util.assert(left!=null && right!=null);
    }
    public ExpList kids() {throw new Error("kids() not applicable to SEQ");}
    public Stm build(ExpList kids) {throw new Error("build() not applicable to SEQ");}
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new SEQ(tf, this, 
		       (Stm)left.rename(tf, ctm),
		       (Stm)right.rename(tf, ctm));
    }
}

