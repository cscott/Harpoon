// CombineEnumerator.java, created Wed Oct 14 08:50:22 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * A <code>CombineEnumerator</code> combines several different
 * <code>Enumeration</code>s into one.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CombineEnumerator.java,v 1.3 2002-02-25 21:08:45 cananian Exp $
 * @deprecated Use harpoon.Util.CombineIterator instead.
 */

public class CombineEnumerator implements Enumeration {
    final Enumeration[] ea;
    int i=0;
    /** Creates a <code>CombineEnumerator</code>. */
    public CombineEnumerator(Enumeration[] ea) {
        this.ea = ea;
    }
    private void adv() {
	while (i < ea.length && !ea[i].hasMoreElements() )
	    i++;
    }
    public Object nextElement() {
	if (hasMoreElements())
	    return ea[i].nextElement();
	else
	    throw new NoSuchElementException();
    }
    public boolean hasMoreElements() {
	adv();
	return (i<ea.length);
    }
}
