// MapComparator.java, created Mon Sep 17 17:16:46 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
/**
 * A <code>MapComparator</code> compares two unsorted maps by first
 * sorting their keys and then comparing them entry-by-entry (treating
 * the map as a sorted pair list).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MapComparator.java,v 1.1.2.2 2001-09-17 23:08:45 cananian Exp $
 */
public class MapComparator implements Comparator {
    final Comparator entryComparator, listComparator;

    /** Creates a <code>MapComparator</code> which compares
     *  entries in the order defined by the <code>keyComparator</code>
     *  and compares keys (using <code>keyComparator</code>) before values
     *  (using <code>valueComparator</code>).
     *  If <code>keyComparator</code> is <code>null</code>, then all
     *  keys must implement <code>java.lang.Comparable</code>.
     *  If <code>valueComparator</code> is <code>null</code>, then all
     *  values must implement <code>java.lang.Comparable</code>.
     */
    public MapComparator(Comparator keyComparator,
			 Comparator valueComparator) {
	if (keyComparator==null) keyComparator = Default.comparator;
	if (valueComparator==null) valueComparator = Default.comparator;
	this.entryComparator =
	    new EntryComparator(keyComparator, valueComparator);
	this.listComparator = new ListComparator(true, entryComparator);
    }

    public int compare(Object o1, Object o2) {
	Map m1 = (Map) o1, m2 = (Map) o2;
	ArrayList al1 = new ArrayList(m1.entrySet());
	ArrayList al2 = new ArrayList(m2.entrySet());
	Collections.sort(al1, entryComparator);
	Collections.sort(al2, entryComparator);
	return listComparator.compare(al1, al2);
    }
    private static class EntryComparator implements Comparator {
	final Comparator keyComparator, valueComparator;
	EntryComparator(Comparator keyComparator, Comparator valueComparator) {
	    this.keyComparator = keyComparator;
	    this.valueComparator = valueComparator;
	}
	public int compare(Object o1, Object o2) {
	    Map.Entry me1 = (Map.Entry) o1, me2 = (Map.Entry) o2;
	    int c = keyComparator.compare(me1.getKey(), me2.getKey());
	    if (c!=0) return c;
	    return valueComparator.compare(me1.getValue(), me2.getValue());
	}
    }
}
