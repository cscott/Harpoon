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
 * @version $Id: FieldMap.java,v 1.4 2003-04-19 01:03:54 salcianu Exp $ */
public abstract class FieldMap implements java.io.Serializable {
    /** Return an offset to the given field. */
    public abstract int fieldOffset(HField hf);
    /** Return an unmodifiable List over all appropriate fields in the given
     *  class, in order from smallest to largest offset. */
    public abstract List<HField> fieldList(HClass hc);
    /** Return the allocated size of a given field. */
    public abstract int fieldSize(HField hf);
    /* Override this method if you need the fields aligned in any special
     * way.  Default implementation aligns every field to its size. */
    public int fieldAlignment(HField hf) { return fieldSize(hf); }
}
