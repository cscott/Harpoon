// ArrayFactory.java, created Sat Nov 28 01:43:53 1998 by cananian
package harpoon.Util;

/**
 * The <code>ArrayFactory</code> interface allows you to make
 * arrays of objects without run-time type information to determine
 * the object's type.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayFactory.java,v 1.1.2.3 1999-06-16 17:02:59 cananian Exp $
 */

public interface ArrayFactory  {
    /** Create and return a new array of the specified length.
     *  The type is determined by the specific <code>ArrayFactory</code>
     *  that you are using.
     * @see harpoon.Temp.Temp#arrayFactory
     * @see harpoon.Temp.Temp#doubleArrayFactory
     * @see harpoon.IR.Quads.Quad#arrayFactory
     * @see harpoon.ClassFile.HCode#elementArrayFactory
     */
    public Object[] newArray(int len);
}
