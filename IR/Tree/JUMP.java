// JUMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.Util;

/**
 * <code>JUMP</code> objects are statements which stand for unconditional
 * computed branches.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: JUMP.java,v 1.1.2.4 1999-02-09 21:54:23 duncan Exp $
 */
public class JUMP extends Stm {
    /** An expression giving the address to jump to. */
    public Exp exp;
    /** A list of possible branch targets. */
    public LabelList targets;
    /** Full constructor. */
    public JUMP(TreeFactory tf, HCodeElement source,
		Exp exp, LabelList targets) {
	super(tf, source);
	this.exp=exp; this.targets=targets;
	Util.assert(exp!=null && targets!=null);
    }
    /** Abbreviated constructor for a non-computed branch. */
    public JUMP(TreeFactory tf, HCodeElement source,
		Label target) {
	this(tf, source,
	     new NAME(tf, source, target), new LabelList(target,null));
    }
    public ExpList kids() { return new ExpList(exp,null); }
    public Stm build(ExpList kids) {
	return new JUMP(tf, this, kids.head,targets);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new JUMP(tf, this, (Exp)exp.rename(tf, ctm), this.targets);
    }  
}

