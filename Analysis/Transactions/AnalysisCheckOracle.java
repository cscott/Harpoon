// AnalysisCheckOracle.java, created Tue Jan 16 12:12:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Collections.*;

import java.util.*;

/**
 * An <code>AnalysisCheckOracle</code> is used when one wants to
 * do some analysis and store the results of the check oracle.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AnalysisCheckOracle.java,v 1.1.2.3 2001-01-26 21:37:49 cananian Exp $
 */
abstract class AnalysisCheckOracle extends CheckOracle {
    final Map results = new HashMap();
    public Set createReadVersions(HCodeElement hce) {
	return ((CheckSet) results.get(hce)).readVersions;
    }
    public Set createWriteVersions(HCodeElement hce) {
	return ((CheckSet) results.get(hce)).writeVersions;
    }
    public Set checkField(HCodeElement hce) {
	return ((CheckSet) results.get(hce)).fields;
    }
    public Set checkArrayElement(HCodeElement hce) {
	return ((CheckSet) results.get(hce)).elements;
    }
    
    /** Creates a <code>AnalysisCheckOracle</code>. */
    AnalysisCheckOracle() { /* nothing */ }

    private final SetFactory sf = new AggregateSetFactory();

    class CheckSet {
	final Set/*<Temp>*/ readVersions = sf.makeSet();
	final Set/*<Temp>*/ writeVersions = sf.makeSet();
	final Set/*<RefAndField>*/ fields = sf.makeSet();
	final Set/*<RefAndIndexAndType>*/ elements = sf.makeSet();
	
	CheckSet() { /* new empty set */ }
	CheckSet(CheckSet cs) { // new set w/ contents of given set.
	    this.readVersions.addAll(cs.readVersions);
	    this.writeVersions.addAll(cs.writeVersions);
	    this.fields.addAll(cs.fields);
	    this.elements.addAll(cs.elements);
	}
	CheckSet(CheckOracle co, HCodeElement hce) {
	    this.readVersions.addAll(co.createReadVersions(hce));
	    this.writeVersions.addAll(co.createWriteVersions(hce));
	    this.fields.addAll(co.checkField(hce));
	    this.elements.addAll(co.checkArrayElement(hce));
	}
	void addAll(CheckSet cs) {
	    this.readVersions.addAll(cs.readVersions);
	    this.writeVersions.addAll(cs.writeVersions);
	    this.fields.addAll(cs.fields);
	    this.elements.addAll(cs.elements);
	}
	void removeAll(CheckSet cs) {
	    this.readVersions.removeAll(cs.readVersions);
	    this.writeVersions.removeAll(cs.writeVersions);
	    this.fields.removeAll(cs.fields);
	    this.elements.removeAll(cs.elements);
	}
	void clear() {
	    this.readVersions.clear();
	    this.writeVersions.clear();
	    this.fields.clear();
	    this.elements.clear();
	}
	public Object clone() { return new CheckSet(this); }
	public String toString() {
	    return "Rd: "+readVersions+" / Wr: "+writeVersions+" / "+
		"Fld: "+fields+" / Ele: "+elements;
	}
    }
}
