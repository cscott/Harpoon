// SESE.java, created Mon Mar  1 23:52:29 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Util.Util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>SESE</code> computes nested single-entry single-exit regions
 * from a cycle-equivalency set.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SESE.java,v 1.1.2.1 1999-03-02 06:44:08 cananian Exp $
 */
public class SESE  {
    public final Region top_level=new Region();  // top-level region.

    /** Creates a <code>SESE</code> from a <code>CycleEq</code>. */
    public SESE(CycleEq ceq) {
	Map entryRegion= new HashMap();
        Map exitRegion = new HashMap();
	for (Iterator it=ceq.cdClasses().iterator(); it.hasNext(); ) {
	    List l = (List) it.next();
	    Object prev=null;
	    for (Iterator lit=l.listIterator(); lit.hasNext(); ) {
		Object o = lit.next();
		if (prev!=null) {
		    Region r = new Region(prev, o);
		    entryRegion.put(prev, r);
		    exitRegion.put(o, r);
		}
		prev = o;
	    }
	}
	Region currentRegion=top_level;
	for (Iterator it=ceq.elements().iterator(); it.hasNext(); ) {
	    Object o = it.next();
	    Region r1 = (Region) entryRegion.get(o);
	    Region r2 = (Region) exitRegion.get(o);
	    if (currentRegion.equals(r1)) {
		currentRegion=currentRegion.parent; // pop up one level.
		r1=null;
	    }
	    if (currentRegion.equals(r2)) {
		currentRegion=currentRegion.parent; // pop up one level
		r2=null;
	    }
	    if (r1!=null) { Region.link(currentRegion, r1); currentRegion=r1; }
	    if (r2!=null) { Region.link(currentRegion, r2); currentRegion=r2; }
	}
	Util.assert(currentRegion.isTopLevel()); // top-level again.
    }

    public Iterator iterator() {
	return new Iterator() {
	    LinkedList ll = new LinkedList();
	    { ll.add(top_level); }
	    public boolean hasNext() { return !ll.isEmpty(); }
	    public Object next() {
		Region r = (Region) ll.removeFirst();
		ll.addAll(r.children());
		return r;
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }
    
    public void print(java.io.PrintWriter pw) {
	// iterator goes down level at a time.
	// add every other level to done Set; use alternation to determine
	// when we've reached the next level.
	Set done = new HashSet(); boolean inSet=true; int level=0;
	for (Iterator it=iterator(); it.hasNext(); ) {
	    Region r = (Region) it.next();
	    if (done.contains(r.parent) == inSet) { level++; inSet = !inSet; }
	    if (inSet) done.add(r);
	    for (int i=0; i<level; i++)
		pw.print(" ");
	    pw.println(r.toString());
	}
    }

    public static class Region {
	// entry and exit nodes of the region.
	public final Object entry, exit;
	// tree info.
	Region parent=null;
	RegionList children=null;

	Region() { // create top-level region.
	    this.entry = this.exit = null;
	    Util.assert(isTopLevel());
	}
	Region(Object entry, Object exit) {
	    this.entry = entry; this.exit = exit;
	    Util.assert(!isTopLevel());
	}

	public Region parent() { return this.parent; }
	public Set children() { return RegionList.asSet(this.children); }

	boolean isTopLevel() {
	    return (this.entry==null) && (this.exit==null);
	}
	public boolean equals(Object o) {
	    if (!(o instanceof Region)) return false;
	    if (isTopLevel()) return ((Region)o).isTopLevel();
	    else return
		     this.entry.equals(((Region)o).entry) &&
		     this.exit.equals(((Region)o).exit);
	}
	public int hashCode() {
	    if (isTopLevel()) return 0;
	    else return (entry.hashCode()<<7) ^ exit.hashCode();
	}
	public String toString() {
	    if (isTopLevel()) return "[-toplevel-]";
	    else return "["+entry+"->"+exit+"]";
	}
	static void link(Region parent, Region child) {
	    child.parent = parent;
	    parent.children = new RegionList(child, parent.children);
	}
    }
    static class RegionList {
	final Region region;
	final RegionList next;
	final int size;
	RegionList(Region region, RegionList next) {
	    this.region = region; this.next = next; 
	    this.size = 1+((next==null)?0:next.size);
	}
	static Iterator elements(final RegionList rl) {
	    return new Iterator() {
		RegionList rlp = rl;
		public boolean hasNext() { return rlp!=null; }
		public Object next() {
		    Region r = rlp.region; rlp=rlp.next; return r;
		}
		public void remove() {
		    throw new UnsupportedOperationException();
		}
	    };
	}
	static Set asSet(final RegionList rl) {
	    return new AbstractSet() {
		public boolean isEmpty() { return rl==null; }
		public int size() { return (rl==null)?0:rl.size; }
		public Iterator iterator() { return elements(rl); }
	    };
	}
    }
}
