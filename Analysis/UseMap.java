// UseMap.java, created Fri Jan 29 20:31:09 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * <code>UseMap</code> maps <code>Temp</code>s to the
 * <code>HCodeElement</code>s which use them.  The <code>UseMap</code>
 * caches its results, so you should throw out the current
 * <code>UseMap</code> object and make another if you make
 * modifications to the IR for the <code>HCode</code> associated with
 * it. 
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: UseMap.java,v 1.1.2.4 2000-01-17 11:10:00 cananian Exp $
 */
public class UseMap extends TempToHceArrayMap {
    
    private Temp[] allUses;
     
    /** Creates a <code>UseMap</code> for <code>hc</code>. 
	<BR> <B>requires:</B> <code>hc</code>'s internal
	                      representation implements
			      <code>harpoon.IR.Properties.UseDef</code>. 
	<BR> <B>effects:</B> creates a <code>UseMap</code> for
	                     <code>hc</code>, performing the necessary
			     analysis during construction.
    */
    public UseMap( HCode hc ) {
        super(hc.elementArrayFactory());
	analyze(hc.getElements());
    }
    
    /** Creates a <code>UseMap</code> for <code>hc</code>.
	
	<BR> NOTE: look into adding <code>getFactory</code> method to
	the <code>HCodeElement</code> interface, so that I can get rid
	of the second requirement.
	
	<BR> <B>requires:</B> <code>hces</code> is an instance of
	                      <code>harpoon.IR.Properties.UseDef[]</code>,
			      <code>arrayFact</code> produces
			      arrays typed to store objects of the
			      type stored in <code>hces</code>.
			      
	<BR> <B>effects:</B> creates a <code>UseMap</code> for
	                     <code>hces</code>, performing the necessary
			     analysis during construction.			     
    */
    public UseMap( ArrayFactory arrayFact, HCodeElement[] hces ) {
        super(arrayFact);
	analyze(hces);
    }

    
    /* Helper method for analysis of <code>this.hcode</code> during
       construction.  
       <BR> <B>requires:</B> <code>hces</code> is an instance of 
	                     <code>harpoon.IR.Properties.UseDef[]</code>. 
       <BR> <B>effects:</B> performs Variable->Use analysis on
                            the <code>hces</code>.
    */
    private void analyze(HCodeElement[] hces) {
	Util.assert(hces instanceof harpoon.IR.Properties.UseDef[],
		    "HCodeElement array in UseMap must implement UseDef.");
	harpoon.IR.Properties.UseDef[] udl = 
	    (harpoon.IR.Properties.UseDef[]) hces;

	Map tmpUse = new HashMap();

	// scan HCodeElements, associating uses with their
	// HCodeElement. 
	for(int i=0; i<hces.length; i++) {
	    associate( hces[i], udl[i].use(), tmpUse);
	}

	// Store the final set in the main map.
	Iterator it=tmpUse.keySet().iterator();
	Vector uses = new Vector();
	while ( it.hasNext() ) {
	    Temp use = (Temp) it.next();
 	    storeTempMapping(use, (Set) tmpUse.get(use));
	    uses.addElement(use);
	}
	allUses = new Temp[uses.size()];
	uses.copyInto( allUses );
    }
        
    /** Return the <code>HCodeElement</code>s which use a given
	<code>Temp</code> in the <code>HCode</code> associated with
	<code>this</code>.
	<BR> <B>effects:</B> Returns an <code>HCodeElement</code>
	                     array of all uses of <code>t</code>. 
    */
    public HCodeElement[] useMap(Temp t) {
	HCodeElement[] r = extractTempMapping(t);
	return (r == null) ? 
	    (HCodeElement[]) arrayFact.newArray(0) :
	    (HCodeElement[]) Util.safeCopy(arrayFact, r);
    }
    
    /** Returns an array of all <code>Temp</code> used in the
	<code>HCode</code> associated with <code>this</code>.  
    */
    public Temp[] allUses() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, allUses);
    }

}
