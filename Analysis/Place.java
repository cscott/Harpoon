// Place.java, created Tue Mar  2 13:11:42 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.ReverseIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>Place</code> determines the proper locations for phi/sigma
 * functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Place.java,v 1.10.2.5 1999-04-03 18:02:09 cananian Exp $
 */
public class Place  {
    /*final*/ SESE sese;
    final Map info = new HashMap();
    
    /** Creates a <code>Place</code>. */
    public Place(HCode hc) {
        this.sese = new SESE(hc, true);
	// make RegionInfos for all Regions
	for (Iterator it=sese.topDown(); it.hasNext(); )
	    info.put(it.next(), new RegionInfo());
	// now fill in the def/use data.
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement) it.next();
	    RegionInfo ri = (RegionInfo) info.get(sese.smallestSESE.get(hce));
	    //
	    Temp[] use = ((harpoon.IR.Properties.UseDef) hce).use();
	    Temp[] def = ((harpoon.IR.Properties.UseDef) hce).def();
	    //
	    for (int i=0; i<use.length; i++) {
		ri.useUp.add(use[i]); ri.useDown.add(use[i]);
	    }
	    for (int i=0; i<def.length; i++) {
		ri.defUp.add(def[i]); ri.defDown.add(def[i]);
	    }
	}
	// smear down.
	for (Iterator it=sese.topDown(); it.hasNext(); ) {
	    SESE.Region r = (SESE.Region) it.next();
	    RegionInfo ri = (RegionInfo) info.get(r);
	    for (Iterator it2=r.children().iterator(); it2.hasNext(); ) {
		RegionInfo riC = (RegionInfo) info.get(it2.next());
		riC.useDown.addAll(ri.useDown);
		riC.defDown.addAll(ri.defDown);
	    }
	}
	// smear up
	for (Iterator it=new ReverseIterator(sese.topDown()); it.hasNext(); ) {
	    SESE.Region r = (SESE.Region) it.next();
	    RegionInfo ri = (RegionInfo) info.get(r);
	    if (r.parent()==null) continue; // top-level.
	    RegionInfo riP= (RegionInfo) info.get(r.parent());
	    riP.useUp.addAll(ri.useUp);
	    riP.defUp.addAll(ri.defUp);
	}
    }
    private Temp[] needed(HCodeElement n) {
	SESE.Region r = (SESE.Region) sese.smallestSESE.get(n);
	RegionInfo ri = (RegionInfo) info.get(r);
	Set s = new HashSet(); s.addAll(ri.useUp); s.addAll(ri.defUp);
	/* THIS IS AN OPTIMIZATION THAT DOESN'T QUITE WORK.
	if (r.parent()!=null) {
	    RegionInfo riP = (RegionInfo) info.get(r.parent());
	    // remove use-before-def at entry phi.
	    if (((HCodeEdge)r.entry).to().equals(n))
		s.retainAll(riP.defDown);
	    // remove unused-def at exit sigma
	    if (((HCodeEdge)r.exit).from().equals(n))
		s.retainAll(riP.useDown);
	}
	*/
	return (Temp[]) s.toArray(new Temp[s.size()]);
    }
    public Temp[] phiNeeded(HCodeElement n) { return needed(n); }
    public Temp[] sigNeeded(HCodeElement n) { return needed(n); }

    private static class RegionInfo {
	final Set useUp = new HashSet(7), useDown = new HashSet(7);
	final Set defUp = new HashSet(7), defDown = new HashSet(7);
	public String toString() {
	    return "< "+ 
		"useUp:"+useUp+" | useDown:"+useDown+" | "+
		"defUp:"+defUp+" | defDown:"+defDown+
		" >";
	}
    }
}
