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
 * @version $Id: TinyClassFieldMap.java,v 1.3 2004-02-08 03:21:01 cananian Exp $
 */
public abstract class TinyClassFieldMap extends ClassFieldMap {
    final int first_alignment;
    
    /** Creates a <code>TinyClassFieldMap</code>. */
    public TinyClassFieldMap() { this(0); }
    /** Creates a <code>TinyClassFieldMap</code>. */
    public TinyClassFieldMap(int first_alignment) {
	this.first_alignment = first_alignment;
    }

    /** Use this function to indicate a *preferred* but not *mandatory*
     *  alignment for the given field.  We will "try hard" to honor this
     *  alignment, but not if it would mean increasing the object size. */
    public int fieldPreferredAlignment(HField hf){ return fieldAlignment(hf); }

    private final Map<HClass,HField[]> cache = new HashMap<HClass,HField[]>();
    protected HField[] declaredFields(HClass hc) {
	if (!cache.containsKey(hc)) {
	    // determine the alignment of the last field of the superclass.
	    int alignment=first_alignment;
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
		    // hack to skip fields which cross the header boundary
		    if (alignment<0 && alignment+fieldSize(hf)>0) continue;
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
		    // hack to skip fields which cross the header boundary
		    if (alignment<0 && alignment+fieldSize(hf)>0) continue;
		    if (0 == (alignment % fieldPreferredAlignment(hf))) {
			selectedField = hf;
			it.remove();
		    }
		}
		// otherwise find the smallest unaligned field.
		if (selectedField==null)
		    selectedField = l.remove(l.size()-1);
		// hack to protect the header boundary.
		if (alignment<0 && alignment+fieldSize(selectedField)>0)
		    alignment=0;
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
    // XXX copied from superclass, but hacked to deal with first_alignment.
    public int fieldOffset(HField hf) {
	assert hf!=null && !hf.isStatic();
	if (!cache2.containsKey(hf)) {
	    int offset=first_alignment;
	    for (HField nexthf : fieldList(hf.getDeclaringClass())) {
		// hack to protect the header boundary
		if (offset<0 && offset+fieldSize(nexthf)>0)
		    offset=0;
		// end hack
		int align = fieldAlignment(nexthf);
		assert align>0;
		if ((offset % align) != 0)
		    offset += align - (offset%align);
		assert (offset % align) == 0;
		if (!cache2.containsKey(nexthf))
		    cache2.put(nexthf, new Integer(offset));
		offset+=fieldSize(nexthf);
	    }
	}
	assert cache2.containsKey(hf) : hf+" not in fieldList()";
	return cache2.get(hf).intValue();
    }
    private final Map<HField,Integer> cache2 = new HashMap<HField,Integer>();
}
