// DefaultClassDepthMap.java, created Sat Jan 16 21:35:05 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.Analysis.ClassHierarchy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A <code>DefaultClassDepthMap</code> computes class depth simply and
 * efficiently.  It uses a <code>ClassHierarchy</code> object to enable it
 * to implement the <code>maxDepth()</code> method.  It also caches
 * the depths it computes to avoid having to traverse the entire class
 * inheritance tree repeatedly.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultClassDepthMap.java,v 1.2 2002-02-25 21:01:55 cananian Exp $
 */
public class DefaultClassDepthMap extends ClassDepthMap {
    private final int maxDepth;
    /** Creates a <code>DefaultClassDepthMap</code> using the given
     *  <code>ClassHierarchy</code> to compute the maximum class depth. */
    public DefaultClassDepthMap(ClassHierarchy ch) {
	int max = 0;
	for (Iterator it = ch.classes().iterator(); it.hasNext(); )
	    max = Math.max(max, classDepth((HClass)it.next()));
	this.maxDepth = max;
    }
    public int classDepth(HClass hc) {
	if (cache.containsKey(hc)) return ((Integer)cache.get(hc)).intValue();
	int depth;
	if (hc.isArray()) // array hierarchy is based on component type
	    depth = 1 + classDepth(hc.getComponentType());
	else {
	    HClass sc = hc.getSuperclass();
	    depth = (sc==null) ? 0 : 1 + classDepth(sc);
	}
	cache.put(hc, new Integer(depth));
	return depth;
    }
    private final Map cache = new HashMap();

    public int maxDepth() { return maxDepth; }
}
