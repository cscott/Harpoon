// MapComparator.java, created Mon Sep 17 17:16:46 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.cscott.jutil.Default;
/**
 * A <code>MapComparator</code> compares two unsorted maps by first
 * sorting their keys and then comparing them entry-by-entry (treating
 * the map as a sorted pair list).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MapComparator.java,v 1.3 2004-02-08 01:56:15 cananian Exp $
 */
public class MapComparator<K,V> implements Comparator<Map<K,V>> {
    final Comparator<Map.Entry<K,V>> entryComparator;
    final Comparator<List<Map.Entry<K,V>>> listComparator;

    /** Creates a <code>MapComparator</code> which compares
     *  entries in the order defined by the <code>keyComparator</code>
     *  and compares keys (using <code>keyComparator</code>) before values
     *  (using <code>valueComparator</code>).
     *  If <code>keyComparator</code> is <code>null</code>, then all
     *  keys must implement <code>java.lang.Comparable</code>.
     *  If <code>valueComparator</code> is <code>null</code>, then all
     *  values must implement <code>java.lang.Comparable</code>.
     */
    public MapComparator(Comparator<? super K> keyComparator,
			 Comparator<? super V> valueComparator) {
	if (keyComparator==null) keyComparator = Default.<K>comparator();
	if (valueComparator==null) valueComparator = Default.<V>comparator();
	this.entryComparator =
	    new EntryComparator<K,V>(keyComparator, valueComparator);
	this.listComparator =
	    new ListComparator<Map.Entry<K,V>>(true, entryComparator);
    }

    public int compare(Map<K,V> m1, Map<K,V> m2) {
	ArrayList<Map.Entry<K,V>>
	    al1 = new ArrayList<Map.Entry<K,V>>(m1.entrySet()),
	    al2 = new ArrayList<Map.Entry<K,V>>(m2.entrySet());
	Collections.sort(al1, entryComparator);
	Collections.sort(al2, entryComparator);
	return listComparator.compare(al1, al2);
    }
    private static class EntryComparator<K,V> 
	implements Comparator<Map.Entry<K,V>> {
	final Comparator<? super K> keyComparator;
	final Comparator<? super V> valueComparator;
	EntryComparator(Comparator<? super K> keyComparator,
			Comparator<? super V> valueComparator) {
	    this.keyComparator = keyComparator;
	    this.valueComparator = valueComparator;
	}
	public int compare(Map.Entry<K,V> me1, Map.Entry<K,V> me2) {
	    int c = keyComparator.compare(me1.getKey(), me2.getKey());
	    if (c!=0) return c;
	    return valueComparator.compare(me1.getValue(), me2.getValue());
	}
    }
}
