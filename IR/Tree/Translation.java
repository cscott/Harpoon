// Translation.java, created Wed Sep 29 18:37:28 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;
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
 * @version $Id: Translation.java,v 1.1.4.2 2000-01-11 00:44:23 pnkfelix Exp $
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
	public final harpoon.IR.Tree.Exp unEx(TreeFactory tf) {
	    return unExImpl(tf);
	}
	protected abstract harpoon.IR.Tree.Exp unExImpl(TreeFactory tf);
	
	public final harpoon.IR.Tree.Stm unNx(TreeFactory tf) {
	    return unNxImpl(tf);
	}
	protected abstract harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf);
	public final harpoon.IR.Tree.Stm unCx(TreeFactory tf,
					      Label iftrue, 
					      Label iffalse) {
	    return unCxImpl(tf, iftrue, iffalse);
	}
	protected abstract harpoon.IR.Tree.Stm unCxImpl(TreeFactory tf,
							Label iftrue, 
							Label iffalse);
    }
    public static class Ex extends Exp {
	final harpoon.IR.Tree.Exp exp;
	public Ex(harpoon.IR.Tree.Exp exp) { this.exp = exp; }
	protected harpoon.IR.Tree.Exp unExImpl(TreeFactory tf) { return exp; }
	protected harpoon.IR.Tree.Stm unNxImpl(TreeFactory tf) {
	    return new EXP(tf, exp, exp);
	}
	protected harpoon.IR.Tree.Stm unCxImpl(TreeFactory tf,
					Label iftrue, Label iffalse) {
	    // XXX: special case CONST 0 and CONST 1 ?
	    return new CJUMP(tf, exp, exp, iftrue, iffalse);
	}
    }
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
    public static abstract class Cx extends Exp {
	protected harpoon.IR.Tree.Exp unExImpl(TreeFactory tf) {
	    Temp  Tr = new Temp(tf.tempFactory(), "cx");
	    Label Lt = new Label();
	    Label Lf = new Label();
	    Stm s = unCx(tf, Lt, Lf);
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
	    Stm s = unCx(tf, l, l);
	    return new SEQ(tf, s, s, new LABEL(tf, s, l, false));
	}
    }
}
