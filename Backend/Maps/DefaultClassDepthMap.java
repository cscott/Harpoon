// DefaultClassDepthMap.java, created Sat Jan 16 21:35:05 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;

/**
 * A <code>DefaultClassDepthMap</code> computes class depth simply and
 * non-efficiently.  It does not implement the <code>maxDepth()</code>
 * method.  A "real" implementation of ClassDepthMap should cache
 * the depths it computes to avoid having to travese the entire class
 * hierarchy repeatedly.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultClassDepthMap.java,v 1.1.2.2 1999-08-04 05:52:27 cananian Exp $
 */
public class DefaultClassDepthMap  {
    /** Creates a <code>DefaultClassDepthMap</code>. */
    public DefaultClassDepthMap() {
        // stupid implementation, no arguments.
    }
    public int classDepth(HClass hc) {
	HClass sc = hc.getSuperclass();
	return (sc==null) ? 0 : 1 + classDepth(sc);
    }
    public int maxDepth() { throw new Error("Unimplemented."); }
}
