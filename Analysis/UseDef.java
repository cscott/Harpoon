// UseDef.java, created Thu Sep 10 15:17:10 1998 by cananian
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Util.Set;
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
 * @version $Id: UseDef.java,v 1.8 1998-09-16 00:42:21 cananian Exp $
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
	allTempList(Temp[] used, Temp[] defined) {
	    this.used = used; this.defined = defined;
	}
    }
	
    void associate(HCodeElement hce, Temp[] tl,
		   Hashtable map, Set allTemps) {
	for (int i=0; i<tl.length; i++) {
	    Set s = (Set) map.get(tl[i]);
	    if (s==null) { s = new Set(); map.put(tl[i], s); }
	    s.union(hce);
	    allTemps.union(tl[i]);
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

	// Scan through and associate uses and defs with their HCodeElements
	for (int i=0; i<el.length; i++) {
	    associate(el[i], udl[i].use(), workUse, used);
	    associate(el[i], udl[i].def(), workDef, defined);
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
					   set2temps(defined)));
    }

    /** Return the HCodeElements which define a given Temp. */
    public HCodeElement[] defMap(HCode hc, Temp t) {
	analyze(hc);
	HCodeElement[] r = (HCodeElement[]) defMap.get(t);
	return (r == null) ? 
	    new HCodeElement[0] : 
	    (HCodeElement[]) Util.copy(r);
    }
    /** Return the HCodeElements which use a given Temp. */
    public HCodeElement[] useMap(HCode hc, Temp t) {
	analyze(hc);
	HCodeElement[] r = (HCodeElement[]) useMap.get(t);
	return (r == null) ? 
	    new HCodeElement[0] : 
	    (HCodeElement[]) Util.copy(r);
    }
    /** Return an array of all Temps defined in a given HCode. */
    public Temp[] allDefs(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return (Temp[]) Util.copy(atl.defined);
    }
    /** Return an array of all Temps used in a given HCode. */
    public Temp[] allUses(HCode hc) {
	analyze(hc);
	allTempList atl = (allTempList) analyzed.get(hc);
	return (Temp[]) Util.copy(atl.used);
    }
}
