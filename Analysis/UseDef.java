// UseDef.java, created Thu Sep 10 15:17:10 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Default;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>UseDef</code> objects map <code>Temp</code>s to the 
 * <code>HCodeElement</code>s which use or define
 * them.  The <code>UseDef</code> caches its results, so you should 
 * throw away your current <code>UseDef</code> object and make 
 * another one if you make modifications to the IR.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDef.java,v 1.12 2002-04-10 02:58:48 cananian Exp $
 */

public class UseDef implements harpoon.Analysis.Maps.UseDefMap {
    /** Creates a new, empty <code>UseDef</code>. */
    public UseDef() { }

    Map analyzed = new HashMap();
    Map useMap = new HashMap();
    Map defMap = new HashMap();

    static class allTempList {
	Temp[] used;
	Temp[] defined;
	Temp[] all;
	allTempList(Temp[] used, Temp[] defined, Temp[] all) {
	    this.used = used; this.defined = defined; this.all = all;
	}
    }
	
    void associate(HCodeElement hce, Temp[] tl,
		   Map map, Set allTemps1, Set allTemps2) {
	for (int i=0; i<tl.length; i++) {
	    Set s = (Set) map.get(tl[i]);
	    if (s==null) { s = new HashSet(); map.put(tl[i], s); }
	    s.add(hce);
	    allTemps1.add(tl[i]);
	    allTemps2.add(tl[i]);
	}
    }

    Temp[] set2temps(Set s) {
	return (Temp[]) s.toArray(new Temp[s.size()]);
    }
    HCodeElement[] set2hces(HCode hc, Set s) {
	return (HCodeElement[])
	    s.toArray(hc.elementArrayFactory().newArray(s.size()));
    }

    HCode lastHCode = null;
    void analyze(HCode code) {
	// make sure we don't analyze an HCode multiple times.
	if (code==lastHCode) return; // we just did this one.
	if (analyzed.containsKey(code)) return; // check the hash table.
	lastHCode = code;

	HCodeElement[] el = code.getElements();
	if (!(el instanceof harpoon.IR.Properties.UseDefable[]))
	    throw new Error(code.getName() + " does not implement UseDefable");
	harpoon.IR.Properties.UseDefable[] udl =
	    (harpoon.IR.Properties.UseDefable[]) el;

	Map workUse = new HashMap();
	Map workDef = new HashMap();
	Set defined = new HashSet();
	Set used    = new HashSet();
	Set all     = new HashSet();

	// Scan through and associate uses and defs with their HCodeElements
	for (int i=0; i<el.length; i++) {
	    associate(el[i], udl[i].use(), workUse, used, all);
	    associate(el[i], udl[i].def(), workDef, defined, all);
	}
	// replace UniqueVectors with HCodeElement arrays to save space.
	for (Iterator it = workUse.keySet().iterator(); it.hasNext(); ) {
	    Temp u = (Temp) it.next();
	    useMap.put(u, set2hces(code,  (Set)workUse.get(u)));
	}
	for (Iterator it = workDef.keySet().iterator(); it.hasNext(); ) {
	    Temp d = (Temp) it.next();
	    defMap.put(d, set2hces(code, (Set)workDef.get(d)));
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
	    (HCodeElement[]) hc.elementArrayFactory().newArray(0) :
	    (HCodeElement[]) Util.safeCopy(hc.elementArrayFactory(), r);
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
	    (HCodeElement[]) hc.elementArrayFactory().newArray(0) :
	    (HCodeElement[]) Util.safeCopy(hc.elementArrayFactory(), r);
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
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, atl.defined);
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
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, atl.used);
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
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, atl.all);
    }
    /** Return an Enumeration of all Temps used or defined in a given HCode. */
    public Enumeration allTempsE(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return arrayEnumerator(atl.all);
    }
	
    private Enumeration arrayEnumerator(Object[] ol) {
	return new IteratorEnumerator(arrayIterator(ol));
    }
    private Iterator arrayIterator(Object[] ol) {
	if (ol==null) return Default.nullIterator;
	return new ArrayIterator(ol);
    }
}
