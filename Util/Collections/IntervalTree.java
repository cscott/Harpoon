// IntervalTree.java, created Tue Jul  24 23:17:23 2001 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.CombineIterator;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.Comparator;
import java.util.Iterator;

/** An <code>IntervalTree</code> is a mutable collection
    of <code>Interval</code>s.  <code>IntervalTree</code>s support 
    efficient lookup of overlapping elements.

    <p> Every element in an <code>IntervalTree</code> must be an
    <code>Interval</code>.  Thus element lookup is done based upon
    <code>Interval</code>s, not on the datum assocatied with each
    <code>Interval</code>.
    
    <p> The intervals maintained by this structure are considered to
    be closed intervals.

    <p> To make use of an <code>IntervalTree</code> cleaner, a
    convenience method, <code>addInterval</code> is provided.
    @see IntervalTree#addInterval

    @see "CLR section 15.3, (page 290)."  */
public class IntervalTree extends RedBlackTree { 

    /** Immutable record representing a closed interval
        [<code>low</code>,<code>high</code>] holding an object
        <code>obj</code>. 
    */
    public static class Interval {
	static Comparator COMPARE = new Comparator() {
	    public int compare(Object a, Object b) {
		return ((Interval)a).low - ((Interval)b).low; }};

	public final int low; 
	public final int high;
	public final Object datum;
	public Interval(Object datum, int low, int high) {
	    this.low = low; 
	    this.high = high;
	    this.datum = datum;
	}
	
	public boolean overlaps(Interval i) {
	    return this.low <= i.high && this.high >= i.low;
	}
    }

    private class IntervalNode extends RBNode {
	// max value of any interval endpoint stored in the
	// subtree rooted at this.
	protected int max;
	IntervalNode(Object o) { 
	    super(o); 
	    Interval i = (Interval) o;
	    if (i != null) 
		this.max = i.high;
	}
    }
    /** requires: o is-a <code>Interval</code>. */
    protected Node makeNode(Object o) { return new IntervalNode(o); }

    protected Node setLeft(Node p, Node l) { 
	Node n = super.setLeft(p,l); fixMax(p); return n;
    }
    protected Node setRight(Node p, Node r) { 
	Node n = super.setRight(p,r); fixMax(p); return n;
    }
    protected Node setParent(Node c, Node p) { 
	Node n = super.setParent(c,p); fixMax(p); return n;
    }
    // bug fix from: Kim Roland Rasmussen <kim.rasmussen@medgen.ku.dk>
    // thanks!
    private void fixMax(Node p) {
        if (p==NIL)
            return;

        if (p.left() == NIL || p.right() == NIL)
            setMax(p, interval(p).high);
        else
            setMax(p, Math.max( max(p.left()), max(p.right()) ));
    }
    private void setMax(Node n, int m) { ((IntervalNode) n).max = m; }
    
    private Interval interval(Node n) { return (Interval) n.key; }
    private int max(Node n) { return ((IntervalNode) n).max; }

    /** Constructs a new empty <code>IntervalTree</code>. */
    public IntervalTree() { super(Interval.COMPARE); }
    
    /** Returns some <code>Interval</code> in <code>this</code> which
	overlaps the bounds defined by the argument interval
	<code>i</code>, or <code>null</code> if no such interval
	exists.  
	<p> This operation is named "Interval-Search" in CLR
	@see CLR, pg 291
    */
    public Interval searchOverlapping(Interval i) {
	return searchOverlapping(root(),i);
    }
    // finds an overlapping interval in the tree rooted at x, if one exists. 
    private Interval searchOverlapping(Node x, Interval i) {
	while (x != NIL && !i.overlaps(interval(x))) {
	    if (x.left() != NIL && max(x.left()) >= i.low) 
		x = x.left();
	    else
		x = x.right();
	}

	if (x == NIL) 
	    return null;
	else 
	    return interval(x);
    }

    /** Returns an <code>Iterator</code> over <code>Intervals</code> that 
	yields every interval in this that overlaps <code>i</code>. 
    */
    public Iterator allOverlapping(Interval i) {
	return allOverlapping( root(), i );
    }
    private Iterator allOverlapping(Node x, Interval i) {
	if (x == NIL) {
	    return Default.nullIterator;
	} else if (interval(x).overlaps(i)) {
	    return new CombineIterator(new Iterator[] {
		Default.singletonIterator(interval(x)),
		allOverlapping(x.left(), i),
		allOverlapping(x.right(), i)
	    });
	} else if (x.left() != NIL && max(x.left()) >= i.low) {
	    // FSK: I suspect there is a asymptotically more efficient
	    // way to handle this case, but haven't worked it out yet.
	    return new CombineIterator( allOverlapping(x.left(), i),
					allOverlapping(x.right(), i));
	} else {
	    // see CLR, pg. 293, Theorem 15.2, case 2. for justification. 
	    return allOverlapping(x.right(), i);
	}
    }
    /** Constructs a new <code>Interval</code> <i>i</i> and adds
	<i>i</i> to <code>this</code>. 
	Convenience method.
	Requires: high > low.
	<code>datum</code> can be null.
    */
    public Interval addInterval(Object datum, int low, int high) {
	Interval i = new Interval(datum, low, high);
	add(i);
	return i;
    }

    
    public static void main(String[] args) {
	IntervalTree t = new IntervalTree();
	
	// using example data from CLR pg 291
	addTestIval( t,  0,  3);

	addTestIval( t,  5,  8);
	addTestIval( t, 15, 23);

	addTestIval( t,  6, 10);
	addTestIval( t, 16, 21);

	addTestIval( t,  8,  9);
	addTestIval( t, 17, 19);
	addTestIval( t, 25, 30);

	addTestIval( t, 19, 20);
	addTestIval( t, 26, 26);

	System.out.println(t.dump());
	testSearchOverlap(t,22,25);
	testSearchOverlap(t,11,14);
    }

    private static void addTestIval(IntervalTree t, int low, int high) {
	t.addInterval(low+"-"+high, low, high);
    }
    private static void testSearchOverlap(IntervalTree t, int low, int high) {
	System.out.println("overlaps "+low+"-"+high+" : "+
			   t.searchOverlapping(new Interval(null, low, high)));
    }
    
}
