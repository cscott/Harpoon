// NullEnumerator.java, created Wed Sep 16 15:07:32 1998 by cananian
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * A <code>NullEnumerator</code> enumerates no elements.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NullEnumerator.java,v 1.1 1998-09-16 23:51:53 cananian Exp $
 */

public class NullEnumerator implements Enumeration {
    /** Creates a <code>NullEnumerator</code>. */
    public NullEnumerator() { }
    
    /** @return <code>false</code> */
    public boolean hasMoreElements() { return false; }
    /** @return <code>null</code> */
    public Object  nextElement() { throw new NoSuchElementException(); }

    /** A static instance of the null enumerator. */
    public static NullEnumerator STATIC = new NullEnumerator();
}
