// Default.java, created Thu Apr  8 02:22:56 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Comparator;

/**
 * <code>Default</code> contains one-off or 'standard, no-frills'
 * implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Default.java,v 1.1.2.1 1999-04-08 06:56:39 cananian Exp $
 */
public abstract class Default  {
    /* A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    public static final Comparator comparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    return ((Comparable)o1).compareTo(o2);
	}
    };
}
