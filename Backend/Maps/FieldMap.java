// FieldMap.java, created Sat Jan 16 21:42:16 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.List;
/**
 * A <code>FieldMap</code> assigns an ordering to a set of fields.
 * Typically separate <code>FieldMap</code>s will be used for class
 * fields (which are allocated locally) and static fields (which are
 * allocated globally).  Note that the ordering corresponds directly to
 * an offset from the first ordered field when the size returned by
 * fieldSize() is taken into account.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldMap.java,v 1.1.2.3 1999-10-12 20:04:49 cananian Exp $ */
public abstract class FieldMap  {
    /** Return an offset to the given field. */
    public int fieldOffset(HField hf) {
	Util.assert(hf!=null);
	int offset=0;
	for (Iterator it=fieldList(hf.getDeclaringClass()).iterator();
	     it.hasNext(); ) {
	    HField nexthf = (HField) it.next();
	    if (hf.equals(nexthf)) return offset;
	    else offset+=fieldSize(nexthf);
	}
	// this is an error not an assert() because asserts can be
	// optimized away.  the algorithm is well and truely broken
	// if we get to this point, and it's better to throw an
	// odd Error than return some bogus value.
	throw new Error("HField "+hf+" not in fieldList()");
    }
    /** Return an unmodifiable List over all appropriate fields in the given
     *  class, in order from smallest to largest offset. */
    public abstract List fieldList(HClass hc);
    /** Return the allocated size of a given field. */
    public abstract int fieldSize(HField hf);
}
