// PreciseType.java, created Wed Aug 11 23:00:32 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * <code>PreciseType</code> enumerates precise types; that is,
 * types smaller than <code>Type.INT</code>.  A <code>PreciseType</code>
 * is a valid <code>Type</code> only if the <code>Tree.Exp</code>
 * implements the <code>PrecisedTyped</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PreciseType.java,v 1.1.2.1 1999-08-12 03:37:27 cananian Exp $
 */
public class PreciseType extends Type {
    // enumerated constants
    /** Type which cannot be expressed by one of the standard
     *  <code>Type</code>s.  
     *  Such types must be defined in terms of a bit-width, and an
     *  indication of whether tha value is signed or not. */
    public final static int SMALL = Type.POINTER+1;

    public final static boolean isValid(int type) {
	return (type==SMALL || Type.isValid(type));
    }

    // human-readability
    public final static String toString(int type) {
	if (type==SMALL) return "SMALL";
	else return Type.toString(type);
    }
}
