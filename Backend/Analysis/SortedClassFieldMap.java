// SortedClassFieldMap.java, created Wed Jul 11 12:22:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * A <code>SortedClassFieldMap</code> is an extension of
 * <code>ClassFieldMap</code> which sorts object fields to
 * minimize "holes" between fields.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SortedClassFieldMap.java,v 1.3 2002-04-10 03:02:22 cananian Exp $
 */
public abstract class SortedClassFieldMap extends ClassFieldMap {
    
    /** Creates a <code>SortedClassFieldMap</code>. */
    public SortedClassFieldMap() { }

    private final Map<HClass,HField[]> cache = new HashMap<HClass,HField[]>();
    protected HField[] declaredFields(HClass hc) {
	if (!cache.containsKey(hc)) {
	    // determine the alignment of the last field of the superclass.
	    int alignment=0;
	    HClass sc = hc.getSuperclass();
	    if (sc!=null) {
		List<HField> l = fieldList(sc);
		if (l.size()>0) {
		    HField lastfield = l.get(l.size()-1);
		    alignment = fieldOffset(lastfield)+fieldSize(lastfield);
		}
	    }
	    // make list of non-static fields.
	    List<HField> l =
		new ArrayList<HField>(Arrays.asList(hc.getDeclaredFields()));
	    for (Iterator<HField> it=l.iterator(); it.hasNext(); )
		if (it.next().isStatic())
		    it.remove();
	    // sort declared fields by max(alignment,size)
	    // (smallest first)
	    Collections.sort(l, new Comparator<HField>() {
		public int compare(HField hf1, HField hf2) {
		    return Math.max(fieldSize(hf1),fieldAlignment(hf1))
			- Math.max(fieldSize(hf2),fieldAlignment(hf2));
		}
	    });
	    // if parent is unaligned, start at small end; else start at big
	    // end.
	    if (l.size()>0) {
		HField big = l.get(l.size()-1);
		if ((alignment % Math.max(fieldSize(big),fieldAlignment(big)))
		    ==0)
		    Collections.reverse(l);
	    }
	    // done.
	    cache.put(hc, l.toArray(new HField[l.size()]));
	}
	return cache.get(hc);
    }
}
