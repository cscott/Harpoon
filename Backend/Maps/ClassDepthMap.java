// ClassDepthMap.java, created Sat Jan 16 21:29:32 1999 by cananian
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;

/**
 * A <code>ClassDepthMap</code> reports the nesting depth of a given
 * class, with <code>java.lang.Object</code> given nesting depth 0.
 * This is used to layout the display structure for fast implementation
 * of <code>instanceof</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassDepthMap.java,v 1.1.2.1 1999-01-17 02:51:27 cananian Exp $
 */
public abstract class ClassDepthMap  {
    /** Return the nesting depth of the given class. Not valid for
     *  interface classes. */
    public abstract int classDepth(HClass hc);

    /** Returns the maximum nesting depth of any class (dependent on a
     *  given context). */
    public abstract int maxDepth();
}
