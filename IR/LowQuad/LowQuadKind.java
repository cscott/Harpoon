// LowQuadKind.java, created Wed Jan 20 22:20:40 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

/**
 * <code>LowQuadKind</code> is an enumerated type for the various kinds of
 * <code>LowQuad</code>s. It extends <code>QuadKind</code>, so it enumerates
 * all the <code>Quad</code> types, too.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadKind.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public abstract class LowQuadKind extends harpoon.IR.Quads.QuadKind {
    private static int n = min();

    // PPTR:
    public static final int PARRAY   = n++;
    public static final int PFIELD   = n++;
    public static final int PMETHOD  = n++;
    // PCONST:
    public static final int PAOFFSET = n++;
    public static final int PFOFFSET = n++;
    public static final int PMOFFSET = n++;
    public static final int PFCONST  = n++;
    public static final int PMCONST  = n++;
    // others:
    public static final int PCALL    = n++;
    public static final int PGET     = n++;
    public static final int POPER    = n++;
    public static final int PSET     = n++;

    public static int min() { return harpoon.IR.Quads.QuadKind.max(); }
    public static int max() { return n; }

    public static boolean isValid(int k) {
	return harpoon.IR.Quads.QuadKind.isValid(k) ||
	    (min() <= k && k < max() );
    }
}
