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
 * @version $Id: CommutativityExpander.java,v 1.1.2.1 2000-02-17 23:53:49 cananian Exp $
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
	Spec.Exp[] choices = expand(r.exp);
	Spec.RuleExp[] result = new Spec.RuleExp[choices.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new Spec.RuleExp(choices[i], r.result_id,
					 r.details, r.action_str);
	return result;
    }
    private static Spec.RuleStm[] expand(Spec.RuleStm r) {
	Spec.Stm[] choices = expand(r.stm);
	Spec.RuleStm[] result = new Spec.RuleStm[choices.length];
	for (int i=0; i<result.length; i++)
	    result[i] = new Spec.RuleStm(choices[i],
					 r.details, r.action_str);
	return result;
    }
    private static Spec.Exp[] dispatch(Spec.Exp exp) {
	if (exp instanceof Spec.ExpBinop)
	    return expand((Spec.ExpBinop)exp);
	else return expand(exp);
    }
    // this is the real deal:
    private static Spec.Exp[] expand(Spec.ExpBinop exp) {
	// make normal combinations first.
	Spec.ExpList[] combos = makeAllCombinations(exp.kids());
	// check whether this exp rules is commutative & worth expanding.
	if (exp.opcode instanceof Spec.LeafOp &&
	    harpoon.IR.Tree.Bop.isCommutative(((Spec.LeafOp)exp.opcode).op) &&
	    !(exp.left instanceof Spec.ExpId &&
	      exp.right instanceof Spec.ExpId)) {
	    // okay, reverse kids and make some more combos.
	    Spec.ExpList[] newcombos = new Spec.ExpList[combos.length * 2];
	    for (int i=0; i<combos.length; i++) {
		Spec.ExpList source = combos[i];
		newcombos[2*i] = source;
		newcombos[2*i+1] =
		    new Spec.ExpList(source.tail.head,
				     new Spec.ExpList(source.head, null));
	    }
	    combos = newcombos;
	}
	// make exps from all the explists.
	Spec.Exp[] result = new Spec.Exp[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = exp.build(combos[i]);
	return result;
    }
    private static Spec.Exp[] expand(Spec.Exp exp) {
	// make explists with all combinations of children.
	Spec.ExpList[] combos = makeAllCombinations(exp.kids());
	// make exps from all the explists.
	Spec.Exp[] result = new Spec.Exp[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = exp.build(combos[i]);
	return result;
    }
    private static Spec.Stm[] expand(Spec.Stm stm) {
	// make explists with all combinations of children.
	Spec.ExpList[] combos = makeAllCombinations(stm.kids());
	// make exps from all the explists.
	Spec.Stm[] result = new Spec.Stm[combos.length];
	for (int i=0; i<result.length; i++)
	    result[i] = stm.build(combos[i]);
	return result;
    }
    private static int size(Spec.ExpList el) {
	int i;
	for (i=0; el!=null; el=el.tail)
	    i++;
	return i;
    }
    private static Spec.ExpList[] makeAllCombinations(Spec.ExpList kids) {
	// construct set of expanded children.
	Spec.Exp[][] choices = new Spec.Exp[size(kids)][];
	for (int i=0; kids!=null; kids=kids.tail)
	    choices[i++] = dispatch(kids.head);
	// make explists with all combinations of children.
	return makeAllCombinations(choices);
    }
    private static Spec.ExpList[] makeAllCombinations(Spec.Exp[][] choices) {
	int[] state = new int[choices.length]; // all zero initially.
	// count the # of possibilities
	int numposs = 1;
	for (int i=0; i<choices.length; i++)
	    numposs *= choices[i].length;
	// iterate:
	Spec.ExpList[] result = new Spec.ExpList[numposs];
	for (int i=0; i < numposs; i++) {
	    // make a Spec.ExpList
	    Spec.ExpList el=null;
	    for (int j=state.length-1; j>=0; j--)
		el = new Spec.ExpList( choices[j][state[j]], el );
	    // add to result.
	    result[i] = el;
	    // advance state by one
	    for (int j=0; j < state.length; j++) {
		if (++state[j] < choices[j].length)
		    break;
		else state[j] = 0;
	    }
	}
	return result;
    }
}
