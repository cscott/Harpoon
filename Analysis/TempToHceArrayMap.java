// TempToHceSetMap.java, created Fri Jan 29 19:04:30 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.Util.Set;

import java.util.Hashtable;

/**
 * <code>TempToHceSetMap</code> is a general class that maps <code>Temp</code>s to 
 * arrays of <code>HCodeElement</code>s
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: TempToHceArrayMap.java,v 1.1.2.1 1999-01-30 23:29:05 pnkfelix Exp $
 */
abstract class TempToHceArrayMap {
    
    protected HCode hcode; // need to have an hcode object to convert
                           // sets 

    private Hashtable map;

    TempToHceArrayMap(HCode hc) {
	this.hcode = hc;
	map = new Hashtable();
    }

    /** Stores a mapping from <code>t</code> to <code>hces</code>.
	<BR> modifies: <code>this.map</code>
	<BR> effects: adds a mapping from <code>t</code> to 
	              <code>hces</code> in <code>this.map</code>,
		      overwriting any previous mapping for
		      <code>t</code> that may exist in
		      <code>this.map</code>.
    */
    protected void storeTempMapping(Temp t, HCodeElement[] hces) {
	map.put(t, hces);
    }

    /** Converts a <code>Set</code> of <code>HCodeElement</code>s into
	an <code>HCodeElement</code> array.
    */
    protected HCodeElement[] setToHces(Set s) {
	HCodeElement[] hcel = 
	    (HCodeElement[]) hcode.elementArrayFactory().newArray(s.size()); 
	s.copyInto(hcel);
	return hcel;

    } 

    /** Stores a mapping from <code>t</code> to <code>hces</code>.
	<BR> modifies: <code>this.map</code>
	<BR> effects: adds a mapping from <code>t</code> to an
	              <code>HCodeElement</code> array represented the
		      elements of <code>hces</code> in
		      <code>this.map</code>, overwriting any previous
		      mapping for <code>t</code> that may exist in
		      <code>this.map</code>. 
    */
    protected void storeTempMapping(Temp t, Set hces) {
	map.put(t, setToHces(hces));
    }

    /** Extracts the <code>HCodeElement</code> array mapping for
	<code>t</code>.
	<BR> effects: if there is no mapping for <code>t</code>, returns
	              null.   Else returns the
		      <code>HCodeElement</code> array associated with
		      <code>t</code>.
    */
    protected HCodeElement[] extractTempMapping(Temp t) {
        return(HCodeElement[]) map.get(t);
    }

    /** Creates a mapping from the elements of <code>temps</code> to
	<code>hce</code> in <code>tmpToHceSet</code>.
	<BR> requires: <code>tmpToHceSet</code> is a <code>Hashtable</code>
	               with <code>Temp</code>s as keys and
		       <code>Set</code>s of <code>HCodeElement</code>s
		       as values.
        <BR> modifies: <code>tmpToHceSet</code>
	<BR> effects: For each element of <code>temps</code>,
	<BR>         1. Retrieves the <code>Set</code> associated with
		        that <code>Temp</code>, or creates one if
			there is not one already present in
			<code>tmpToHceSet</code>.
	<BR>	     2. Adds <code>hce</code> to the <code>Set</code>
		        associated with the <code>Temp</code>.
    */			
    protected static void associate(HCodeElement hce, Temp[] temps,
				    Hashtable tmpToHceSet) {
	for (int i=0; i<temps.length; i++) {
	    Set s = (Set) tmpToHceSet.get(temps[i]);
	    if (s == null) { 
		s = new Set(); 
		tmpToHceSet.put(temps[i], s);
	    }
	    s.union(hce);
	}
    }

}
