// WeightedSet.java, created Wed Jun 21  3:22:28 2000 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Util.Collections.CollectionWrapper;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

/** wrapper around set with an associated weight.
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: WeightedSet.java,v 1.1.2.2 2001-06-17 22:29:56 cananian Exp $
*/
class WeightedSet extends CollectionWrapper implements Set, Comparable {
    Set temps;
    int weight;
    WeightedSet(Set s, int i) {
	super(s);
	this.weight = i;
    }
    public int compareTo(Object o) {
	WeightedSet s = (WeightedSet) o;
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
