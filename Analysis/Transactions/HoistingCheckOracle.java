// HoistingCheckOracle.java, created Sat Jan 13 14:45:14 2001 by cananian
// Copyright (C) 2000  <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.DomTree;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.SetFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
/**
 * A <code>HoistingCheckOracle</code> tries to hoist and coalesce checks
 * whenever possible.  It improves on the results of a client
 * <code>CheckOracle</code>.
 * <p>
 * The algorithm used is as follows: each check placed by the input
 * oracle is moved to its immediate dominator iff that node is
 * postdominated by the current node and that node is dominated by
 * the definition of the variable referenced in the check.  The
 * process is repeated until no checks can be moved higher.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HoistingCheckOracle.java,v 1.1.2.5 2001-01-26 22:31:25 cananian Exp $
 */
class HoistingCheckOracle extends AnalysisCheckOracle {
    /** Creates a <code>HoistingCheckOracle</code> for the given
     *  <code>HCode</code> which refines the checks placed by
     *  <code>CheckOracle</code> <code>co</code>. */
    public HoistingCheckOracle(HCode hc, UseDefer udr, CheckOracle co) {
	this(hc, udr, new DomTree(hc, false), co);
    }
    // separate constructor to let us reuse an existing domtree.
    HoistingCheckOracle(HCode hc, UseDefer udr, DomTree dt, CheckOracle co) {
	/* compute the proper check locations (post-order down dt) */
	DomTree pdt = new DomTree(hc, true);
	for (Iterator it=new ArrayIterator(dt.roots()); it.hasNext(); )
	    hoister((HCodeElement)it.next(), co, udr, dt, pdt,
		    false/*can't hoist above root*/);
	/* done! */
    }

    /* Returns checks which can be hoisted to immediate dominator */
    CheckSet hoister(HCodeElement hce, CheckOracle co, UseDefer udr,
		     DomTree dt, DomTree pdt, boolean canHoist)
    {
	/* collect checks from dominated children and from check oracle */
	CheckSet checks = new CheckSet(co, hce);
	for (Iterator it=new ArrayIterator(dt.children(hce)); it.hasNext(); )
	    checks.addAll(hoister((HCodeElement)it.next(),co,udr,dt,pdt,true));
	/** optimize: write versions are read versions */
	checks.readVersions.removeAll(checks.writeVersions);
	/** copy set of all checks. */
	CheckSet nohoist = new CheckSet(checks);

	/* can't hoist anything unless this==pidom(idom(this)) */
	canHoist = canHoist && (hce == pdt.idom(dt.idom(hce)));
	/* never hoist above a MONITORENTER */
	canHoist = canHoist && !(dt.idom(hce) instanceof MONITORENTER);
	/* okay, remove all if !canHoist */
	if (!canHoist) checks.clear();
	
	/* fetch the set of temps defined in our idom */
	Collection idomDef = (dt.idom(hce)==null) ? Collections.EMPTY_SET :
	    udr.defC(dt.idom(hce)); /* defs in idom of this */
	/* checks for a temp can't be hoisted above def */
	checks.removeAll(idomDef);

	/** leave checks here which we can't hoist. */
	nohoist.removeAll(checks);
	results.put(hce, nohoist);

	/** give all of the rest of the checks to the idom */
	return checks;
    }
}
