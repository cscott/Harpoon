// SESE.java, created Mon Mar  1 23:52:29 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * <code>SESE</code> computes nested single-entry single-exit regions
 * from a cycle-equivalency set.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SESE.java,v 1.1.2.3 1999-03-02 09:56:20 cananian Exp $
 */
public class SESE  {
    /** Root of <code>Region</code> tree. */
    public final Region topLevel=new Region();
    /** Mapping from Nodes/Edges to smallest enclosing canonical SESE
     *  <code>Region</code>. */
    public /*final*/ Map smallestSESE;

    /** Creates a <code>SESE</code> from a <code>CycleEq</code>. */
    public SESE(HCode hc, boolean edgegraph) {
	// compute cycle equivalence.
	CycleEq ceq = new CycleEq(hc, edgegraph);
	// use cycle equivalence classes to determine canonical sese regions.
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
	// now do DFS traversal of CFG to determine nesting of regions.
	Map workSmallSESE = new HashMap();
	// state for DFS
	Set visited = new HashSet();  visited.add(hc.getRootElement());
	Stack nodeS = new Stack();    nodeS.push(hc.getRootElement());
	Stack regionS=new Stack();    regionS.push(topLevel);

	while (!nodeS.isEmpty()) {
	    Util.assert(nodeS.size()==regionS.size());
	    Object o = nodeS.pop(); // either an HCodeElement or an HCodeEdge
	    Region cR= (Region) regionS.pop(); // currentRegion.
	    // deal with region entry/exit.
	    if (edgegraph==(o instanceof HCodeElement)) {
		workSmallSESE.put(o, cR); // cR is smallest enclosing SESE.
	    } else { // update current region.
		// if this is found in the cycle-equivalency stuff, push/pop.
		Region r1 = (Region) entryRegion.get(o);
		Region r2 = (Region) exitRegion.get(o);
		// pop up one level if we are leaving the region.
		if (cR.equals(r1)) { cR=cR.parent; r1=null; }
		if (cR.equals(r2)) { cR=cR.parent; r2=null; }
		// push a level if we are entering the region.
		if (r1!=null) { Region.link(cR, r1); cR=r1; }
		if (r2!=null) { Region.link(cR, r2); cR=r2; }
	    }
	    // continue DFS traversal.
	    Object[] succ=null;
	    if (o instanceof HCodeElement) // Node.  Push edges.
		succ = ((harpoon.IR.Properties.Edges)o).succ();
	    if (o instanceof HCodeEdge) // Edge.  Push node.
		succ = new HCodeElement[] { ((HCodeEdge)o).to() };
	    for (int i=succ.length-1; i>=0; i--)
		if (!visited.contains(succ[i])) {
		    nodeS.push(succ[i]); regionS.push(cR);
		    visited.add(succ[i]);
		}
	} // END while.
	Util.assert(nodeS.isEmpty() && regionS.isEmpty());
	// make smallestSESE map unmodifiable.
	smallestSESE = Collections.unmodifiableMap(workSmallSESE);
    }

    /** Iterate through SESE regions, top-down.  All top-level regions
     *  are visited first, then all children of top-level regions, then
     *  all grandchildren, then all great-grandchildren, etc. */
    public Iterator iterator() {
	return new Iterator() {
	    LinkedList ll = new LinkedList();
	    { ll.add(topLevel); }
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
	    indent(pw, level); pw.println(r.toString());
	    // now print members.
	    indent(pw, level); pw.print('(');
	    Map members = new HashMap(smallestSESE);
	    members.values().retainAll(Collections.singleton(r));
	    for (Iterator it2=members.keySet().iterator(); it2.hasNext(); ) {
		pw.print(it2.next().toString());
		if (it2.hasNext()) pw.print(' ');
	    }
	    pw.println(')');
	}
    }
    private void indent(java.io.PrintWriter pw, int howmuch) {
	for (int i=0; i<howmuch; i++)
	    pw.print(' ');
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
