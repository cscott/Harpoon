// SingletonEnumerator.java, created Sat Sep 19 04:39:48 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * <code>SingletonEnumerator</code> enumerates a single value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SingletonEnumerator.java,v 1.3 2002-02-25 21:08:55 cananian Exp $
 * @deprecated Use harpoon.Util.Default.singletonIterator instead.
 */

public class SingletonEnumerator implements Enumeration {
    final Object o;
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
