// DiffInclConstraint.java, created Sun Apr  7 19:08:01 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import harpoon.Util.PredicateWrapper;
import harpoon.Util.Util;

/**
 * <code>DiffInclConstraint</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: DiffInclConstraint.java,v 1.1 2002-04-11 04:25:19 salcianu Exp $
 */
public class DiffInclConstraint extends Constraint {
    private Var v1;
    private Var v2;
    private PredicateWrapper pred;

    /** Creates a <code>DiffInclConstraint</code>. */
    public DiffInclConstraint(Var v1, PredicateWrapper pred, Var v2) {
	this.v1   = v1;
	this.v2   = v2;
	this.pred = pred;
        in_dep_array  = new Var[]{v1};
        out_dep_array = new Var[]{v2};
    }

    public void action(PSolAccesser sacc) {
	Set delta1 = sacc.getDeltaSet(v1);
	for(Iterator it = delta1.iterator(); it.hasNext(); ) {
	    Object obj = it.next();
	    if(pred.check(obj))
		sacc.updateSetWithOneElem(v2, obj);
	}
    }

    public Constraint convert(Map m) {
	return
	    new DiffInclConstraint((Var) Util.convert(v1, m), pred,
				   (Var) Util.convert(v2, m));
    }

    public String toString() {
	return "DIC: " + v1 + " \\subseteq " + v2 + " with filter " + pred; 
    }

}
