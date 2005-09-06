// MutRegExpSimplifier.java, created Mon Sep  5 19:41:04 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.Mutation;

import java.util.Map;
import java.util.HashMap;

import jpaul.RegExps.RegExp;

import jpaul.Misc.IdentityWrapper;

/**
 * <code>MutRegExpSimplifier</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MutRegExpSimplifier.java,v 1.1 2005-09-06 04:39:05 salcianu Exp $
 */
class MutRegExpSimplifier {

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
	    
	    // TODO: more simplif:

	    // 1. in jpaul.RegExps.RegExp - Union should not contain
	    // duplicates a|b|c|a = a|b|c - Get all terms of a
	    // (possibly nested) Union; put them in a set (no
	    // duplicates), and next built a new Union from the disjoint elems.

	    // 2. In this file: linearize nested concats; next
	    // transform each ocurrence of (foo)*.REACH into REACH

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

	    private boolean isReach(RegExp<MLabel> regExp) {
		if(!(regExp instanceof RegExp.Atomic/*<MLabel>*/)) return false;
		return ((RegExp.Atomic<MLabel>) regExp).a instanceof MLabel.Reach;
	    }

	    public RegExp<MLabel> visit(RegExp.Concat<MLabel> cre) {
		return
		    new RegExp.Concat<MLabel>(simp(cre.left), simp(cre.right));
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
