// TempToHceSetMap.java, created Fri Jan 29 19:04:30 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.Util.ArrayFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <code>TempToHceSetMap</code> is a general class that maps <code>Temp</code>s to 
 * arrays of <code>HCodeElement</code>s
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: TempToHceArrayMap.java,v 1.1.2.5 2000-01-17 11:10:00 cananian Exp $
 */
abstract class TempToHceArrayMap {

    // need to have an array factory to convert sets
    protected ArrayFactory arrayFact; 

    private Map map;

    /** Constructs a TempToHceArrayMap with
	<code>hc.elementArrayFactory()</code> as its associated
	<code>ArrayFactory</code>. 
     */
    TempToHceArrayMap(HCode hc) {
	this.arrayFact = hc.elementArrayFactory();
	map = new HashMap();
    }

    /** Constructs a TempToHceArrayMap with <code>fact</code> as its
	associated <code>ArrayFactory</code>.
     */
    TempToHceArrayMap(ArrayFactory fact) {
	this.arrayFact = fact;
	map = new HashMap();
    }

    /** Stores a mapping from <code>t</code> to <code>hces</code>.
	<BR> <B>effects:</B> adds a mapping from <code>t</code> to  
	                     <code>hces</code>, overwriting any
			     previous mapping for <code>t</code> that
			     may exist in <code>this</code>. 
    */
    protected void storeTempMapping(Temp t, HCodeElement[] hces) {
	// modifies: this.map
	map.put(t, hces);
    }

    /** Converts a <code>Set</code> of <code>HCodeElement</code>s into
	an <code>HCodeElement</code> array.
	<BR> <B>requires:</B> <code>s</code> is a <code>Set</code> of
	                      <code>HCodeElement</code> objects of the
			      same type as the <code>HCode</code>
			      associated with <code>this</code>.
	<BR> <B>effects:</B> generates a new array (typed with the
	                     HCode associated with <code>this</code>)
			     sized to hold all of the elements of
			     <code>s</code>, copies the elements of
			     <code>s</code> into it, and returns the
			     newly generated array.
    */
    protected HCodeElement[] setToHces(Set s) {
	HCodeElement[] hcel = 
	    (HCodeElement[]) arrayFact.newArray(s.size()); 
	  
	return (HCodeElement[]) s.toArray(hcel);

    } 

    /** Stores a mapping from <code>t</code> to <code>hces</code>.
	<BR> <B>requires:</B> <code>hces</code> is a <code>Set</code>
	                      of <code>HCodeElement</code> objects of
			      the same type as the <code>HCode</code> 
			      associated with <code>this</code>.
	<BR> <B>effects:</B> adds a mapping from <code>t</code> to an
	                     <code>HCodeElement</code> array
			     representing the elements of
			     <code>hces</code> in <code>this</code>,
			     overwriting any previous mapping for
			     <code>t</code> that may exist in
			     <code>this</code>.  
    */
    protected void storeTempMapping(Temp t, Set hces) {
	// modifies: this.map

	map.put(t, setToHces(hces));
    }

    /** Extracts the <code>HCodeElement</code> array mapping for
	<code>t</code>.
	<BR> <B>effects:</B> if there is no mapping for
	                     <code>t</code>, returns null.   Else
			     returns the <code>HCodeElement</code>
			     array associated with <code>t</code>.
    */
    protected HCodeElement[] extractTempMapping(Temp t) {
        return(HCodeElement[]) map.get(t);
    }

    /** Creates a mapping from the elements of <code>temps</code> to
	<code>hce</code> in <code>tmpToHceSet</code>.
	<BR> <B>requires:</B> <code>tmpToHceSet</code> is a
	                      <code>Map</code> accepting
			      <code>Temp</code>s as keys and
			      <code>Set</code>s of
			      <code>HCodeElement</code>s as values.
        <BR> <B>modifies:</B> <code>tmpToHceSet</code>
	<BR> <B>effects:</B> For each element of <code>temps</code>:
	<BR>         1. Retrieves the <code>Set</code> associated with
		        that <code>Temp</code>, or creates one if
			there is not one already present in
			<code>tmpToHceSet</code>.
	<BR>	     2. Adds <code>hce</code> to the <code>Set</code>
		        associated with the <code>Temp</code>.
    */			
    protected static void associate(HCodeElement hce, Temp[] temps,
				    Map tmpToHceSet) {
	for (int i=0; i<temps.length; i++) {
	    Set s = (Set) tmpToHceSet.get(temps[i]);
	    if (s == null) { 
		s = new HashSet(); 
		tmpToHceSet.put(temps[i], s);
	    }
	    s.add(hce);
	}
    }

}

