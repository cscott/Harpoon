// HoistingCheckOracle.java, created Sat Jan 13 14:45:14 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.DomTree;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayIterator;
import net.cscott.jutil.AggregateSetFactory;
import net.cscott.jutil.GenericMultiMap;
import net.cscott.jutil.MultiMap;
import net.cscott.jutil.SetFactory;

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
 * @version $Id: HoistingCheckOracle.java,v 1.5 2004-02-08 01:54:21 cananian Exp $
 */
// note: doesn't allow hoisting past sigmas.  since input is SSA, this is
// fine.  If you ever want to give it SSI instead, you should fix this.
class HoistingCheckOracle extends AnalysisCheckOracle {
    /** Creates a <code>HoistingCheckOracle</code> for the given
     *  <code>HCode</code> which refines the checks placed by
     *  <code>CheckOracle</code> <code>co</code>. */
    public HoistingCheckOracle(HCode hc, CFGrapher cfgr, UseDefer udr,
			       CheckOracle co) {
	this(hc, cfgr, udr, new DomTree(hc, cfgr, false), co);
    }
    // separate constructor to let us reuse an existing domtree.
    HoistingCheckOracle(HCode hc, CFGrapher cfgr, UseDefer udr,
			DomTree dt, CheckOracle co) {
	/* compute the proper check locations (post-order down dt) */
	DomTree pdt = new DomTree(hc, true);
	for (Iterator it=new ArrayIterator(dt.roots()); it.hasNext(); )
	    hoister((HCodeElement)it.next(), co, cfgr, udr, dt, pdt,
		    false/*can't hoist above root*/);
	/* done! */
    }

    /* Returns checks which can be hoisted to immediate dominator */
    CheckSet hoister(HCodeElement hce, CheckOracle co,
		     CFGrapher cfgr, UseDefer udr,
		     DomTree dt, DomTree pdt, boolean canHoist)
    {
	/* collect checks from dominated children and from check oracle */
	CheckSet checks = new CheckSet(co, hce);
	for (Iterator it=new ArrayIterator(dt.children(hce)); it.hasNext(); )
	    checks.addAll(hoister((HCodeElement)it.next(),
				  co, cfgr, udr, dt, pdt, true));
	/** find common checks in successors. */
	HCodeEdge[] succ = (HCodeEdge[]) cfgr.succC(hce).toArray(new HCodeEdge[0]);
	CheckSet common = null; // will contain intersection of all succ. chks
	for (int i=0; i<succ.length; i++) {
	    CheckSet scs = (CheckSet) results.get(succ[i].to());
	    if (scs==null) { common=null; break; /* bail out */}
	    if (common==null) common=new CheckSet(scs);
	    else {
		scs = new CheckSet(scs);
		scs.readVersions.addAll(scs.writeVersions);
		common.retainAll(scs);
	    }
	    common.readVersions.addAll(common.writeVersions);
	}
	/** filter common checks */
	if (hce instanceof MONITORENTER) common=null;
	// XXX: common=null if hoisting past possible call to Object.wait().
	if (common!=null) common.removeAll(udr.defC(hce));
	/** steal filtered common checks from successors */
	if (common!=null && !common.isEmpty()) {
	    System.err.println("HOISTING: "+common+" "+hce.getSourceFile());
	    for (int i=0; i<succ.length; i++) {
		CheckSet scs = (CheckSet) results.get(succ[i].to());
		scs.removeAll(common);
	    }
	    checks.addAll(common);
	}
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
