// DeadCodeElimination.java, created Wed Feb 16 12:58:13 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.Liveness;
import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <code>DeadCodeElimination</code> removes unused MOVEs, useless
 * EXPRs, and whatever other cruft it can detect using a liveness
 * analysis.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DeadCodeElimination.java,v 1.4 2002-04-10 03:02:06 cananian Exp $
 */
public abstract class DeadCodeElimination extends Simplification {
    // hide constructor
    private DeadCodeElimination() { }

    /** Code factory for applying DeadCodeElimination to a
     *  canonical tree.  Clones the tree before doing
     *  DCE in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m).hcode();
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { /* i guess not */ }
		    // ...do liveness analysis and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, HCE_RULES(code));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }
    
    public static List HCE_RULES(final harpoon.IR.Tree.Code code) {
	// compute liveness
	final CFGrapher cfgr = code.getGrapher();
	final UseDefer ud = code.getUseDefer();
	final Liveness l = new LiveVars(code, cfgr, ud, Collections.EMPTY_SET);
	// now collect info about dead moves.
	final Set deadMoves = new HashSet();
	for (Iterator it=code.getElementsI(); it.hasNext(); ) {
	    Tree tr = (Tree) it.next();
	    if (tr.kind() == TreeKind.MOVE &&
		((MOVE)tr).getDst().kind() == TreeKind.TEMP) {
		Temp temp = ((TEMP) ((MOVE) tr).getDst()).temp;
		Set liveOut = l.getLiveOut(tr);
		if (!liveOut.contains(temp)) // a MOVE to an unused temp.
		    deadMoves.add(tr);
	    }
	}
	// debugging
	System.err.print("["+deadMoves.size()+" DEAD MOVES]");

	return Arrays.asList(new Rule[] {
	    // remove expressions of the form EXPR(CONST(c)) or EXPR(TEMP(t))
	    //
	    // SEQ(EXPR(CONST(c)), s1) --> s1
	    // SEQ(s1, EXPR(CONST(c)) --> s1
	    // SEQ(EXPR(TEMP(t)), s1) --> s1
	    // SEQ(s1, EXPR(TEMP(t))) --> s1
	    new Rule("removeNop") {
		public boolean match(Stm s) {
		    if (s.kind() != TreeKind.SEQ) return false;
		    SEQ seq = (SEQ) s;
		    return isNop(seq.getLeft()) || isNop(seq.getRight());
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    SEQ seq = (SEQ) s;
		    if (isNop(seq.getLeft())) return seq.getRight();
		    if (isNop(seq.getRight())) return seq.getLeft();
		    throw new Error("Neither side is a Nop!");
		}
		private boolean isNop(Stm s) {
		    if (s.kind() != TreeKind.EXPR) return false;
		    EXPR exp = (EXPR) s;
		    return contains(_KIND(exp.getExp()), _CONST|_TEMP);
		}
	    },
	    // MOVE(t1, e) --> EXPR(e) if t1 is dead after move
	    new Rule("deadMoves") {
		public boolean match(Stm s) {
		    return deadMoves.contains(s);
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    MOVE move = (MOVE) s;
		    return new EXPR(tf, move, move.getSrc());
		}
	    },
	});
    }
}
