// WeightedSet.java, created Wed Jun 21  3:22:28 2000 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import net.cscott.jutil.SetWrapper;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

/** wrapper around set with an associated weight.
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: WeightedSet.java,v 1.4 2007-03-23 23:06:17 cananian Exp $
*/
class WeightedSet<E> extends SetWrapper<E>
    implements Comparable<WeightedSet<E>> {
    private final Set<E> s;
    Set temps; // mutable outside this class
    final int weight;
    WeightedSet(Set<E> s, int i) {
	this.s = s;
	this.weight = i;
    }
    protected Set<E> wrapped() { return s; }
    public int compareTo(WeightedSet<E> s) {
	return (s.weight - this.weight);
    } 
    public boolean equals(Object o) {
	try {
	    WeightedSet ws = (WeightedSet) o;
	    return (super.equals(ws) &&
		    this.weight == ws.weight);
	} catch (ClassCastException e) {
	    return false;
	}
    }
    public String toString() { 
	return "<Set:"+super.toString()+
	    ",Weight:"+weight+
	    (temps==null?"":(",Temps:"+temps))+">"; 
    }
}
