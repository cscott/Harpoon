// SingletonEnumerator.java, created Sat Sep 19 04:39:48 1998 by cananian
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * <code>SingletonEnumerator</code> enumerates a single value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SingletonEnumerator.java,v 1.1 1998-09-19 08:44:35 cananian Exp $
 */

public class SingletonEnumerator implements Enumeration {
    Object o;
    boolean done=false;
    /** Creates a <code>SingletonEnumerator</code> which enumerates the
     *  single value <code>o</code>. */
    public SingletonEnumerator(Object o) {
        this.o = o;
    }
    public boolean hasMoreElements() { return !done; }
    public Object nextElement() {
	if (done) throw new NoSuchElementException();
	done = true;
	return o;
    }
}
