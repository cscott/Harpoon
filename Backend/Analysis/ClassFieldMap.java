// ClassFieldMap.java, created Sun Oct 10 19:30:33 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.Util.HClassUtil;
import net.cscott.jutil.UnmodifiableListIterator;
import harpoon.Util.Util;

import java.util.AbstractSequentialList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
/**
 * A <code>ClassFieldMap</code> is a <code>FieldMap</code> for
 * non-static fields of a class.  The user must implement a function
 * giving the size of a field to complete the implementation.
 * Results are cached for efficiency.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassFieldMap.java,v 1.6 2004-02-08 01:57:24 cananian Exp $
 */
public abstract class ClassFieldMap extends harpoon.Backend.Maps.FieldMap {
    /** Creates a <code>ClassFieldMap</code>. */
    public ClassFieldMap() { /* no special initialization. */ }

    // caching version of method inherited from superclass.
    public int fieldOffset(HField hf) {
	assert hf!=null && !hf.isStatic();
	if (!cache.containsKey(hf)) {
	    int offset=0;
	    for (Iterator<HField> it =
		     fieldList(hf.getDeclaringClass()).iterator();
		 it.hasNext(); ) {
		HField nexthf = it.next();
		int align = fieldAlignment(nexthf);
		assert align>0;
		if ((offset % align) != 0)
		    offset += align - (offset%align);
		assert (offset % align) == 0;
		if (!cache.containsKey(nexthf))
		    cache.put(nexthf, new Integer(offset));
		offset+=fieldSize(nexthf);
	    }
	}
	assert cache.containsKey(hf) : hf+" not in fieldList()";
	return cache.get(hf).intValue();
    }
    private final Map<HField,Integer> cache = new HashMap<HField,Integer>();

    // the meat of this class: return non-static fields in order, from
    // top-most superclass down.
    public List<HField> fieldList(final HClass hc) {
	assert hc!=null;
	// first calculate size of list.
	int n=0;
	for (HClass hcp=hc; hcp!=null; hcp=hcp.getSuperclass()) {
	    HField[] fields = declaredFields(hcp);
	    for (int i=0; i<fields.length; i++)
		if (!fields[i].isStatic()) n++;
	}
	final int size = n;
	// now make & return list object.
	return new AbstractSequentialList<HField>() {
	    public int size() { return size; }
	    public ListIterator<HField> listIterator(final int index) {
		return new UnmodifiableListIterator<HField>() {
		    final HClass[] parents = HClassUtil.parents(hc);
		    HField[] fields;
		    int pindex, findex, xindex;
		    boolean done=true, forwards;
		    {   // initialization code: count from beginning or end
			// depending on which is closer.
			if (index < size/2) { // count from beginning.
			    pindex=0;
			    fields = declaredFields(parents[pindex]);
			    findex=-1; forwards=true;
			    for (xindex=-1; xindex < index; xindex++)
				advance();
			} else { // count from end.
			    pindex = parents.length-1;
			    fields = declaredFields(parents[pindex]);
			    findex=fields.length; forwards=true;
			    for (xindex=size; xindex > index; xindex--)
				retreat();
			}
		    }
		    public boolean hasNext() {
			if (!forwards) changeDirection();
			return !done;
		    }
		    public boolean hasPrevious() {
			if (forwards) changeDirection();
			return !done;
		    }
		    public HField next() {
			if (!forwards) changeDirection();
			if (done) throw new NoSuchElementException();
			xindex++;
			HField hf = fields[findex]; advance(); return hf;
		    }
		    public HField previous() {
			if (forwards) changeDirection();
			if (done) throw new NoSuchElementException();
			xindex--;
			HField hf = fields[findex]; retreat(); return hf;
		    }
		    public int nextIndex() { return xindex; }
		    private void advance() {
			done=false;
			while (true) {
			    for (findex++; findex < fields.length; findex++)
				if (!fields[findex].isStatic()) return;
			    if (++pindex >= parents.length) break;
			    fields = declaredFields(parents[pindex]);
			    findex = -1;
			}
			--pindex; done=true;
		    }
		    private void retreat() {
			done=false;
			while (true) {
			    for (findex--; findex >= 0; findex--)
				if (!fields[findex].isStatic()) return;
			    if (--pindex < 0) break;
			    fields = declaredFields(parents[pindex]);
			    findex = fields.length;
			}
			++pindex; done=true;
		    }
		    private void changeDirection() {
			if (forwards) retreat(); else advance();
			forwards = !forwards;
		    }
		};
	    }
	};
    }
    /** Return the declared fields of the specified class in the
     *  order in which they should be allocated.  This implementation
     *  just returns the result of <code>hc.getDeclaredFields()</code>,
     *  but you can override this to use a more intelligent sorting
     *  routine to save space. You do not have to filter static methods
     *  out of the returned array, but you may if you like. */
    protected HField[] declaredFields(HClass hc) {
	return hc.getDeclaredFields();
    }
}
