// CommutativityExpander.java, created Thu Feb 17 15:42:30 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.PatMat;

import java.util.*;
/**
 * The <code>CommutativityExpander</code> tool expands a set of
 * <code>Spec.Rules</code> to include add'l valid patterns
 * generated from the commutative properties of various
 * <code>Spec.ExpBinop</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CommutativityExpander.java,v 1.1.2.3 2000-02-19 01:41:49 cananian Exp $
 */
public abstract class CommutativityExpander  {
    //hide constructor.
    private CommutativityExpander() { }
    
    public static Spec expand(Spec s) {
	return new Spec(s.global_stms, s.class_stms,
			s.method_prologue_stms, s.method_epilogue_stms,
			expand(s.rules));
    }
    private static Spec.RuleList expand(Spec.RuleList rl) {
	if (rl==null) return null;
	Spec.RuleList result = expand(rl.tail);
	Spec.Rule[] ra = expand(rl.head);
	for (int i=0; i<ra.length; i++)
	    result = new Spec.RuleList(ra[i], result);
	return result;
    }
    private static Spec.Rule[] expand(Spec.Rule r) {
	if (r instanceof Spec.RuleExp)
	    return expand((Spec.RuleExp) r);
	if (r instanceof Spec.RuleStm)
	    return expand((Spec.RuleStm) r);
	throw new Error("Unknown Spec.Rule type!");
    }
    private static Spec.RuleExp[] expand(Spec.RuleExp r) {
	ExpAndPred[] choices = expand(r.exp);
	Spec.RuleExp[] result = new Spec.RuleExp[choices.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new Spec.RuleExp(choices[i].exp, r.result_id,
					 addPred(r.details, choices[i].pred),
					 r.action_str);
	return result;
    }
    private static Spec.RuleStm[] expand(Spec.RuleStm r) {
	StmAndPred[] choices = expand(r.stm);
	Spec.RuleStm[] result = new Spec.RuleStm[choices.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new Spec.RuleStm(choices[i].stm,
					 addPred(r.details, choices[i].pred),
					 r.action_str);
	return result;
    }
    private static ExpAndPred[] dispatch(Spec.Exp exp) {
	if (exp instanceof Spec.ExpBinop)
	    return expand((Spec.ExpBinop)exp);
	else return expand(exp);
    }
    // this is the real deal:
    private static ExpAndPred[] expand(Spec.ExpBinop exp) {
	// make normal combinations first.
	ExpListAndPred[] combos = makeAllCombinations(exp.kids());
	// check whether this exp rules is commutative & worth expanding.
	if ( // first off, one of left/right has to be non-id
	    !(exp.left instanceof Spec.ExpId&&exp.right instanceof Spec.ExpId)
	    && // also, if op is a leafop, it needs to be a commutative one.
	    (exp.opcode instanceof Spec.LeafId ||
	     (exp.opcode instanceof Spec.LeafOp &&
	      harpoon.IR.Tree.Bop.isCommutative(((Spec.LeafOp) exp.opcode).op))
	     )) {
	    // check to see if an extra predicate is needed.
	    String extrapred = (exp.opcode instanceof Spec.LeafOp) ? null :
		"harpoon.IR.Tree.Bop.isCommutative(" +
		                           ((Spec.LeafId)exp.opcode).id + ")";
	    // okay, reverse kids and make some more combos.
	    ExpListAndPred[] newcombos = new ExpListAndPred[combos.length * 2];
	    for (int i=0; i<combos.length; i++) {
		ExpListAndPred source = combos[i];
		newcombos[2*i] = source;
		Spec.ExpList nel =
		    new Spec.ExpList(source.explist.tail.head,
				new Spec.ExpList(source.explist.head, null));
		newcombos[2*i+1] =
		    new ExpListAndPred(nel, addPred(source.pred, extrapred));
	    }
	    combos = newcombos;
	}
	// make exps from all the explists.
	ExpAndPred[] result = new ExpAndPred[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new ExpAndPred(exp.build(combos[i].explist),
				       combos[i].pred);
	return result;
    }
    private static ExpAndPred[] expand(Spec.Exp exp) {
	// make explists with all combinations of children.
	ExpListAndPred[] combos = makeAllCombinations(exp.kids());
	// make exps from all the explists.
	ExpAndPred[] result = new ExpAndPred[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new ExpAndPred(exp.build(combos[i].explist),
				       combos[i].pred);
	return result;
    }
    private static StmAndPred[] expand(Spec.Stm stm) {
	// make explists with all combinations of children.
	ExpListAndPred[] combos = makeAllCombinations(stm.kids());
	// make exps from all the explists.
	StmAndPred[] result = new StmAndPred[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new StmAndPred(stm.build(combos[i].explist),
				       combos[i].pred);
	return result;
    }
    private static int size(Spec.ExpList el) {
	int i;
	for (i=0; el!=null; el=el.tail)
	    i++;
	return i;
    }
    private static ExpListAndPred[] makeAllCombinations(Spec.ExpList kids) {
	// construct set of expanded children.
	ExpAndPred[][] choices = new ExpAndPred[size(kids)][];
	for (int i=0; kids!=null; kids=kids.tail)
	    choices[i++] = dispatch(kids.head);
	// make explists with all combinations of children.
	return makeAllCombinations(choices);
    }
    private static ExpListAndPred[] makeAllCombinations(ExpAndPred[][] choices)
    {
	int[] state = new int[choices.length]; // all zero initially.
	// count the # of possibilities
	int numposs = 1;
	for (int i=0; i<choices.length; i++)
	    numposs *= choices[i].length;
	// iterate:
	ExpListAndPred[] result = new ExpListAndPred[numposs];
	for (int i=0; i < numposs; i++) {
	    // make a Spec.ExpList
	    Spec.ExpList el=null; String pred=null;
	    for (int j=state.length-1; j>=0; j--) {
		ExpAndPred eap = choices[j][state[j]];
		pred = addPred(eap.pred, pred);
		el = new Spec.ExpList( eap.exp, el );
	    }
	    // add to result.
	    result[i] = new ExpListAndPred(el, pred);
	    // advance state by one
	    for (int j=0; j < state.length; j++) {
		if (++state[j] < choices[j].length)
		    break;
		else state[j] = 0;
	    }
	}
	return result;
    }
    private static class ExpAndPred {
	final Spec.Exp exp;
	final String pred;
	ExpAndPred(Spec.Exp exp, String pred) {
	    this.exp = exp; this.pred = pred;
	}
    }
    private static class ExpListAndPred {
	final Spec.ExpList explist;
	final String pred;
	ExpListAndPred(Spec.ExpList explist, String pred) {
	    this.explist = explist; this.pred = pred;
	}
    }
    private static class StmAndPred {
	final Spec.Stm stm;
	final String pred;
	StmAndPred(Spec.Stm stm, String pred) {
	    this.stm = stm; this.pred = pred;
	}
    }
    private static String addPred(String p1, String p2) {
	if (p1==null) return p2;
	if (p2==null) return p1;
	return "("+p1+") && ("+p2+")";
    }
    private static Spec.DetailList addPred(Spec.DetailList dl, String pred) {
	if (pred==null)
	    return dl;
	if (dl==null)
	    return new Spec.DetailList(new Spec.DetailPredicate(pred), null);
	if (dl.head instanceof Spec.DetailPredicate) {
	    Spec.DetailPredicate sdp = (Spec.DetailPredicate) dl.head;
	    return new Spec.DetailList(new Spec.DetailPredicate
				       (addPred(sdp.predicate_string,
						pred)), dl.tail);
	}
	else return new Spec.DetailList(dl.head, addPred(dl.tail, pred));
    }
}
