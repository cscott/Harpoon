// ArrayEnumerator.java, created Wed Sep 16 15:14:59 1998 by cananian
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * An <code>ArrayEnumerator</code> enumerates the elements of an array.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayEnumerator.java,v 1.1 1998-09-16 23:51:53 cananian Exp $
 */

public class ArrayEnumerator implements Enumeration {
    Object[] oa;
    int i = 0;

    /** Creates an <code>ArrayEnumerator</code>. */
    public ArrayEnumerator(Object[] oa) {
        this.oa = oa;
    }
    public boolean hasMoreElements() { return ( i < oa.length ); }
    public Object  nextElement() {
	if (i < oa.length)
	    return oa[i++];
	else
	    throw new NoSuchElementException();
    }
}
