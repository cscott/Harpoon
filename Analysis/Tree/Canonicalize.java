// Canonicalize.java, created Mon Feb 14 20:16:11 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXP;
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
 * <code>Canonicalize</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Canonicalize.java,v 1.1.2.2 2000-02-15 14:55:59 cananian Exp $
 */
public abstract class Canonicalize extends Simplification {
    // hide constructor
    private Canonicalize() { }

    private final static List _RULES = new ArrayList(); 
    /** Default alegraic simplification rules. */
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
	    public Exp apply(Exp e) {
		TreeFactory tf = e.getFactory();
		ESEQ e1 = (ESEQ) e;
		ESEQ e2 = (ESEQ) e1.getExp();
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
		    contains(_KIND(e.getParent()), _MOVE)) return false;
		ExpList el = e.kids();
		if (el==null) return false;
		return contains(_KIND(el.head), _ESEQ);
	    }
	    public Exp apply(Exp e) {
		TreeFactory tf = e.getFactory();
		ExpList el = e.kids();
		ESEQ eseq = (ESEQ) el.head;
		el = new ExpList(eseq.getExp(), el.tail);
		return new ESEQ(tf, e, eseq.getStm(), e.build(el));
	    }
	    public boolean match(Stm s) {
		if (contains(_KIND(s), _SEQ)) return false;
		ExpList el = s.kids();
		if (el==null) return false;
		return contains(_KIND(el.head), _ESEQ);
	    }
	    public Stm apply(Stm s) {
		TreeFactory tf = s.getFactory();
		ExpList el = s.kids();
		ESEQ eseq = (ESEQ) el.head;
		el = new ExpList(eseq.getExp(), el.tail);
		return new SEQ(tf, s, eseq.getStm(), s.build(el));
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
	    public Exp apply(Exp e) {
		TreeFactory tf = e.getFactory();
		return e.build(shiftOne(tf, e.kids()));
	    }
	    public Stm apply(Stm s) {
		TreeFactory tf = s.getFactory();
		return s.build(shiftOne(tf, s.kids()));
	    }
	    ExpList shiftOne(TreeFactory tf, ExpList el) {
		if (!contains(_KIND(el.tail.head), _ESEQ))
		  return new ExpList(el.head, shiftOne(tf, el.tail));
		Exp  e1 = el.head;
		ESEQ e2 = (ESEQ) el.tail.head;
		if (commute(e2.getStm(), e1)) // simple case.
		    return new ExpList(new ESEQ(tf, e1, e2.getStm(), e1),
				       new ExpList(e2.getExp(), el.tail.tail));
		// otherwise, we have to make a new temp...
		Temp t  = new Temp(tf.tempFactory(), "canon");
		return new ExpList
		(new ESEQ
		 (tf, e1,
		  new SEQ
		  (tf, e1,
		   new MOVE
		   (tf, e1, new TEMP(tf, e1, e1.type(), t), e1),
		   e2.getStm() ),
		  new TEMP(tf, e1, e1.type(), t) ),
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
	return contains(_KIND(a), _EXP) &&
	    contains(_KIND(((EXP)a).getExp()), _CONST);
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
