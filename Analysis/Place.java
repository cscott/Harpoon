// Place.java, created Mon Jan 31 08:30:07 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDef;
import harpoon.Temp.Temp;
import harpoon.Util.WorkSet;
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
 * @version $Id: Place.java,v 1.10.2.8 2000-01-31 21:53:49 cananian Exp $
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
	    vars.addAll(((UseDef)it.next()).defC());
	// create result multimaps
	phis = new GenericMultiMap(new BitSetFactory(vars),
				   Factories.hashMapFactory());
	sigmas = new GenericMultiMap(new BitSetFactory(vars),
				   Factories.hashMapFactory());
	// for each variable v in G, do:
	for (Iterator it=vars.iterator(); it.hasNext(); ) {
	    Temp v = (Temp) it.next();
	    PlaceOne(sese.topLevel, v, false, live); // place phis
	    PlaceOne(sese.topLevel, v, true, live); // place sigmas
	}
    }
    private boolean PlaceOne(SESE.Region r, Temp v, boolean ps, Liveness live)
    {
	// post-order traversal.
	boolean flag = false;
	for (Iterator it=r.children().iterator(); it.hasNext(); )
	    if (PlaceOne((SESE.Region)it.next(), v, ps, live))
		flag = true;
	for (Iterator it=r.nodes().iterator(); !flag && it.hasNext(); ) {
	    UseDef n = (UseDef) it.next();
	    if (ps==false && n.defC().contains(v))
		flag = true;
	    if (ps==true  && n.useC().contains(v))
		flag = true;
	}

	// add phis/sigmas to merges/splits where v may be live
	if (flag) {
	    for (Iterator it=r.nodes().iterator(); it.hasNext(); ) {
		HCodeElement n = (HCodeElement) it.next();
		if (ps==false && ((CFGraphable)n).predC().size() > 1 && 
		    live.getLiveIn(n).contains(v))
		    phis.add(n, v);
		if (ps==true && ((CFGraphable)n).succC().size() > 1 &&
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
