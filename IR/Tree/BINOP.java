// BINOP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>BINOP</code> objects are expressions which stand for result of
 * applying some binary operator <i>o</i> to a pair of subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: BINOP.java,v 1.1.2.7 1999-02-24 01:18:53 andyb Exp $
 * @see Bop
 */
public class BINOP extends OPER {
    /** The subexpression of the left-hand side of the operator. */
    public Exp left;
    /** The subexpression of the right-hand side of the operator. */
    public Exp right;
    /** Constructor. */
    public BINOP(TreeFactory tf, HCodeElement source,
		 int optype, int binop, Exp left, Exp right) {
	super(tf, source, optype, binop);
	this.left=left; this.right=right;
	Util.assert(Bop.isValid(binop));
    }
    // binops defined in harpoon.IR.Tree.Bop.
    public int type() {
	switch(op) {
	case Bop.CMPLT: case Bop.CMPLE: case Bop.CMPEQ:
	case Bop.CMPGE: case Bop.CMPGT:
	    return INT; // boolean comparison result
	default:
	    return optype;
	}
    }

    public ExpList kids() {return new ExpList(left, new ExpList(right,null));}
    public Exp build(ExpList kids) {
	return new BINOP(tf, this, optype, op, kids.head, kids.tail.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
  
    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new BINOP(tf, this, optype, op, 
			 (Exp)left.rename(tf, ctm), 
			 (Exp)right.rename(tf, ctm));
    }

    public String toString() {
        return "BINOP<"+Type.toString(optype)+">("+Bop.toString(op)+
               ", #" + left.getID() + ", #" + right.getID() + ")";
    }
}

