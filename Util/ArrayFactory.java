// ArrayFactory.java, created Sat Nov 28 01:43:53 1998 by cananian
package harpoon.Util;

/**
 * The <code>ArrayFactory</code> interface allows you to make
 * arrays of objects without run-time type information to determine
 * the object's type.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayFactory.java,v 1.1.2.2 1999-01-22 23:34:00 cananian Exp $
 */

public interface ArrayFactory  {
    /* Create and return a new array of the specified length. */
    public Object[] newArray(int len);
}
