// UseDef.java, created Thu Sep 10 15:17:10 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Util.Set;
import harpoon.Util.NullEnumerator;
import harpoon.Util.ArrayEnumerator;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>UseDef</code> objects map <code>Temp</code>s to the 
 * <code>HCodeElement</code>s which use or define
 * them.  The <code>UseDef</code> caches its results, so you should 
 * throw away your current <code>UseDef</code> object and make 
 * another one if you make modifications to the IR.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDef.java,v 1.10 1998-10-11 02:36:59 cananian Exp $
 */

public class UseDef implements harpoon.Analysis.Maps.UseDefMap {
    /** Creates a new, empty <code>UseDef</code>. */
    public UseDef() { }

    Hashtable analyzed = new Hashtable();
    Hashtable useMap = new Hashtable();
    Hashtable defMap = new Hashtable();

    static class allTempList {
	Temp[] used;
	Temp[] defined;
	Temp[] all;
	allTempList(Temp[] used, Temp[] defined, Temp[] all) {
	    this.used = used; this.defined = defined; this.all = all;
	}
    }
	
    void associate(HCodeElement hce, Temp[] tl,
		   Hashtable map, Set allTemps1, Set allTemps2) {
	for (int i=0; i<tl.length; i++) {
	    Set s = (Set) map.get(tl[i]);
	    if (s==null) { s = new Set(); map.put(tl[i], s); }
	    s.union(hce);
	    allTemps1.union(tl[i]);
	    allTemps2.union(tl[i]);
	}
    }

    Temp[] set2temps(Set s) {
	Temp[] tl = new Temp[s.size()];
	s.copyInto(tl);
	return tl;
    }
    HCodeElement[] set2hces(Set s) {
	HCodeElement[] hcel = new HCodeElement[s.size()];
	s.copyInto(hcel);
	return hcel;
    }

    HCode lastHCode = null;
    void analyze(HCode code) {
	// make sure we don't analyze an HCode multiple times.
	if (code==lastHCode) return; // we just did this one.
	if (analyzed.containsKey(code)) return; // check the hash table.
	lastHCode = code;

	HCodeElement[] el = code.getElements();
	if (!(el instanceof harpoon.IR.Properties.UseDef[]))
	    throw new Error(code.getName() + " does not implement UseDef");
	harpoon.IR.Properties.UseDef[] udl =
	    (harpoon.IR.Properties.UseDef[]) el;

	Hashtable workUse = new Hashtable();
	Hashtable workDef = new Hashtable();
	Set defined = new Set();
	Set used    = new Set();
	Set all     = new Set();

	// Scan through and associate uses and defs with their HCodeElements
	for (int i=0; i<el.length; i++) {
	    associate(el[i], udl[i].use(), workUse, used, all);
	    associate(el[i], udl[i].def(), workDef, defined, all);
	}
	// replace UniqueVectors with HCodeElement arrays to save space.
	for (Enumeration e = workUse.keys(); e.hasMoreElements(); ) {
	    Temp u = (Temp) e.nextElement();
	    useMap.put(u, set2hces((Set)workUse.get(u)));
	}
	for (Enumeration e = workDef.keys(); e.hasMoreElements(); ) {
	    Temp d = (Temp) e.nextElement();
	    defMap.put(d, set2hces((Set)workDef.get(d)));
	}
	// store set of all temps & mark as analyzed.
	analyzed.put(code, new allTempList(set2temps(used),
					   set2temps(defined),
					   set2temps(all)));
    }

    /** Return the HCodeElements which define a given Temp. */
    public HCodeElement[] defMap(HCode hc, Temp t) {
	analyze(hc);
	HCodeElement[] r = (HCodeElement[]) defMap.get(t);
	return (r == null) ? 
	    new HCodeElement[0] : 
	    (HCodeElement[]) Util.copy(r);
    }
    /** Enumerate the HCodeElements which define a given Temp. */
    public Enumeration defMapE(HCode hc, Temp t) {
	analyze(hc);
	return arrayEnumerator( (HCodeElement[]) defMap.get(t) );
    }
    /** Return the HCodeElements which use a given Temp. */
    public HCodeElement[] useMap(HCode hc, Temp t) {
	analyze(hc);
	HCodeElement[] r = (HCodeElement[]) useMap.get(t);
	return (r == null) ? 
	    new HCodeElement[0] : 
	    (HCodeElement[]) Util.copy(r);
    }
    /** Enumerate the HCodeElements which use a given Temp. */
    public Enumeration useMapE(HCode hc, Temp t) {
	analyze(hc);
	return arrayEnumerator( (HCodeElement[]) useMap.get(t) );
    }
    /** Return an array of all Temps defined in a given HCode. */
    public Temp[] allDefs(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return (Temp[]) Util.copy(atl.defined);
    }
    /** Return an Enumeration of all Temps defined in a given HCode. */
    public Enumeration allDefsE(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return arrayEnumerator(atl.defined);
    }
    /** Return an array of all Temps used in a given HCode. */
    public Temp[] allUses(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return (Temp[]) Util.copy(atl.used);
    }
    /** Return an Enumeration of all Temps used in a given HCode. */
    public Enumeration allUsesE(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return arrayEnumerator(atl.used);
    }
    /** Return an array of all Temps used or defined in a given HCode. */
    public Temp[] allTemps(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return (Temp[]) Util.copy(atl.all);
    }
    /** Return an Enumeration of all Temps used or defined in a given HCode. */
    public Enumeration allTempsE(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return arrayEnumerator(atl.all);
    }
	
    private Enumeration arrayEnumerator(Object[] ol) {
	if (ol==null) return NullEnumerator.STATIC;
	return new ArrayEnumerator(ol);
    }
}
