// LowQuadKind.java, created Wed Jan 20 22:20:40 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

/**
 * <code>LowQuadKind</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadKind.java,v 1.1.2.2 1999-01-21 05:19:13 cananian Exp $
 */
public abstract class LowQuadKind extends harpoon.IR.Quads.QuadKind {
    private static int n = min();

    public static final int PCALL  = n++;
    public static final int PGET   = n++;
    public static final int POPER  = n++;
    public static final int PSET   = n++;

    public static int min() { return harpoon.IR.Quads.QuadKind.max(); }
    public static int max() { return n; }

    public static boolean isValid(int k) {
	return harpoon.IR.Quads.QuadKind.isValid(k) ||
	    (min() <= k && k < max() );
    }
}
