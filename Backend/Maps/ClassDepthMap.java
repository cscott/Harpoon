// ClassDepthMap.java, created Sat Jan 16 21:29:32 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;

/**
 * A <code>ClassDepthMap</code> reports the nesting depth of a given
 * class, with <code>java.lang.Object</code> given nesting depth 0.
 * This is used to layout the display structure for fast implementation
 * of <code>instanceof</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassDepthMap.java,v 1.2 2002-02-25 21:01:55 cananian Exp $
 */
public abstract class ClassDepthMap  {
    /** Return the nesting depth of the given class. Not valid for
     *  interface classes. */
    public abstract int classDepth(HClass hc);

    /** Returns the maximum nesting depth of any class (dependent on a
     *  given context). */
    public abstract int maxDepth();
}
