// TinyClassFieldMap.java, created Fri Mar 15 19:21:30 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Backend.Analysis.ClassFieldMap;
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
 * <code>TinyClassFieldMap</code> is an extension of <code>ClassFieldMap</code>
 * which lays out objects *attempting* to align them, but not forcing an
 * alignment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TinyClassFieldMap.java,v 1.1.2.1 2002-03-16 01:35:35 cananian Exp $
 */
public abstract class TinyClassFieldMap extends ClassFieldMap {
    
    /** Creates a <code>TinyClassFieldMap</code>. */
    public TinyClassFieldMap() { }

    /** Use this function to indicate a *preferred* but not *mandatory*
     *  alignment for the given field.  We will "try hard" to honor this
     *  alignment, but not if it would mean increasing the object size. */
    public int fieldPreferredAlignment(HField hf){ return fieldAlignment(hf); }

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
	    // sort declared fields by max(preferredalignment,alignment,size)
	    // (largest first)
	    Collections.sort(l, new Comparator<HField>() {
		public int compare(HField hf1, HField hf2) {
		    int align1 = Math.max(fieldPreferredAlignment(hf1),
					  fieldAlignment(hf1));
		    int align2 = Math.max(fieldPreferredAlignment(hf2),
					  fieldAlignment(hf2));
		    return Math.max(fieldSize(hf2),align2)
			- Math.max(fieldSize(hf1),align1);
		}
	    });
	    // make our result list
	    List<HField> result = new ArrayList<HField>(l.size());
	    // while we have more fields to order:
	    while (!l.isEmpty()) {
		HField selectedField = null;
		// find the largest aligned *must-align* field.
		// (a must-align field has fieldAlignment>1)
		for (Iterator<HField> it=l.iterator();
		     selectedField==null && it.hasNext(); ) {
		    HField hf = it.next();
		    if (fieldAlignment(hf) > 1 &&
			0 == (alignment % fieldAlignment(hf))) {
			selectedField = hf;
			it.remove();
		    }
		}
		// else, find the largest preferably-aligned field.
		for (Iterator<HField> it=l.iterator();
		     selectedField==null && it.hasNext(); ) {
		    HField hf = it.next();
		    if (0 == (alignment % fieldPreferredAlignment(hf))) {
			selectedField = hf;
			it.remove();
		    }
		}
		// otherwise find the smallest unaligned field.
		if (selectedField==null)
		    selectedField = l.remove(l.size()-1);
		// okay, add the selected field to the result, update alignment
		result.add(selectedField);
		while (0 != (alignment % fieldAlignment(selectedField)))
		    alignment++;
		alignment += fieldSize(selectedField);
	    }
	    // done.
	    cache.put(hc, result.toArray(new HField[result.size()]));
	}
	return cache.get(hc);
    }
}
