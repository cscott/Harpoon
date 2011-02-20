// ExactTypeMap.java, created Thu Aug 24 12:03:50 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import java.io.Serializable;
import java.util.AbstractList;
/**
 * <code>ExactTypeMap</code> is an extension of <code>TypeMap</code> that
 * allows the indication of 'exact' types -- that is, temporaries that
 * can be guaranteed to contain an object of the indicated type
 * <strong>and not a subtype of that type</strong>.
 * <p>
 * Any <code>TypeMap</code> can be made into an <code>ExactTypeMap</code>
 * by conservatively returning <code>typeMap(hce,t).isPrimitive()</code>
 * to all calls to <code>isExactType()</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ExactTypeMap.java,v 1.3 2002-09-02 19:23:26 cananian Exp $
 */
public interface ExactTypeMap<HCE extends HCodeElement> extends TypeMap<HCE> {
    /** Returns <code>true</code> if temporary <code>t</code> defined at
     *  definition point <code>hce</code> contains an object of the
     *  type returned by <code>typeMap(hce, t)</code> <strong>and not
     *  a subtype of that type</strong>.  Returns <code>false</code> if
     *  the object in <code>t</code> can be a subtype of the type returned
     *  by <code>typeMap(hce, t)</code>.
     *  <p>
     *  If <code>typeMap()</code> returns a primitive type, then
     *  <code>isExactType()</code> should return <code>true</code>.
     *
     * @param hce The <code>HCodeElement</code> defining <code>t</code> for
     *        this request.
     * @param t The <code>Temp</code> to examine.
     * @return <code>true</code> if <code>t</code>'s type is exact,
     *         <code>false</code> otherwise.
     * @exception NullPointerException if <code>t</code> or <code>hce</code>
     *            is <code>null</code>.
     * @exception TypeNotKnownException if the <code>ExactTypeMap</code> does
     *            not have any information about <code>t</code> as defined
     *            at <code>hc</code>. */
    public boolean isExactType(HCE hce, Temp t)
	throws TypeMap.TypeNotKnownException;

    /** Implementors of <code>ExactTypeMap</code> will probably find this
     *  pair type useful. */
    static class ExactType extends AbstractList implements Serializable {
	public final HClass type;
	public final boolean isExact;
	public ExactType(HClass type, boolean isExact) {
	    this.type = type; this.isExact = type.isPrimitive() || isExact;
	}
	public int size() { return 2; }
	public Object get(int index) {
	    switch(index) {
	    case 0: return type;
	    case 1: return new Boolean(isExact);
	    default: throw new IndexOutOfBoundsException();
	    }
	}
    }
}


