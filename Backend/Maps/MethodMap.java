// MethodMap.java, created Sat Jan 16 21:20:45 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;

/**
 * A <code>MethodMap</code> assigns an ordering to the methods in a
 * class. Typically a separate <code>MethodMap</code> will be used for
 * class and interface methods.  Note that a <code>MethodMap</code>
 * does <u>not</u> specify a direct offset, since the size of each
 * method pointer will be machine-dependent and the start of the
 * method table may be offset from the class descriptor pointer.  The
 * function of an <code>OffsetMap</code> is to take the orderings
 * specified by a pair of <code>MethodMap</code>s (one for
 * single-inheritance classes, one for interfaces) and layout a class
 * descriptor table from it, computing appropriate byte offsets to the
 * various method pointers.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodMap.java,v 1.2 2002-02-25 21:02:00 cananian Exp $ */
public abstract class MethodMap  {
    /** Return an ordering of the given method. */
    public abstract int methodOrder(HMethod hm);
}
