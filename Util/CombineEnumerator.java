// CombineEnumerator.java, created Wed Oct 14 08:50:22 1998 by cananian
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * A <code>CombineEnumerator</code> combines several different
 * <code>Enumeration</code>s into one.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CombineEnumerator.java,v 1.1 1998-10-16 06:21:23 cananian Exp $
 */

public class CombineEnumerator implements Enumeration {
    final Enumeration[] ea;
    int i=0;
    /** Creates a <code>CombineEnumerator</code>. */
    public CombineEnumerator(Enumeration[] ea) {
        this.ea = ea;
    }
    public Object nextElement() {
	while (!ea[i].hasMoreElements() && i < ea.length)
	    i++;
	if (i < ea.length)
	    return ea[i].nextElement();
	else
	    throw new NoSuchElementException();
    }
    public boolean hasMoreElements() {
	while (!ea[i].hasMoreElements() && i < ea.length)
	    i++;
	return (i<ea.length);
    }
}
