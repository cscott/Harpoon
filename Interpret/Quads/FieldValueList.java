// FieldValueList.java, created Mon Dec 28 00:24:15 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

/**
 * <code>FieldValueList</code> holds field values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldValueList.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
final class FieldValueList  {
    /** which field this value is for. */
    final HField which;
    /** the value of the field. */
    Object value;
    /** the next field/value pair in the list. */
    FieldValueList next;

    FieldValueList(HField which, Object value, FieldValueList next) {
	this.which = which; this.value = value; this.next = next;
    }

    static Object get(FieldValueList fvl, HField f) {
	for (FieldValueList fvlp=fvl; fvlp!=null; fvlp=fvlp.next)
	    if (fvlp.which.equals(f)) return fvlp.value;
	throw new Error("Field not found: "+f);
    }
    static FieldValueList update(FieldValueList fvl, HField f, Object value) {
	for (FieldValueList fvlp=fvl; fvlp!=null; fvlp=fvlp.next)
	    if (fvlp.which.equals(f)) { fvlp.value = value; return fvl; }
	return new FieldValueList(f, value, fvl);
    }
}
