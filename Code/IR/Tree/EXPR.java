// EXPR.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>EXPR</code> objects evaluate an expression (for side-effects) and then
 * throw away the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: EXPR.java,v 1.4 2002-04-10 03:05:45 cananian Exp $
 */
public class EXPR extends Stm {
    /** Constructor. */
    public EXPR(TreeFactory tf, HCodeElement source, 
	       Exp exp) {
	super(tf, source, 1);
	assert exp!=null;
	this.setExp(exp);
	assert tf == exp.tf : "Dest and Src must have same tree factory";
	
	// FSK: debugging hack
	// this.accept(TreeVerifyingVisitor.norepeats());
    }

    /** Returns the expression to evaluate. */
    public Exp getExp() { return (Exp) getChild(0); }

    /** Sets the expression to evaluate. */
    public void setExp(Exp exp) { setChild(0, exp); }

    public int kind() { return TreeKind.EXPR; }

    public Stm build(TreeFactory tf, ExpList kids) {
	assert kids!=null && kids.tail==null;
	assert tf == kids.head.tf;
	return new EXPR(tf, this, kids.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new EXPR(tf, this, (Exp)getExp().rename(tf, tm, cb)), tm);
    }

    public String toString() {
        return "EXPR(#" + getExp().getID() + ")";
    }
}

