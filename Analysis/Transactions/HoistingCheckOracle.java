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
 * whenever possible.  It consults <code>SimpleCheckOracle</code> and
 * then tries to improve its results.
 * <p>
 * The algorithm used is as follows: each check placed by the input
 * oracle is moved to its immediate dominator iff that node is
 * postdominated by the current node and that node is dominated by
 * the definition of the variable referenced in the check.  The
 * process is repeated until no checks can be moved higher.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HoistingCheckOracle.java,v 1.1.2.2 2001-01-14 10:21:35 cananian Exp $
 */
class HoistingCheckOracle extends CheckOracle {
    final MultiMap readVMap, writeVMap, checkFMap, checkEMap;
    public Set createReadVersions(HCodeElement hce) {
	return (Set) readVMap.getValues(hce);
    }
    public Set createWriteVersions(HCodeElement hce) {
	return (Set) writeVMap.getValues(hce);
    }
    public Set checkField(HCodeElement hce) {
	return (Set) checkFMap.getValues(hce);
    }
    public Set checkArrayElement(HCodeElement hce) {
	return (Set) checkEMap.getValues(hce);
    }

    /** Creates a <code>HoistingCheckOracle</code> for the given
     *  <code>HCode</code> which refines the checks placed by
     *  <code>CheckOracle</code> <code>co</code>. */
    public HoistingCheckOracle(HCode hc, UseDefer udr, CheckOracle co) {
	/** Initialize backing data stores */
	SetFactory sf = new AggregateSetFactory();
	readVMap = new GenericMultiMap(sf);
	writeVMap = new GenericMultiMap(sf);
	checkFMap = new GenericMultiMap(sf);
	checkEMap = new GenericMultiMap(sf);

	/* okay, compute the proper check locations (post-order down dt) */
	DomTree dt = new DomTree(hc, false), pdt = new DomTree(hc, true);
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
	CheckSet checks = new CheckSet();
	checks.union(co, hce);
	for (Iterator it=new ArrayIterator(dt.children(hce)); it.hasNext(); )
	    checks.union(hoister((HCodeElement)it.next(), co,udr,dt,pdt,true));
	/** optimize: write versions are read versions */
	checks.readVersions.removeAll(checks.writeVersions);

	/* can't hoist anything unless this==pidom(idom(this)) */
	canHoist = canHoist && (hce == pdt.idom(dt.idom(hce)));
	/* never hoist above a MONITORENTER */
	canHoist = canHoist && !(dt.idom(hce) instanceof MONITORENTER);
	
	/** checks which we can't hoist we leave here. */
	
	/* fetch the set of temps defined in our idom */
	Collection idomDef = (dt.idom(hce)==null) ? Collections.EMPTY_SET :
	/* read and write versions can't be hoisted above def. */
	    udr.defC(dt.idom(hce)); /* defs in idom of this */
	for (Iterator it=checks.readVersions.iterator(); it.hasNext(); ) {
	    Temp t = (Temp) it.next(); // read version for t.
	    if (!canHoist || idomDef.contains(t)) {
		it.remove(); readVMap.add(hce, t);
	    }
	}
	for (Iterator it=checks.writeVersions.iterator(); it.hasNext(); ) {
	    Temp t = (Temp) it.next(); // write version for t.
	    if (!canHoist || idomDef.contains(t)) {
		it.remove(); writeVMap.add(hce, t);
	    }
	}
	/* field checks can't be hoisted above the objref def */
	for (Iterator it=checks.fields.iterator(); it.hasNext(); ) {
	    RefAndField raf = (RefAndField) it.next(); // field check.
	    if (!canHoist || idomDef.contains(raf.objref)) {
		it.remove(); checkFMap.add(hce, raf);
	    }
	}
	/* element checks can't be hoisted above either the objref or
	 * index def */
	for (Iterator it=checks.elements.iterator(); it.hasNext(); ) {
	    RefAndIndexAndType rit=(RefAndIndexAndType) it.next(); // el check.
	    if (!canHoist ||
		idomDef.contains(rit.objref) || idomDef.contains(rit.index)) {
		it.remove(); checkEMap.add(hce, rit);
	    }
	}

	/** give all of the rest of the checks to the idom */
	return checks;
    }


    private class CheckSet {
	private static final SetFactory sf = new AggregateSetFactory();

	final Set/*<Temp>*/ readVersions = sf.makeSet();
	final Set/*<Temp>*/ writeVersions = sf.makeSet();
	final Set/*<RefAndField>*/ fields = sf.makeSet();
	final Set/*<RefAndIndexAndType>*/ elements = sf.makeSet();
	
	CheckSet() { /* do nothing */ }
	void union(CheckOracle co, HCodeElement hce) {
	    this.readVersions.addAll(co.createReadVersions(hce));
	    this.writeVersions.addAll(co.createWriteVersions(hce));
	    this.fields.addAll(co.checkField(hce));
	    this.elements.addAll(co.checkArrayElement(hce));
	}
	void union(CheckSet cs) {
	    this.readVersions.addAll(cs.readVersions);
	    this.writeVersions.addAll(cs.writeVersions);
	    this.fields.addAll(cs.fields);
	    this.elements.addAll(cs.elements);
	}
    }
}
