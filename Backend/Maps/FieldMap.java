// FieldMap.java, created Sat Jan 16 21:42:16 1999 by cananian
package harpoon.Backend.Maps;

import harpoon.ClassFile.HField;

/**
 * A <code>FieldMap</code> assigns an ordering to a set of fields.
 * Typically separate <code>FieldMap</code>s will be used for class
 * fields (which are allocated locally) and static fields (which are
 * allocated globally).  Note that the ordering does <i>not</i>
 * correspond directly to an offset, since the size of fields may vary
 * on different architectures, and the start of the field space may be
 * offset from the base pointer.  The function of the
 * <code>OffsetMap</code> is to take these factors into account.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldMap.java,v 1.1.2.1 1999-01-17 02:51:28 cananian Exp $ */
public abstract class FieldMap  {
    /** Return an ordering of the given field. */
    public abstract int fieldOrder(HField hf);
}
