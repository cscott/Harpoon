// DefaultClassDepthMap.java, created Sat Jan 16 21:35:05 1999 by cananian
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
 * @version $Id: DefaultClassDepthMap.java,v 1.1.2.1 1999-01-17 02:51:28 cananian Exp $
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
