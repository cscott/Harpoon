// MutRegExpSimplifier.java, created Mon Sep  5 19:41:04 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

import jpaul.RegExps.RegExp;

import jpaul.Misc.IdentityWrapper;
import jpaul.Misc.Function;

import jpaul.DataStructs.DSUtil;

/**
 * <code>MutRegExpSimplifier</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MutRegExpSimplifier.java,v 1.2 2005-09-07 20:36:50 salcianu Exp $
 */
class MutRegExpSimplifier {

    private static boolean DEBUG = false;

    private MutRegExpSimplifier() {}

    private Map<IdentityWrapper<RegExp<MLabel>>,RegExp<MLabel>> cache =
	new HashMap<IdentityWrapper<RegExp<MLabel>>,RegExp<MLabel>>();

    private RegExp<MLabel> simp(RegExp<MLabel> regExp) {
	IdentityWrapper<RegExp<MLabel>> iw = new IdentityWrapper<RegExp<MLabel>>(regExp);
	RegExp<MLabel> simpRegExp = cache.get(iw);
	if(simpRegExp == null) {
	    simpRegExp = _simp(regExp);
	    cache.put(iw, simpRegExp);
	}
	return simpRegExp;
    }

    private RegExp<MLabel> _simp(RegExp<MLabel> regExp) {
	return regExp.accept(new RegExp.Visitor<MLabel,RegExp<MLabel>>() {
	    public RegExp<MLabel> visit(RegExp.Union<MLabel> ure) {
		RegExp<MLabel> left  = simp(ure.left);
		if(isReach(left))
		    return left;
		RegExp<MLabel> right = simp(ure.right);
		if(isReach(right))
		    return right;
		return new RegExp.Union<MLabel>(left, right);
	    }

	    public RegExp<MLabel> visit(RegExp.Star<MLabel> sre) {
		RegExp<MLabel> starred = simp(sre.starred);
		if(isReach(starred)) {
		    return starred;
		}
		return new RegExp.Star<MLabel>(starred);
	    }

	    private boolean isAtomic(RegExp<MLabel> regExp, MLabel mLabel) {
		if(!(regExp instanceof RegExp.Atomic/*<MLabel>*/)) return false;
		return ((RegExp.Atomic<MLabel>) regExp).a.equals(mLabel);
	    }

	    private boolean isReach(RegExp<MLabel> regExp) {
		return isAtomic(regExp, MLabel.reach);
	    }

	    private boolean isStar(RegExp<MLabel> regExp) {
		return regExp instanceof RegExp.Star/*<MLabel>*/;
	    }

	    public RegExp<MLabel> visit(RegExp.Concat<MLabel> cre) {
		List<RegExp<MLabel>> concatTerms = 
		    (List<RegExp<MLabel>>) DSUtil.mapColl
		    (cre.allTransTerms(),
		     new Function<RegExp<MLabel>,RegExp<MLabel>>() {
			 public RegExp<MLabel> f(RegExp<MLabel> re) {
			     return _simp(re);
			 }
		     },
		     new LinkedList<RegExp<MLabel>>());

		simplifyConcatTerms(concatTerms);

		return RegExp.<MLabel>buildConcat(concatTerms);
	    }


	    private void simplifyConcatTerms(List<RegExp<MLabel>> concatTerms) {
		//System.out.println("Original terms = " + concatTerms);
		
		boolean changed = false;
		for(ListIterator<RegExp<MLabel>> li = concatTerms.listIterator(); li.hasNext(); ) {
		    RegExp<MLabel> term = li.next();
		    if(isReach(term)) {
			// 1. remove previous star/reach expressions (if any)
			li.previous();
			while(li.hasPrevious()) {
			    RegExp<MLabel> prev = li.previous();
			    if(isReach(prev) || isStar(prev)) {
				li.remove();
				changed = true;
			    }
			    else {
				li.next();
				break;
			    }
			}
			li.next();
			// 2. remove next star/reach expressions (if any)
			while(li.hasNext()) {
			    RegExp<MLabel> next = li.next();
			    if(isReach(next) || isStar(next)) {
				li.remove();
				changed = true;
				}
			    else {
				li.previous();
				break;
			    }
			}
		    }
		}
		
		//if(changed) System.out.println("Simplified terms = " + concatTerms);

		// REACHfromSTAT.REACH -> REACHfromSTAT
		// REACHfromSTAT.(foo)* -> REACHfromSTAT
		if(concatTerms.size() >= 2) {
		    Iterator<RegExp<MLabel>> it = concatTerms.iterator();
		    RegExp<MLabel> first  = it.next();
		    RegExp<MLabel> second = it.next();
		    if(isAtomic(first, MLabel.reachFromStat)) {
			if(isAtomic(second, MLabel.reach) || isStar(second)) {
			    // remove the REACH / (foo)* element
			    concatTerms.remove(1);
			    changed = true;
			    //System.out.println("Simplified terms (2) = " + concatTerms);
			}
		    }
		}

	    }

	    // default visitor: don't do any simplification
	    // will be called only for None, EmptyStr, and Atomic
	    public RegExp<MLabel> visit(RegExp<MLabel> regExp) {
		return regExp;
	    }
	});
    }

    static RegExp<MLabel> simplify(RegExp<MLabel> regExp) {
	return (new MutRegExpSimplifier()).simp(regExp);
    }

}
