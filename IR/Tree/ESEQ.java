// ESEQ.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>ESEQ</code> objects are expressions which chain a statement and
 * an expression together.  The statement is evaluated for side effects, then
 * the expression is evaluated.  The value of the expression is the value of
 * the <code>ESEQ</code>.<p>
 * <code>ESEQ</code>s are <code>PreciselyTyped</code> because the enclosed
 * <code>Exp</code> may be <code>PreciselyTyped</code>.  In contexts such
 * as <code>MOVE(ESEQ(stm, MEM&lt;small&gt;(e1)), e2)</code>, the
 * <code>ESEQ</code> clearly has the same small type as the <code>MEM</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ESEQ.java,v 1.2 2002-02-25 21:05:31 cananian Exp $
 */
public class ESEQ extends Exp implements PreciselyTyped {
    /** Constructor. */
    public ESEQ(TreeFactory tf, HCodeElement source,
		Stm stm, Exp exp) {
	super(tf, source, 2);
	Util.assert(stm!=null && exp!=null);
	Util.assert(tf == exp.tf);
	Util.assert(tf == stm.tf); 
	setStm(stm); setExp(exp);
    }

    /** Returns the statement to evaluate for side-effects. */
    public Stm getStm() { return (Stm) getChild(0); } 
    /** Returns the expression whose value is the the value of
     *  the <code>ESEQ</code>.*/
    public Exp getExp() { return (Exp) getChild(1); } 

    /** Sets the statement to evaluate for side-effects. */
    public void setStm(Stm stm) { setChild(0, stm); }
    /** Sets the expression whose value is the the value of
     *  the <code>ESEQ</code>.*/
    public void setExp(Exp exp) { setChild(1, exp); }

    public ExpList kids() {throw new Error("kids() not applicable to ESEQ");}

    public int kind() { return TreeKind.ESEQ; }

    public Exp build(TreeFactory tf, ExpList kids) {throw new Error("build() not applicable to ESEQ");}

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this, new ESEQ(tf, this, 
					  (Stm)getStm().rename(tf, tm, cb), 
					  (Exp)getExp().rename(tf, tm, cb)),
			   tm);
    }

    public int type() { return getExp().type(); }
    public boolean isSmall() { 
	return (getExp() instanceof PreciselyTyped)
	    ? ((PreciselyTyped)getExp()).isSmall()
	    : false;
    }
    public int bitwidth() { return ((PreciselyTyped)getExp()).bitwidth(); }
    public boolean signed() { return ((PreciselyTyped)getExp()).signed(); }

    public String toString() {
        return "ESEQ(#" + getStm().getID() + ", #" + getExp().getID() + ")";
    }
}

