// UseMap.java, created Fri Jan 29 20:31:09 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Set;

import java.util.Hashtable;
import java.util.Enumeration;
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
 * @version $Id: UseMap.java,v 1.1.2.1 1999-01-30 23:29:05 pnkfelix Exp $
 */
public class UseMap extends TempToHceArrayMap {
    
    private Temp[] allUses;
     
    /** Creates a <code>UseMap</code> for <code>hc</code>. 
	<BR> requires: <code>hc</code>'s internal representation
	        implements <code>harpoon.IR.Properties.UseDef</code>.
	<BR> effects: creates a <code>UseMap</code> for
	        <code>hc</code>, performing the necessary analysis
		during construction.
    */
    public UseMap( HCode hc ) {
        super(hc);
	analyze();
    }
    
    /* Helper method for analysis of <code>this.hcode</code> during
       construction.  
       <BR> requires: <code>hc</code>'s internal representation
                implements <code>harpoon.IR.Properties.UseDef</code>.
       <BR> effects: performs Variable->Use analysis on
                <code>this.hcode</code> 
    */
    private void analyze() {
	HCodeElement[] hces = hcode.getElements();
	Util.assert(hces instanceof harpoon.IR.Properties.UseDef[],
		    hcode.getName() + " does not implement UseDef");
	harpoon.IR.Properties.UseDef[] udl = 
	    (harpoon.IR.Properties.UseDef[]) hces;

	Hashtable tmpUse = new Hashtable();

	// scan HCodeElements, associating uses with their
	// HCodeElement. 
	for(int i=0; i<hces.length; i++) {
	    associate( hces[i], udl[i].use(), tmpUse);
	}

	// Store the final set in the main map.
	Enumeration e=tmpUse.keys();
	Vector uses = new Vector();
	while ( e.hasMoreElements() ) {
	    Temp use = (Temp) e.nextElement();
 	    storeTempMapping(use, (Set) tmpUse.get(use));
	    uses.addElement(use);
	}
	allUses = new Temp[uses.size()];
	uses.copyInto( allUses );
    }
        
    /** Return the <code>HCodeElement</code>s which use a given
	<code>Temp</code> in the <code>HCode</code> associated with
	this.
	<BR> effects: Returns an <code>HCodeElement</code> array of
	              all uses of <code>t</code>.
    */
    public HCodeElement[] useMap(Temp t) {
	HCodeElement[] r = extractTempMapping(t);
	return (r == null) ? 
	    (HCodeElement[]) hcode.elementArrayFactory().newArray(0) :
	    (HCodeElement[]) Util.safeCopy(hcode.elementArrayFactory(), r);
    }
    
    /** Returns an array of all <code>Temp</code> used in the
	<code>HCode</code> associated with this.  
    */
    public Temp[] allUses() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, allUses);
    }





}
