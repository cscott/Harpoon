// Place.java, created Mon Sep 14 23:41:52 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Temp.Temp;
import harpoon.Util.Worklist;
import harpoon.Util.Set;

import java.util.Hashtable;
/**
 * <code>Place</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Place.java,v 1.5 1998-09-16 01:00:06 cananian Exp $
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
    public Place(UseDef usedef, boolean isPost) {
	this.usedef = usedef;
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
	    boolean tempUse = (usedef == null);
	    boolean tempDF  = (df == null);
	    if (tempUse) usedef = new UseDef();
	    if (tempDF ) df     = new DomFrontier(isPost);

	    placePhi(hc);

	    if (tempUse) usedef = null; // free analysis objects.
	    if (tempDF ) df     = null;

	    analyzed.put(hc, hc);
	    lastHCode = hc;
	}
    }

    void placePhi(HCode hc) {
	// for each defined variable a
	Temp[] al = (!isPost) ? usedef.allDefs(hc) : usedef.allUses(hc);
	for (int i=0; i < al.length; i++) {
	    Temp a = al[i];

	    Worklist W = new Set();
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
	    Set s = (Set) get(hce);
	    if (s==null) return new Temp[0];
	    Temp[] r = new Temp[s.size()];
	    s.copyInto(r);
	    return r;
	}
	boolean memberSet(HCodeElement hce, Temp t) {
	    Set s = (Set) get(hce);
	    if (s==null) return false;
	    return s.contains(t);
	}
	void unionSet(HCodeElement hce, Temp Tnew) {
	    Set s = (Set) get(hce);
	    if (s == null) {
		s = new Set();
		put(hce, s);
	    }
	    s.union(Tnew);
	}
    }
}
