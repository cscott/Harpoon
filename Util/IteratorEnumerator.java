// IteratorEnumerator.java, created Tue Feb 23 02:06:53 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.Iterator;
/**
 * An <code>IteratorEnumerator</code> converts an <code>Iterator</code>
 * into an <code>Enumeration</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IteratorEnumerator.java,v 1.2 2002-02-25 21:08:45 cananian Exp $
 */
public class IteratorEnumerator implements Enumeration {
    private final Iterator i;
    /** Creates a <code>IteratorEnumerator</code>. */
    public IteratorEnumerator(Iterator i) { this.i = i; }
    public boolean hasMoreElements() { return i.hasNext(); }
    public Object nextElement() { return i.next(); }
}
