// Tuple.java, created Sat Oct 10 01:37:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.*;
/**
 * A <code>Tuple</code> is an ordered list of objects that works
 * properly in Hashtables & etc.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tuple.java,v 1.2 1998-10-11 02:37:59 cananian Exp $
 */

public class Tuple  {
    Object elements[];
    /** Creates a <code>Tuple</code>. */
    public Tuple(Object[] elements) {
        this.elements = elements;
    }
    public int hashCode() {
	int hc = elements.length;
	for (int i=0; i<elements.length; i++)
	    hc ^= elements[i].hashCode();
	return hc;
    }
    public boolean equals(Object obj) {
	if (!(obj instanceof Tuple)) return false;
	Tuple t = (Tuple) obj;
	if (this.elements.length != t.elements.length) return false;
	for (int i=0; i<elements.length; i++)
	    if (!this.elements[i].equals(t.elements[i]))
		return false;
	return true;
    }
}
