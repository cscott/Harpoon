// FieldValueList.java, created Mon Dec 28 00:24:15 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HField;

/**
 * <code>FieldValueList</code> holds field values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FieldValueList.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
final class FieldValueList {
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
    static FieldValueList clone(FieldValueList fvl) {
       return (fvl==null)?fvl:
       new FieldValueList(fvl.which, fvl.value, clone(fvl.next));
    }
  
    public String toString() {
	StringBuffer sb = new StringBuffer("");
	if (which != null) {
	    sb.append("< ");
	    sb.append(which);
	    sb.append(" >");
	}
	if (next != null) {
	    sb.append(next.toString());
	}

	return sb.toString();
    }
}

