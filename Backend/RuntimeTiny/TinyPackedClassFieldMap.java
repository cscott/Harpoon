// PackedClassFieldMap.java, created Wed Jul 11 20:26:56 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Backend.Maps.FieldMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
/**
 * <code>PackedClassFieldMap</code> is a <code>FieldMap</code> for
 * non-static fields of a class which attempts to maximally fill holes
 * in the data structure (even if this means commingling a subclass'
 * fields with those of its superclass) in order to minimize the
 * space required by objects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TinyPackedClassFieldMap.java,v 1.1.2.2 2002-03-20 23:36:42 cananian Exp $
 */
public abstract class TinyPackedClassFieldMap extends FieldMap {
    final Runtime runtime;
    /** Creates a <code>PackedClassFieldMap</code>. */
    public TinyPackedClassFieldMap(Runtime runtime) {
	this.runtime = runtime;
    }
    
    /** Override this function to indicate a *preferred* but not *mandatory*
     *  alignment for the given field.  We will "try hard" to honor this
     *  alignment, but not if it would mean increasing the object size. */
    public int fieldPreferredAlignment(HField hf){ return fieldAlignment(hf); }

    /** Return an offset to the given field. */
    public int fieldOffset(HField hf) {
	assert !hf.isStatic();
	if (!fieldcache.containsKey(hf)) do_one(hf.getDeclaringClass());
	assert fieldcache.containsKey(hf);
	return fieldcache.get(hf).intValue();
    }
    /** Return an unmodifiable List over all appropriate fields in the given
     *  class, in order from smallest to largest offset. */
    public List<HField> fieldList(HClass hc) {
	// get list of all fields in this class and its parents.
	List<HField> l = new ArrayList<HField>();
	for (HClass p = hc; p!=null; p=p.getSuperclass())
	    l.addAll(Arrays.asList(p.getDeclaredFields()));
	// weed out static fields
	for (Iterator<HField> it=l.iterator(); it.hasNext(); )
	    if (it.next().isStatic())
		it.remove();
	// now sort by fieldOffset.
	Collections.sort(l, new Comparator<HField>() {
	    public int compare(HField hf1, HField hf2) {
		return fieldOffset(hf1) - fieldOffset(hf2);
	    }
	});
	// quick verification of well-formedness
	// (offsets must be strictly increasing)
	int last=-100;
	for (Iterator<HField> it=l.iterator(); it.hasNext(); ) {
	    int off=fieldOffset(it.next());
	    assert last<off : "Ill-formed field list";
	    last = off;
	}
	// done!
	return l;
    }

