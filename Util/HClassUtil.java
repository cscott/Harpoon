// HClassUtil.java, created Fri Sep 11 09:14:23 1998 by cananian
package harpoon.Util;

import harpoon.ClassFile.*;
/**
 * <code>HClassUtil</code> contains various useful methods for dealing with
 * HClasses that do not seem to belong with the standard HClass methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassUtil.java,v 1.1 1998-09-11 15:16:33 cananian Exp $
 */

public final class HClassUtil  {
    // Disable constructor.  Only static methods here.
    private HClassUtil() { }
    
    /** Count the number of dimensions of an array type.
     *  @return 0 for a non-array, n for an n-dimensional array type.
     */
    public static final int dims(HClass hc) {
	int i;
	for (i=0; hc.isArray(); i++)
	    hc = hc.getComponentType();
	return i;
    }
    /** Return the ultimate component type of an array (that is, after
     *  all array dimensions have been stripped off. 
     * @return input class <code>hc</code> if <code>hc</code> is not an
     *         array, otherwise a component class <code>c</code> where
     *         <code>c</code> is not an array. */
    public static final HClass baseClass(HClass hc) {
	while (hc.isArray())
	    hc = hc.getComponentType();
	return hc;
    }
    /** Make an n-dimensional array class from the given component class.
     *  The parameter <code>dims</code> is the number of array dimensions
     *  to add. */
    public static final HClass arrayClass(HClass hc, int dims) {
	StringBuffer sb = new StringBuffer();
	return HClass.forDescriptor(Util.repeatString("[",dims)+
				    hc.getDescriptor());
    }
}
