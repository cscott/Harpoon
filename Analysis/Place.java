// Place.java, created Mon Sep 14 23:41:52 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Temp.Temp;
import harpoon.Util.Worklist;
import harpoon.Util.UniqueFIFO;

import java.util.Hashtable;
/**
 * <code>Place</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Place.java,v 1.3 1998-09-15 21:38:07 cananian Exp $
 */

public class Place  {
    boolean isPost;
    UseDef usedef; // null indicates create-on-demand and release asap.
    DomFrontier df; // null indicates create-on-demand and release asap.

    /** Creates a <code>Place</code>. */
    public Place(boolean isPost) {
	this.usedef = null;
	this.df = null;
	this.isPost = isPost;
    }
    public Place(UseDef usedef, DomFrontier df) {
	this.usedef = usedef;
	this.df = df;
	this.isPost = df.isPost;
    }
    SetHTable Aphi = new SetHTable();

    public Temp[] neededFunc(HCode hc, HCodeElement n) {
	analyze(hc); return Aphi.getSet(n);
    }

    Hashtable analyzed = new Hashtable();
    HCode lastHCode = null;
    void analyze(HCode hc) {
	if (hc == lastHCode) return; // quick exit for common case.
	if (analyzed.get(hc)==null) {
	    boolean tempObjects = (usedef==null && df==null);
	    if (tempObjects) { // allocate analysis objects.
		usedef = new UseDef(); df = new DomFrontier(isPost);
	    }
	    placePhi(hc);
	    if (tempObjects) { // free analysis objects.
		usedef = null; df = null;
	    }
	    analyzed.put(hc, hc);
	    lastHCode = hc;
	}
    }

    void placePhi(HCode hc) {
	// for each defined variable a
	Temp[] al = (!isPost) ? usedef.allDefs(hc) : usedef.allUses(hc);
	for (int i=0; i < al.length; i++) {
	    Temp a = al[i];

	    Worklist W = new UniqueFIFO();
	    // W <- defsites[a]
	    HCodeElement el[] = 
		(!isPost) ? usedef.defMap(hc, a) : usedef.useMap(hc, a);
	    for (int j=0; j < el.length; j++)
		W.push(el[j]);

	    while (!W.isEmpty()) {
		HCodeElement n = (HCodeElement) W.pull();
		// for each Y in DF[n]
		HCodeElement[] Yl = df.DF(hc, n);
		for (int j=0; j < Yl.length; j++) {
		    HCodeElement Y = Yl[j];
		    if (!Aphi.memberSet(Y, a)) {
			Aphi.unionSet(Y, a);
			// if a not in Aorig[Y] then W = W union { Y }
			harpoon.IR.Properties.UseDef ud = 
			    (harpoon.IR.Properties.UseDef) Y;
			Temp[] Aorig = (!isPost) ? ud.def() : ud.use() ;
			int k; for (k=0; k < Aorig.length; k++)
			    if (Aorig[k] == a) break;
			if (k == Aorig.length) // a not in Aorig[Y]
			    W.push(Y);
		    }
		}
	    }
	}
    }

    static class SetHTable extends Hashtable {
	void clearSet(HCodeElement hce) {
	    remove(hce);
	}
	Temp[] getSet(HCodeElement hce) {
	    if (!containsKey(hce)) return new Temp[0];
	    return (Temp[]) get(hce);
	}
	boolean memberSet(HCodeElement hce, Temp t) {
	    Temp[] set = getSet(hce);
	    for (int i=0; i < set.length; i++)
		if (set[i] == t) return true;
	    return false;
	}
	void unionSet(HCodeElement hce, Temp Tnew) {
	    if (!containsKey(hce)) {
		put(hce, new Temp[] { Tnew });
	    } else {
		Temp[] oldset = (Temp[]) get(hce);
		Temp[] newset = new Temp[oldset.length+1];
		for (int i=0; i < oldset.length; i++)
		    if (oldset[i] == Tnew)
			return; // don't add; already present.
		    else
			newset[i] = oldset[i];
		newset[oldset.length] = Tnew;
		put(hce, newset);
	    }
	}
    }
}
