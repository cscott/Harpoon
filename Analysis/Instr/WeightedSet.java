package harpoon.Analysis.Instr;

import harpoon.Util.Collections.CollectionWrapper;

import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

/** wrapper around set with an associated weight. */
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
