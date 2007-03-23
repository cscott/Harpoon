// SortedMapComparator.java, created Mon Sep 17 16:57:49 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import net.cscott.jutil.Default;
/**
 * A <code>SortedMapComparator</code> compares two sorted maps
 * entry-by-entry (treating the map as a sorted pair list).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SortedMapComparator.java,v 1.5 2007-03-23 23:06:18 cananian Exp $
 */
public class SortedMapComparator<K,V> implements Comparator<SortedMap<K,V>> {
    final Comparator<? super K> keyComparator;
    final Comparator<? super V> valueComparator;

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
    public SortedMapComparator(Comparator<? super K> keyComparator,
			       Comparator<? super V> valueComparator) {
	/* XXX: JAVAC
	this.keyComparator = (keyComparator==null) ?
	    ((Comparator<? super K>)Default.comparator) : keyComparator;
	this.valueComparator = (valueComparator==null) ?
	    ((Comparator<? super V>)Default.comparator) : valueComparator;
	*/
	if (keyComparator==null)
	    this.keyComparator = (Comparator<K>) Default.comparator;
	else
	    this.keyComparator = keyComparator;
	if (valueComparator==null)
	    this.valueComparator = (Comparator<V>) Default.comparator;
	else
	    this.valueComparator = valueComparator;
    }

    public int compare(SortedMap<K,V> sm1, SortedMap<K,V> sm2) {
	// throws ClassCastException if objects are not the proper types.
	Iterator<Map.Entry<K,V>> it1 = sm1.entrySet().iterator();
	Iterator<Map.Entry<K,V>> it2 = sm2.entrySet().iterator();
	while (it1.hasNext() && it2.hasNext()) {
	    Map.Entry<K,V> me1 = it1.next();
	    Map.Entry<K,V> me2 = it2.next();
	    int kcmp = keyComparator.compare(me1.getKey(), me2.getKey());
	    if (kcmp!=0) return kcmp;
	    int vcmp = valueComparator.compare(me1.getValue(), me2.getValue());
	    if (vcmp!=0) return vcmp;
	}
	// for maps of unequal size, the shorter is the smaller.
	return it1.hasNext() ? 1 : it2.hasNext() ? -1 : 0;
    }
}
