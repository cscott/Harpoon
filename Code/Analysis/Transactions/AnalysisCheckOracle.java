// AnalysisCheckOracle.java, created Tue Jan 16 12:12:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.SetFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * An <code>AnalysisCheckOracle</code> is used when one wants to
 * do some analysis and store the results of the check oracle.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AnalysisCheckOracle.java,v 1.3 2002-04-10 03:01:43 cananian Exp $
 */
abstract class AnalysisCheckOracle extends CheckOracle {
    final Map<HCodeElement,CheckSet> results =
	new HashMap<HCodeElement,CheckSet>();
    public Set<Temp> createReadVersions(HCodeElement hce) {
	return results.get(hce).readVersions;
    }
    public Set<Temp> createWriteVersions(HCodeElement hce) {
	return results.get(hce).writeVersions;
    }
    public Set<RefAndField> checkFieldReads(HCodeElement hce) {
	return results.get(hce).fieldReads;
    }
    public Set<RefAndField> checkFieldWrites(HCodeElement hce) {
	return results.get(hce).fieldWrites;
    }
    public Set<RefAndIndexAndType> checkArrayElementReads(HCodeElement hce) {
	return results.get(hce).elementReads;
    }
    public Set<RefAndIndexAndType> checkArrayElementWrites(HCodeElement hce) {
	return results.get(hce).elementWrites;
    }
    
    /** Creates a <code>AnalysisCheckOracle</code>. */
    AnalysisCheckOracle() { /* nothing */ }

    private final SetFactory sf = new AggregateSetFactory();

    class CheckSet {
	final Set<Temp> readVersions = sf.makeSet();
	final Set<Temp> writeVersions = sf.makeSet();
	final Set<RefAndField> fieldReads = sf.makeSet();
	final Set<RefAndField> fieldWrites = sf.makeSet();
	final Set<RefAndIndexAndType> elementReads = sf.makeSet();
	final Set<RefAndIndexAndType> elementWrites = sf.makeSet();
	
	CheckSet() { /* new empty set */ }
	CheckSet(CheckSet cs) { // new set w/ contents of given set.
	    this.readVersions.addAll(cs.readVersions);
	    this.writeVersions.addAll(cs.writeVersions);
	    this.fieldReads.addAll(cs.fieldReads);
	    this.fieldWrites.addAll(cs.fieldWrites);
	    this.elementReads.addAll(cs.elementReads);
	    this.elementWrites.addAll(cs.elementWrites);
	}
	CheckSet(CheckOracle co, HCodeElement hce) {
	    this.readVersions.addAll(co.createReadVersions(hce));
	    this.writeVersions.addAll(co.createWriteVersions(hce));
	    this.fieldReads.addAll(co.checkFieldReads(hce));
	    this.fieldWrites.addAll(co.checkFieldWrites(hce));
	    this.elementReads.addAll(co.checkArrayElementReads(hce));
	    this.elementWrites.addAll(co.checkArrayElementWrites(hce));
	}
	void addAll(CheckSet cs) {
	    this.readVersions.addAll(cs.readVersions);
	    this.writeVersions.addAll(cs.writeVersions);
	    this.fieldReads.addAll(cs.fieldReads);
	    this.fieldWrites.addAll(cs.fieldWrites);
	    this.elementReads.addAll(cs.elementReads);
	    this.elementWrites.addAll(cs.elementWrites);
	}
	/** Remove all checks which mention <code>Temp</code>s contained
	 *  in the given <code>Collection</code>. */
	void removeAll(Collection<Temp> temps) {
	    for (Iterator<Temp> it=readVersions.iterator(); it.hasNext(); )
		if (temps.contains(it.next()))
		    it.remove();
	    for (Iterator<Temp> it=writeVersions.iterator(); it.hasNext(); )
		if (temps.contains(it.next()))
		    it.remove();
	    for (Iterator<RefAndField> it=fieldReads.iterator();it.hasNext();)
		if (temps.contains(it.next().objref))
		    it.remove();
	    for (Iterator<RefAndField> it=fieldWrites.iterator();it.hasNext();)
		if (temps.contains(it.next().objref))
		    it.remove();
	    for (Iterator<RefAndIndexAndType> it=elementReads.iterator();
		 it.hasNext(); ) {
		RefAndIndexAndType rit = it.next();
		if (temps.contains(rit.objref) || temps.contains(rit.index))
		    it.remove();
	    }
	    for (Iterator<RefAndIndexAndType> it=elementWrites.iterator();
		 it.hasNext(); ) {
		RefAndIndexAndType rit = it.next();
		if (temps.contains(rit.objref) || temps.contains(rit.index))
		    it.remove();
	    }
	}
	void retainAll(CheckSet cs) {
	    this.readVersions.retainAll(cs.readVersions);
	    this.writeVersions.retainAll(cs.writeVersions);
	    this.fieldReads.retainAll(cs.fieldReads);
	    this.fieldWrites.retainAll(cs.fieldWrites);
	    this.elementReads.retainAll(cs.elementReads);
	    this.elementWrites.retainAll(cs.elementWrites);
	}
	void removeAll(CheckSet cs) {
	    this.readVersions.removeAll(cs.readVersions);
	    this.writeVersions.removeAll(cs.writeVersions);
	    this.fieldReads.removeAll(cs.fieldReads);
	    this.fieldWrites.removeAll(cs.fieldWrites);
	    this.elementReads.removeAll(cs.elementReads);
	    this.elementWrites.removeAll(cs.elementWrites);
	}
	boolean isEmpty() {
	    return readVersions.isEmpty() && writeVersions.isEmpty()
		&& fieldReads.isEmpty() && fieldWrites.isEmpty()
		&& elementReads.isEmpty() && elementWrites.isEmpty();
	}
	void clear() {
	    this.readVersions.clear();
	    this.writeVersions.clear();
	    this.fieldReads.clear();
	    this.fieldWrites.clear();
	    this.elementReads.clear();
	    this.elementWrites.clear();
	}
	public CheckSet clone() { return new CheckSet(this); }
	public String toString() {
	    return "Rd: "+readVersions+" / Wr: "+writeVersions+" / "+
		"FldR: "+fieldReads+" / FldW: "+fieldWrites+" / "+
		"EleR: "+elementReads+" / EleW: "+elementWrites;
	}
    }
}