    /** this method assigns "good" offsets to all the fields in class hc,
     *  using the assignments in hc's superclass and the list of free space
     *  in the superclass as starting points. */
    private void do_one(HClass hc) {
	assert hc!=null;
	// get the 'free space' structure of the parent.
	List<Interval> free = new LinkedList<Interval>
	    (Arrays.asList(freespace(hc.getSuperclass())));
	// get all the fields of this class that we have to allocate.
	List<HField> l =
	    new ArrayList<HField>(Arrays.asList(hc.getDeclaredFields()));
	// weed out static fields
	for (Iterator<HField> it=l.iterator(); it.hasNext(); )
	    if (it.next().isStatic())
		it.remove();
	// we're going to allocate fields from largest to smallest,
	// so sort them first. (primarily sort by size, then by alignment)
	Collections.sort(l, new Comparator<HField>() {
	    public int compare(HField hf1, HField hf2) {
		// note reversed comparison here so that largest end up first.
		int r = fieldSize(hf2) - fieldSize(hf1);
		if (r==0) r = fieldPreferredAlignment(hf2)
			      - fieldPreferredAlignment(hf1);
		if (r==0) r = fieldAlignment(hf2) - fieldAlignment(hf1);
		return r;
	    }
	});
	// now allocate them one by one (largest to smallest, like we said)
	// the alloc_one method has the side effect of adding an entry to
	// the fieldcache.
	nextfield:
	while (!l.isEmpty()) {
	    // allocate largest *must-align* field
	    for (Iterator<HField> it=l.iterator(); it.hasNext(); )
		if (alloc_one(it.next(), free, true, false)) {
		    it.remove();
		    continue nextfield;
		}
	    // allocate largest *preferably-aligned* field
	    for (Iterator<HField> it=l.iterator(); it.hasNext(); )
		if (alloc_one(it.next(), free, false, true)) {
		    it.remove();
		    continue nextfield;
		}
	    // allocate smalled unaligned field.
	    if (alloc_one(l.remove(l.size()-1), free, false, false))
		continue nextfield;
	    // should never get here!
	    assert false : "should always be able to alloc smallest field";
	}
	// cache away the free list for the next guy (subclasses of this)
	freecache.put(hc, free.toArray(new Interval[free.size()]));
	// done!
    }
    /** this method tries to allocate the field hf as low in the object
     *  as possible, using the current list of free spaces in the class. */
    private boolean alloc_one(HField hf, List<Interval> free,
			      boolean mustAlign, boolean prefAlign) {
	int size = fieldSize(hf), align = fieldAlignment(hf);
	int palign = fieldPreferredAlignment(hf);
	// 'mustAlign' fields have align > 1
	if (align<=1 && mustAlign) return false;
	// go through free list, looking for a place to put this.
	// first, try to put it at its preferred alignment, but not at the end
	// as a last resort, put it anywhere, ignoring the preferred alignment.
	for (int pass=0; pass<2; pass++) {
	  for (ListIterator<Interval> li = free.listIterator(); li.hasNext();){
	    Interval i = li.next();
	    assert i.low < i.high; // validity of interval.
	    // use preferred alignment for first pass, but don't allow alloc
	    // at end (in 'infinite interval')
	    int malign = (pass==0) ? palign : align;
	    if (pass==0 && !li.hasNext()) continue;
	    // l and h will be the boundaries of the field, if we put it
	    // in this interval.
	    int l = i.low;
	    // if 'mustAlign' boot all fields which aren't perfectly aligned.
	    if (mustAlign && (!li.hasNext()) && 0!=(l%align)) continue;
	    // if 'prefAlign' boot fields which aren't preferentially aligned
	    if (prefAlign && (!li.hasNext()) && 0!=(l%palign)) continue;
	    // okay, otherwise add padding if necessary.
	    while ((l%malign)!=0) l++; // (slow, but works even w/ negative l)
	    int h = l + size;
	    if (h <= i.high) { // yay, the field fits here!
		// update interval list.
		Interval bot = new Interval(i.low, l);
		Interval top = new Interval(h, i.high);
		li.remove(); // replace the old interval with the split ones
		if (bot.low < bot.high) li.add(bot);
		if (top.low < top.high) li.add(top);
		// cache field offset.
		fieldcache.put(hf, new Integer(l));
		// done!
		return true; // successfully alloc'ed this field
	    }
	  }
	}
	return false; // unable to alloc this field, given our constraints
    }
    /** maps fields to already-computed offsets */
    private final Map<HField,Integer> fieldcache =
	new HashMap<HField,Integer>();

    /** this method returns the cached 'free space' list for class hc.
     *  if the free space list for hc has not already been created and
     *  cached, invokes do_one(hc) to create and cache it.  hc==null
     *  indicates the "non-existent superclass of everything", and
     *  naturally fields can be allocated anywhere in it.  This is
     *  represented as the interval [0, infinity). */
    private Interval[] freespace(HClass hc) {
	if (hc==null)
	    if (runtime.clazBytes==4)
		return new Interval[] { new Interval(0, Integer.MAX_VALUE) };
	    else
		return new Interval[] { new Interval(-8+runtime.clazBytes, -4),
					new Interval(0, Integer.MAX_VALUE) };
	if (!freecache.containsKey(hc)) do_one(hc);
	assert freecache.containsKey(hc);
	return freecache.get(hc);
    }
    /** maps classes to 'free space' lists. */
    private final Map<HClass,Interval[]> freecache =
	new HashMap<HClass,Interval[]>();

    /** This class represents an open interval in the class, which we
     *  may assign a field to (if it fits). */
    private static class Interval {
	final int low, high;
	Interval(int low, int high) { this.low = low; this.high=high; }
    }
}
