// Translation.java, created Wed Sep 29 18:37:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>Translation</code> is an empty class wrapper for various
 * special context-sensitive <code>Tree.Exp</code> wrappers.
 * Boolean expressions are often best compiled different ways
 * depending on whether they are used in a direct assignment or
 * as the argument to a conditional branch. 
 * <code>Translation.Exp</code> is a closure object that allows
 * this sort of conditional context-sensitive expression resolution.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translation.java,v 1.4 2002-04-10 03:05:46 cananian Exp $
 */
public abstract class Translation {
    /** The <code>Translation.Exp</code> class represents an expression
     *  that might be used in several different ways: as a value,
     *  as a branch condition, or as code to be executed for side-effects
     *  only.
     *
     *  For each instance of this class, only one of these methods
     *  should be called, and it should only be called once.
     */
    public static abstract class Exp {
	private boolean once=false;
	public final harpoon.IR.Tree.Exp unEx(TreeFactory tf) {
	    assert !once; once=true;
	    return unExImpl(tf);
	}
	protected abstract harpoon.IR.Tree.Exp unExImpl(TreeFactory tf);
	
	public final harpoon.IR.Tree.Stm unNx(TreeFactory tf) {
	    assert !once; once=true;
	    return unNxImpl(tf);
	}
	protected abstract harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf);
	public final harpoon.IR.Tree.Stm unCx(TreeFactory tf,
					      Label iftrue, 
					      Label iffalse) {
	    assert !once; once=true;
	    return unCxImpl(tf, iftrue, iffalse);
	}
	protected abstract harpoon.IR.Tree.Stm unCxImpl(TreeFactory tf,
							Label iftrue, 
							Label iffalse);
    }
    /** The <code>Translation.Ex</code> class is a <code>Translation.Exp</code>
     *  representing a value expression.  It can be evaluated as an
     *  expression, a conditional, or a side-effects-only statement. */
    public static class Ex extends Exp {
	final harpoon.IR.Tree.Exp exp;
	public Ex(harpoon.IR.Tree.Exp exp) { this.exp = exp; }
	protected harpoon.IR.Tree.Exp unExImpl(TreeFactory tf) { return exp; }
	protected harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf) {
	    return new EXPR(tf, exp, exp);
	}
	protected harpoon.IR.Tree.Stm unCxImpl(TreeFactory tf,
					Label iftrue, Label iffalse) {
	    // special cases for CONST 0 and CONST 1 are done in
	    // a separate subclass of Ex (ExCONST, in ToTree)
	    return new CJUMP(tf, exp, exp, iftrue, iffalse);
	}
    }
    /** The <code>Translation.Nx</code> class is a <code>Translation.Exp</code>
     *  representing a statement.  It can only be evaluated for side-effects;
     *  any attempt to evaluate it as a value expression or conditional
     *  will throw an <code>Error</code>. */
    public static class Nx extends Exp {
	final harpoon.IR.Tree.Stm stm;
	public Nx(harpoon.IR.Tree.Stm stm) { this.stm = stm; }
	protected harpoon.IR.Tree.Exp unExImpl(TreeFactory tf) {
	    throw new Error("Nx cannot be converted to Ex");
	}
	protected harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf) { return stm; }
	protected harpoon.IR.Tree.Stm unCxImpl(TreeFactory tf,
					Label iftrue, Label iffalse) {
	    throw new Error("Nx cannot be converted to Cx");
	}
    }
    /** The <code>Translation.Cx</code> abstract class specifies how to
     *  evaluate a conditional as an expression or as a side-effects-only
     *  statement. */
    public static abstract class Cx extends Exp {
	protected harpoon.IR.Tree.Exp unExImpl(TreeFactory tf) {
	    Temp  Tr = new Temp(tf.tempFactory(), "cx");
	    Label Lt = new Label();
	    Label Lf = new Label();
	    Stm s = unCxImpl(tf, Lt, Lf);
	    return new ESEQ
		(tf, s,
		 new SEQ
		 (tf, s,
		  new MOVE(tf, s,
			   new TEMP(tf, s, Type.INT, Tr),
			   new CONST(tf, s, 1)),
		  new SEQ
		  (tf, s,
		   s,
		   new SEQ
		   (tf, s,
		    new LABEL(tf, s, Lf, false),
		    new SEQ
		    (tf, s,
		     new MOVE(tf, s,
			      new TEMP(tf, s, Type.INT, Tr),
			      new CONST(tf, s, 0)),
		     new LABEL(tf, s, Lt, false))))),
		 new TEMP(tf, s, Type.INT, Tr));
	}
	protected harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf) {
	    Label l = new Label();
	    Stm s = unCxImpl(tf, l, l);
	    return new SEQ(tf, s, s, new LABEL(tf, s, l, false));
	}
    }
}
