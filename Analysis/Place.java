// Place.java, created Mon Jan 31 08:30:07 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDefable;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.MultiMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>Place</code> determines the proper locations for phi/sigma
 * functions.  This is the placement algorithm detailed in my
 * thesis.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Place.java,v 1.10.2.14 2001-11-08 00:21:12 cananian Exp $
 */
public class Place {
    private final MultiMap phis;
    private final MultiMap sigmas;
    
    /** Creates a <code>Place</code>. */
    public Place(HCode hc, Liveness live) {
	// create SESE
	SESE sese = new SESE(hc);
	// collect all vars
        Set vars = new WorkSet();
	for (Iterator it=hc.getElementsI(); it.hasNext(); )
	    vars.addAll(((UseDefable)it.next()).defC());
	// create result multimaps
	phis = new GenericMultiMap(new BitSetFactory(vars));
	sigmas = new GenericMultiMap(new BitSetFactory(vars));
	// for each variable v in G, do:
	for (Iterator it=vars.iterator(); it.hasNext(); ) {
	    Temp v = (Temp) it.next();
	    PlaceOne(sese.topLevel, v, live); // place phis and sigmas
	}
    }
    private boolean PlaceOne(SESE.Region r, Temp v, Liveness live)
    {
	// post-order traversal.
	boolean flag = false;
	for (Iterator it=r.children().iterator(); it.hasNext(); )
	    if (PlaceOne((SESE.Region)it.next(), v, live))
		flag = true;
	for (Iterator it=r.nodes().iterator(); !flag && it.hasNext(); ) {
	    UseDefable n = (UseDefable) it.next();
	    // we need phis for 'use-only' vars because the sigma is
	    // going to create a definition for them.  Likewise we
	    // need sigmas for 'def-only' vars. [CSA]
	    if (n.defC().contains(v) || n.useC().contains(v))
		flag = true;
	}

	// add phis/sigmas to merges/splits where v may be live
	if (flag) {
	    for (Iterator it=r.nodes().iterator(); it.hasNext(); ) {
		HCodeElement n = (HCodeElement) it.next();
		if (((CFGraphable)n).predC().size() > 1 && 
		    live.getLiveIn(n).contains(v))
		    phis.add(n, v);
		if (((CFGraphable)n).succC().size() > 1 &&
		    live.getLiveIn(n).contains(v))
		    sigmas.add(n, v);
	    }
	}
	return flag;
    }

    public Temp[] phiNeeded(HCodeElement n) {
	Collection c = phis.getValues(n);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }
    public Temp[] sigNeeded(HCodeElement n) {
	Collection c = sigmas.getValues(n);
	return (Temp[]) c.toArray(new Temp[c.size()]);
    }
}
