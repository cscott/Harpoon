// AnalysisCheckOracle.java, created Tue Jan 16 12:12:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.*;
import harpoon.Util.Collections.*;

import java.util.*;
/**
 * An <code>AnalysisCheckOracle</code> is used when one wants to
 * do some analysis and store the results of the check oracle.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AnalysisCheckOracle.java,v 1.1.2.1 2001-01-16 18:42:11 cananian Exp $
 */
abstract class AnalysisCheckOracle extends CheckOracle {
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
    
    /** Creates a <code>AnalysisCheckOracle</code>. */
    AnalysisCheckOracle() {
	/* Initialize backing data stores */
	SetFactory sf = new AggregateSetFactory();
	readVMap = new GenericMultiMap(sf);
	writeVMap = new GenericMultiMap(sf);
	checkFMap = new GenericMultiMap(sf);
	checkEMap = new GenericMultiMap(sf);
    }

    static class CheckSet {
	private static final SetFactory sf = new AggregateSetFactory();

	final Set/*<Temp>*/ readVersions = sf.makeSet();
	final Set/*<Temp>*/ writeVersions = sf.makeSet();
	final Set/*<RefAndField>*/ fields = sf.makeSet();
	final Set/*<RefAndIndexAndType>*/ elements = sf.makeSet();
	
	CheckSet() { /* new empty set */ }
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
    }
}
