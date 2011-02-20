// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;

/**
 * A <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.5 2002-09-02 19:23:26 cananian Exp $
 */
public interface TypeMap<HCE extends HCodeElement>  { 
    /** 
     * Return the type of a given temporary <code>t</code>, as defined
     * at the definition point <code>hce</code>.  Iff the type of the
     * temporary is not known, or if <code>hce</code> does not define
     * <code>t</code>, throws <code>TypeNotKnownException</code>.  If
     * the temporary represents a derived pointer, <code>null</code>
     * should be returned; in which case the <code>Derivation</code>
     * <b>must</b> return a non-<code>null</code> value.  Obviously,
     * <code>TypeMap</code>s for high-level representations without
     * derived pointers should never return <code>null</code>.  As a
     * special case, <code>HClass.Void</code> may be returned in
     * low-level IRs to indicate an opaque pointer value which does
     * <b>not</b> correspond to a java object pointer or some
     * derivation thereof --- for example, a pointer into a method
     * dispatch table.  The only other time <code>HClass.Void</code>
     * should be returned is for known-<code>null</code> pointer values.
     * 
     * @param hce The <code>HCodeElement</code> defining <code>t</code>.
     * @param t The temporary to examine.
     * @return the static type of <code>t</code>.  
     * @exception NullPointerException if <code>t</code> or <code>hc</code>
     *            is <code>null</code>.
     * @exception TypeNotKnownException if the <code>TypeMap</code> does
     *            not have any information about <code>t</code> as defined
     *            at <code>hc</code>.  */
    public HClass typeMap(HCE hce, Temp t)
	throws TypeNotKnownException;

    /** <code>TypeNotKnownException</code> is thrown to indicate that
     *  the <code>TypeMap</code> does not have type information for the
     *  specified <code>Temp</code> in the given <code>HCodeElement</code>.
     *  This could be because the <code>HCodeElement</code> does not
     *  define the specified <code>Temp</code>, or because the type
     *  analysis was faulty.  In either case, this exception should
     *  never be caught -- it indicates a flaw in either the type
     *  analysis or the querying method, which should be fixed. */
    public static class TypeNotKnownException extends RuntimeException {
	public TypeNotKnownException(HCodeElement hce, Temp t) {
	    super("Type not known for Temp "+t+" in "+hce);
	}
    }
}
