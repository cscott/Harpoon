// Canonicalize.java, created Mon Feb 14 20:16:11 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * <code>Canonicalize</code> is an application of <code>Simplification</code>
 * to do pattern-driven tree canonicalization.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Canonicalize.java,v 1.2 2002-02-25 21:00:32 cananian Exp $
 */
public abstract class Canonicalize extends Simplification {
    // hide constructor
    private Canonicalize() { }

    private final static List _RULES = new ArrayList(); 
    /** Default canonicalization rules. */
    public final static List RULES = // protect the rules list.
	Collections.unmodifiableList(_RULES);

    /** Code factory for applying the default set of simplifications to
     *  the given tree form.  Clones the tree before simplifying it
     *  in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    }

    // static initialization: add all rules to the rule set
    static {

	//          ...REDUCE TOP-LEVEL ESEQs...
	// ESEQ(s1, ESEQ(s2, e)) --> ESEQ(SEQ(s1, s2), e)
	Rule doubleEseq = new Rule("doubleEseq") {
	    public boolean match(Exp e) {
		if (!contains(_KIND(e), _ESEQ)) return false;
		ESEQ eseq = (ESEQ) e;
		return contains(_KIND(eseq.getExp()), _ESEQ);
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		ESEQ e1 = (ESEQ) e;
		ESEQ e2 = (ESEQ) e1.getExp();
		if (dg!=null) { dg.remove(e1); dg.remove(e2); }
		return new ESEQ(tf, e,
				new SEQ(tf, e, e1.getStm(), e2.getStm()),
				e2.getExp());
	    }
	};
	//            ...REDUCE LEFT-MOST ESEQs.....
	// BINOP(op, ESEQ(s, e1), e2) --> ESEQ(s, BINOP(op, e1, e2))
	// MEM(ESEQ(s, e1)) --> ESEQ(s, MEM(e1))
	// ...and other Exps with ESEQs as left-most child...
	// JUMP(ESEQ(s, e1)) --> SEQ(s, JUMP(e1))
	// CJUMP(ESEQ(s, e1), l1, l2) --> SEQ(s, CJUMP(e1, l1, l2))
	// ...and other Stms with ESEQs as left-most child. 
	Rule leftEseq = new Rule("leftEseq") {
	    public boolean match(Exp e) {
		if (contains(_KIND(e), _ESEQ)) return false;
		if (e.getParent()!=null && // avoid MOVE(ESEQ(MEM(..)), ..)
		    contains(_KIND(e.getParent()), _MOVE) &&
		    ((MOVE)e.getParent()).getDst() == e) return false;
		ExpList el = e.kids();
		if (el==null) return false;
		return contains(_KIND(el.head), _ESEQ);
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		ExpList el = e.kids();
		ESEQ eseq = (ESEQ) el.head;
		el = new ExpList(eseq.getExp(), el.tail);
		Exp newE = e.build(el);
		if (dg != null) dg.update(e, newE);
		return new ESEQ(tf, e, eseq.getStm(), newE);
	    }
	    public boolean match(Stm s) {
		if (contains(_KIND(s), _SEQ)) return false;
		ExpList el = s.kids();
		if (el==null) return false;
		return contains(_KIND(el.head), _ESEQ);
	    }
	    public Stm apply(TreeFactory tf, Stm s, DerivationGenerator dg) {
		ExpList el = s.kids();
		ESEQ eseq = (ESEQ) el.head;
		el = new ExpList(eseq.getExp(), el.tail);
		return new SEQ(tf, s, eseq.getStm(), sbuild(s,dg, el));
	    }
	};

	//            ...MOVE RIGHT-HAND ESEQs LEFTWARD...
	// BINOP(op, e1, ESEQ(s, e2)) --> ESEQ(s, BINOP(op, e1, e2))
	//    if (s, e1) commute...
	//           --> BINOP(op, ESEQ(SEQ(MOVE(t, e1), s), t), e2)
	// ...otherwise.
	// CALL(..[e1, ESEQ(s, e2), e3]..)-->SEQ(s, CALL(..[e1, e2, e3]..))
	//    if (s, e1) commute...
	//             --> CALL(..[ESEQ(SEQ(MOVE(t, e1), s), t), e2, e3]..)
	// ...otherwise.
	Rule rightEseq = new Rule("rightEseq") {
	    public boolean match(Exp e) {
		if (contains(_KIND(e), _ESEQ)) return false;
		ExpList el = e.kids();
		if (el==null || el.tail==null) return false;
		for (ExpList elp = el; elp.tail!=null; elp=elp.tail) {
		    if (contains(_KIND(elp.tail.head), _ESEQ))
		      return true;
		}
		return false;
	    }
	    public boolean match(Stm s) {
		if (contains(_KIND(s), _SEQ)) return false;
		ExpList el = s.kids();
		if (el==null || el.tail==null) return false;
		for (ExpList elp = el; elp.tail!=null; elp=elp.tail) {
		    if (contains(_KIND(elp.tail.head), _ESEQ))
		      return true;
		}
		return false;
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		Exp newE = e.build(shiftOne(tf, dg, e.kids()));
		if (dg != null) dg.update(e, newE);
		return newE;
	    }
	    public Stm apply(TreeFactory tf, Stm s, DerivationGenerator dg) {
		return sbuild(s,dg, shiftOne(tf, dg, s.kids()));
	    }
	    ExpList shiftOne(TreeFactory tf, DerivationGenerator dg,
			     ExpList el) {
		if (!contains(_KIND(el.tail.head), _ESEQ))
		  return new ExpList(el.head, shiftOne(tf, dg, el.tail));
		Exp  e1 = el.head;
		ESEQ e2 = (ESEQ) el.tail.head;
		if (dg != null) dg.remove(e2); // ain't gonna live no mo'
		if (commute(e2.getStm(), e1)) { // simple case.
		    // [..e1, ESEQ(s, e2), ..] --> [..ESEQ(s, e1), e2,..]
		    return new ExpList(new ESEQ(tf, e1, e2.getStm(), e1),
				       new ExpList(e2.getExp(), el.tail.tail));
		}
		// otherwise, we have to make a new temp...
		//  [..e1, ESEQ(s, e2),..] -->
		//                     [..ESEQ(SEQ(MOVE(t, e1), s), t), e2..]
		Temp t  = new Temp(tf.tempFactory(), "canon");
		TEMP T1 = new TEMP(tf, e1, e1.type(), t);
		TEMP T2 = new TEMP(tf, e1, e1.type(), t);
		if (dg!=null) { // make types for the new TEMPs/Temp
		    HClass hc = dg.typeMap(e1);
		    if (hc!=null) {
			dg.putTypeAndTemp(T1, hc, t);
			dg.putTypeAndTemp(T2, hc, t);
		    } else {
			dg.putDerivation(T1, dg.derivation(e1));
			dg.putDerivation(T2, dg.derivation(e1));
		    }
		}
		return new ExpList
		(new ESEQ
		 (tf, e1,
		  new SEQ
		  (tf, e1,
		   new MOVE(tf, e1, T1, e1),
		   e2.getStm() ),
		  T2 ),
		 new ExpList(e2.getExp(), el.tail.tail));
	    }
	};

	// add rules to the rule set.
	_RULES.add(doubleEseq);
	_RULES.add(leftEseq);
	_RULES.add(rightEseq);
    }

    private static boolean commute(Stm a, Exp b) {
	return isNop(a) || contains(_KIND(b), _NAME|_CONST);
    }
    private static boolean isNop(Stm a) {
	return contains(_KIND(a), _EXPR) &&
	    contains(_KIND(((EXPR)a).getExp()), _CONST|_TEMP);
    }
    /* MOVE.build() doesn't correctly propagate derivation information to
     * MOVE(MEM(...), ...); this utility function fixes this case up. */
    private static Stm sbuild(Stm s, DerivationGenerator dg, ExpList kids) {
	Stm ns = s.build(kids);
	if (dg!=null &&
	    contains(_KIND(s), _MOVE) &&
	    contains(_KIND(s.getFirstChild()), _MEM))
	    dg.update((Exp)s.getFirstChild(), (Exp)ns.getFirstChild());
	return ns;
    }
    /** Testing function, for use in assertions that a given tree is
     *  canonical. */
    public static boolean containsEseq(Tree t) {
	if (t.kind() == TreeKind.ESEQ) return true;
	// else, recurse
	for (Tree tp=t.getFirstChild(); tp!=null; tp=tp.getSibling())
	    if (containsEseq(tp)) return true;
	// i guess it doesn't, then.
	return false;
    }
}
