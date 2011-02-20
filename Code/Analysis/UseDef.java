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
 * @version $Id: UseDef.java,v 1.13 2003-07-11 09:38:37 cananian Exp $
 */

public class UseDef<HCE extends HCodeElement>
    implements harpoon.Analysis.Maps.UseDefMap<HCE> {
    /** Creates a new, empty <code>UseDef</code>. */
    public UseDef() { }

    Map<HCode<HCE>,allTempList> analyzed =
	new HashMap<HCode<HCE>,allTempList>();
    Map<Temp,HCE[]> useMap = new HashMap<Temp,HCE[]>();
    Map<Temp,HCE[]> defMap = new HashMap<Temp,HCE[]>();

    static class allTempList {
	Temp[] used;
	Temp[] defined;
	Temp[] all;
	allTempList(Temp[] used, Temp[] defined, Temp[] all) {
	    this.used = used; this.defined = defined; this.all = all;
	}
    }
	
    void associate(HCE hce, Temp[] tl,
		   Map<Temp,Set<HCE>> map,
		   Set<Temp> allTemps1, Set<Temp> allTemps2) {
	for (int i=0; i<tl.length; i++) {
	    Set<HCE> s = map.get(tl[i]);
	    if (s==null) { s = new HashSet<HCE>(); map.put(tl[i], s); }
	    s.add(hce);
	    allTemps1.add(tl[i]);
	    allTemps2.add(tl[i]);
	}
    }

    Temp[] set2temps(Set<Temp> s) {
	return s.toArray(new Temp[s.size()]);
    }
    HCE[] set2hces(HCode<HCE> hc, Set<HCE> s) {
	return s.toArray(hc.elementArrayFactory().newArray(s.size()));
    }

    HCode<HCE> lastHCode = null;
    void analyze(HCode<HCE> code) {
	// make sure we don't analyze an HCode multiple times.
	if (code==lastHCode) return; // we just did this one.
	if (analyzed.containsKey(code)) return; // check the hash table.
	lastHCode = code;

	HCE[] el = code.getElements();
	if (!(el instanceof harpoon.IR.Properties.UseDefable[]))
	    throw new Error(code.getName() + " does not implement UseDefable");
	harpoon.IR.Properties.UseDefable[] udl =
	    (harpoon.IR.Properties.UseDefable[]) el;

	Map<Temp,Set<HCE>> workUse = new HashMap<Temp,Set<HCE>>();
	Map<Temp,Set<HCE>> workDef = new HashMap<Temp,Set<HCE>>();
	Set<Temp> defined = new HashSet<Temp>();
	Set<Temp> used    = new HashSet<Temp>();
	Set<Temp> all     = new HashSet<Temp>();

	// Scan through and associate uses and defs with their HCodeElements
	for (int i=0; i<el.length; i++) {
	    associate(el[i], udl[i].use(), workUse, used, all);
	    associate(el[i], udl[i].def(), workDef, defined, all);
	}
	// replace UniqueVectors with HCodeElement arrays to save space.
	for (Iterator<Temp> it = workUse.keySet().iterator(); it.hasNext(); ) {
	    Temp u = it.next();
	    useMap.put(u, set2hces(code,  workUse.get(u)));
	}
	for (Iterator<Temp> it = workDef.keySet().iterator(); it.hasNext(); ) {
	    Temp d = it.next();
	    defMap.put(d, set2hces(code, workDef.get(d)));
	}
	// store set of all temps & mark as analyzed.
	analyzed.put(code, new allTempList(set2temps(used),
					   set2temps(defined),
					   set2temps(all)));
    }

    /** Return the HCodeElements which define a given Temp. */
    public HCE[] defMap(HCode<HCE> hc, Temp t) {
	analyze(hc);
	HCE[] r = defMap.get(t);
	return (r == null) ? 
	    hc.elementArrayFactory().newArray(0) :
	    Util.safeCopy(hc.elementArrayFactory(), r);
    }
    /** Return the HCodeElements which use a given Temp. */
    public HCE[] useMap(HCode<HCE> hc, Temp t) {
	analyze(hc);
	HCE[] r = useMap.get(t);
	return (r == null) ? 
	    hc.elementArrayFactory().newArray(0) :
	    Util.safeCopy(hc.elementArrayFactory(), r);
    }
    /** Return an array of all Temps defined in a given HCode. */
    public Temp[] allDefs(HCode<HCE> hc) {
	analyze(hc);
	allTempList atl = analyzed.get(hc);
	return Util.safeCopy(Temp.arrayFactory, atl.defined);
    }
    /** Return an array of all Temps used in a given HCode. */
    public Temp[] allUses(HCode<HCE> hc) {
	analyze(hc);
	allTempList atl = analyzed.get(hc);
	return Util.safeCopy(Temp.arrayFactory, atl.used);
    }
    /** Return an array of all Temps used or defined in a given HCode. */
    public Temp[] allTemps(HCode<HCE> hc) {
	analyze(hc);
	allTempList atl = analyzed.get(hc);
	return Util.safeCopy(Temp.arrayFactory, atl.all);
    }
}
