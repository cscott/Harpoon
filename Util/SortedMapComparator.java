// SortedMapComparator.java, created Mon Sep 17 16:57:49 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
/**
 * A <code>SortedMapComparator</code> compares two sorted maps
 * entry-by-entry (treating the map as a sorted pair list).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SortedMapComparator.java,v 1.1.2.1 2001-09-18 22:35:16 cananian Exp $
 */
public class SortedMapComparator implements Comparator {
    final Comparator keyComparator, valueComparator;

    /** Creates a <code>SortedMapComparator</code> which compares
     *  entries in the order defined by the <code>SortedMap</code> and
     *  compares keys before values.
     *  All keys and values must implement <code>java.lang.Comparable</code>.
     */
    public SortedMapComparator() { this(null, null); }
    /** Creates a <code>SortedMapComparator</code> which compares
     *  entries in the order defined by the <code>SortedMap</code>
     *  and compares keys (using <code>keyComparator</code>) before values
     *  (using <code>valueComparator</code>).
     *  If <code>keyComparator</code> is <code>null</code>, then all
     *  keys must implement <code>java.lang.Comparable</code>.
     *  If <code>valueComparator</code> is <code>null</code>, then all
     *  values must implement <code>java.lang.Comparable</code>.
     */
    public SortedMapComparator(Comparator keyComparator,
			       Comparator valueComparator) {
	this.keyComparator = (keyComparator==null) ?
	    Default.comparator : keyComparator;
	this.valueComparator = (valueComparator==null) ?
	    Default.comparator : valueComparator;
    }

    public int compare(Object o1, Object o2) {
	// throws ClassCastException if objects are not the proper types.
	SortedMap sm1 = (SortedMap) o1, sm2 = (SortedMap) o2;
	Iterator it1 = sm1.entrySet().iterator();
	Iterator it2 = sm2.entrySet().iterator();
	while (it1.hasNext() && it2.hasNext()) {
	    Map.Entry me1 = (Map.Entry) it1.next();
	    Map.Entry me2 = (Map.Entry) it2.next();
	    int kcmp = keyComparator.compare(me1.getKey(), me2.getKey());
	    if (kcmp!=0) return kcmp;
	    int vcmp = valueComparator.compare(me1.getValue(), me2.getValue());
	    if (vcmp!=0) return vcmp;
	}
	// for maps of unequal size, the shorter is the smaller.
	return it1.hasNext() ? 1 : it2.hasNext() ? -1 : 0;
    }
}
