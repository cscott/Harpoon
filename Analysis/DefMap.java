// DefMap.java, created Fri Jan 29 17:16:38 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;
import harpoon.IR.Properties.UseDef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * <code>DefMap</code> maps <code>Temp</code>s to the
 * <code>HCodeElement</code>s which define them.  The
 * <code>DefMap</code> caches its results, so you should throw away
 * the current <code>DefMap</code> object and make another one if you
 * make modifications to the IR for the <code>HCode</code> associated
 * with it. 
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: DefMap.java,v 1.1.2.5 2000-01-17 11:10:00 cananian Exp $
 */
public class DefMap extends TempToHceArrayMap {

    private Temp[] allDefs;

    /** Creates a <code>DefMap</code> for <code>hc</code>. 
	<BR> <B>requires:</B> <code>hc</code>'s internal
	                      representation implements
			      <code>harpoon.IR.Properties.UseDef</code>. 
	<BR> <B>effects:</B> creates a <code>DefMap</code> for
	                     <code>hc</code>, performing the necessary
			     analysis during construction.
    */
    public DefMap( HCode hc ) {
	super(hc);
	analyze(hc.getElements());
    }
    
    /* Helper method for analysis of <code>this.hcode</code> during
       construction.  
       <BR> <B>requires:</B> <code>hces</code> instanceof
                             <code>harpoon.IR.Properties.UseDef[]</code>.
       <BR> <B>effects:</B> performs Variable->Def analysis on
                            the <code>HCode</code> associated with
			    <code>this</code>.
    */
    private void analyze(HCodeElement[] hces) {
	Util.assert(hces instanceof harpoon.IR.Properties.UseDef[],
		    "HCodeElement array in DefMap must implement UseDef.");
	harpoon.IR.Properties.UseDef[] udl = 
	    (harpoon.IR.Properties.UseDef[]) hces;

	Map tmpDef = new HashMap();

	// scan HCodeElements, associating defs with their
	// HCodeElement. 
	for(int i=0; i<hces.length; i++) {
	    associate( hces[i], udl[i].def(), tmpDef);
	}

	// Store the final set in the main map.
	Iterator it=tmpDef.keySet().iterator();
	Vector defs = new Vector();
	while ( it.hasNext() ) {
	    Temp def = (Temp) it.next();
 	    storeTempMapping(def, (Set) tmpDef.get(def));
	    defs.addElement(def);
	}
	allDefs = (Temp[]) Temp.arrayFactory.newArray(defs.size()); 
	defs.copyInto( allDefs );
    }
    
    /** Return the <code>HCodeElement</code>s which define a given
	<code>Temp</code> in the <code>HCode</code> associated with
	<code>this</code>.
	<BR> <B>effects:</B> Returns an <code>HCodeElement</code>
	                     array of all definitions of
			     <code>t</code>. 
    */
    public HCodeElement[] defMap(Temp t) {
	HCodeElement[] r = extractTempMapping(t);
	return (r == null) ? 
	    (HCodeElement[]) arrayFact.newArray(0) :
	    (HCodeElement[]) Util.safeCopy(arrayFact, r);
    }
    
    /** Returns an array of all <code>Temp</code> defined in the
	<code>HCode</code> associated with <code>this</code>.  
    */
    public Temp[] allDefs() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, allDefs);
    }

}



