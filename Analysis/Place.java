// Place.java, created Mon Sep 14 23:41:52 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Temp.Temp;
import harpoon.Util.Worklist;
import harpoon.Util.NullEnumerator;
import harpoon.Util.Set;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>Place</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Place.java,v 1.9 1998-09-18 00:50:28 cananian Exp $
 */

public class Place  {
    UseDef ud; // null indicates create-on-demand and release asap.
    DomFrontier df; // null indicates create-on-demand and release asap.
    DomFrontier pdf; // null indicates create-on-demand and release asap.

    /** Creates a <code>Place</code>. */
    public Place(UseDef usedef, DomFrontier df, DomFrontier pdf) {
	this.ud = usedef;
	this.df = df;
	this.pdf = pdf;
    }
    public Place() {
	this(null, null, null);
    }
    SetHTable Aphi = new SetHTable();
    SetHTable Asig = new SetHTable();

    public Temp[] phiNeeded(HCode hc, HCodeElement n) {
	analyze(hc); return Aphi.getSet(n);
    }
    public Enumeration phiNeededE(HCode hc, HCodeElement n) {
	analyze(hc); return Aphi.getSetE(n);
    }
    public Temp[] sigNeeded(HCode hc, HCodeElement n) {
	analyze(hc); return Asig.getSet(n);
    }
    public Enumeration sigNeededE(HCode hc, HCodeElement n) {
	analyze(hc); return Asig.getSetE(n);
    }

    Hashtable analyzed = new Hashtable();
    HCode lastHCode = null;
    void analyze(HCode hc) {
	if (hc == lastHCode) return; // quick exit for common case.
	if (analyzed.get(hc)==null) {
	    boolean tempUse = (ud == null);
	    boolean tempDF  = (df == null);
	    boolean tempPDF = (pdf== null);
	    if (tempUse) ud  = new UseDef();
	    if (tempDF ) df  = new DomFrontier(false);
	    if (tempPDF) pdf = new DomFrontier(true);

	    place(hc);

	    if (tempUse)  ud  = null; // free analysis objects.
	    if (tempDF )  df  = null;
	    if (tempPDF ) pdf = null;

	    analyzed.put(hc, hc);
	    lastHCode = hc;
	}
    }

    void place(HCode hc) {
	Worklist Wphi = new Set();
	Worklist Wsig = new Set();

	// for each used/defined variable a
	for (Enumeration aE = ud.allTempsE(hc); aE.hasMoreElements(); ) {
	    Temp a = (Temp) aE.nextElement();

	    // worklists are empty here.
	    Util.assert(Wphi.isEmpty() && Wsig.isEmpty());

	    // Wphi <- defsites[a]
	    for (Enumeration e = ud.defMapE(hc, a); e.hasMoreElements(); )
		Wphi.push(e.nextElement());
	    // Wsig <- usesites[a]
	    for (Enumeration e = ud.useMapE(hc, a); e.hasMoreElements(); )
		Wsig.push(e.nextElement());

	    while ( ! ( Wphi.isEmpty() && Wsig.isEmpty() ) )  {
		if (!Wphi.isEmpty()) {
		    // remove some node n from Wphi
		    HCodeElement n = (HCodeElement) Wphi.pull();
		    // for each Y in DF[n]
		    for (Enumeration yE=df.dfE(hc, n); 
			 yE.hasMoreElements(); ) {
			HCodeElement Y = (HCodeElement) yE.nextElement();
			if (!Aphi.memberSet(Y, a)) {
			    Aphi.unionSet(Y, a);
			    update(a, Y, Wphi, Wsig);
			}
		    }
		} // end Wphi processing.
		if (!Wsig.isEmpty()) {
		    // remove some node n from Wsig
		    HCodeElement n = (HCodeElement) Wsig.pull();
		    // for each Y in PDF[n]
		    for (Enumeration yE = pdf.dfE(hc, n); 
			 yE.hasMoreElements(); ) {
			HCodeElement Y = (HCodeElement) yE.nextElement();
			if (!Asig.memberSet(Y, a)) {
			    Asig.unionSet(Y, a);
			    update(a, Y, Wphi, Wsig);
			}
		    }
		} // end Wsig processing.
	    } // end while.
	} // end "for all variables a"
    } // end place.

    // determine whether we need to add n to Wphi or Wsig.
    private static void update(Temp a, HCodeElement Y, 
			       Worklist Wphi, Worklist Wsig)
    {
	harpoon.IR.Properties.UseDef Yud =
	    (harpoon.IR.Properties.UseDef) Y; // access this property.
	int i;
	// Get Aorig_def and Aorig_use (pre-phi/sig uses and defs of Y)
	Temp[] Aorig_def = Yud.def();
	Temp[] Aorig_use = Yud.use();
	// if a not in Aorig_def[Y] then Wphi = Wphi union { Y }
	for (i = 0; i < Aorig_def.length; i++)
	    if (Aorig_def[i] == a)
		break;
	if (i == Aorig_def.length) // a not in Aorig_def[Y]
	    Wphi.push(Y);
	// if a not in Aorig_use[Y] then Wsig = Wsig union { Y }
	for (i = 0; i < Aorig_use.length; i++)
	    if (Aorig_use[i] == a)
		break;
	if (i == Aorig_use.length) // a not in Aorig_use[Y]
	    Wsig.push(Y);
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
	Enumeration getSetE(HCodeElement hce) {
	    Set s = (Set) get(hce);
	    if (s==null)
		return NullEnumerator.STATIC;
	    else
		return s.elements();
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
