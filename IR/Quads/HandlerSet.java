// HandlerSet.java, created Wed Dec 23 02:37:47 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Util.Collections.UnmodifiableIterator;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * A <code>HandlerSet</code> is a linked list of handlers, for purposes
 * of comparison.  See the <code>equals()</code> method.  Used by
 * both <code>Translate</code> and <code>Print</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HandlerSet.java,v 1.5 2002-08-30 22:39:35 cananian Exp $
 */
final public class HandlerSet {
    final HANDLER h;
    final HandlerSet next;
    HandlerSet(HANDLER h, HandlerSet next) {
	assert h!=null /*&& !contains(next, h)*/;
	this.h = h; this.next = next;
    }
    /** Determines if this <code>HandlerSet</code> contains a given
     *  <code>HANDLER</code>. */
    final boolean contains(HANDLER h) { return contains(this, h); }
    /** Returns an enumeration of the <code>HANDLER</code>s in this
     *  <code>HandlerSet</code>.
     * @deprecated Use iterator() instead. */
    final Enumeration elements() { return elements(this); }
    /** Returns an iteration over the <code>HANDLER</code>s in this
     *  <code>HandlerSet</code>. */
    final Iterator iterator() { return iterator(this); }
    /** Determines if an object is equal to this <code>HandlerSet</code>. */
    public final boolean equals(Object o) {
	HandlerSet hs;
	try{ hs=(HandlerSet)o; } catch (ClassCastException e) { return false; }
	return equals(this, hs);
    }
    /** Computes a hashcode for this <code>HandlerSet</code>. */
    public final int hashCode() {
	return h.hashCode() ^ ((next!=null)?(next.hashCode()<<1):0);
    }
    /** Determines if two <code>HandlerSet</code>s are equal. */
    public static final boolean equals(HandlerSet h1, HandlerSet h2) {
	if (h1==null || h2==null) return (h1==h2);
	else return (h1.h==h2.h) ? equals(h1.next,h2.next) : false;
    }
    /** Returns an enumeration of the <code>HANDLER</code>s in the given
     *  <code>HandlerSet</code>. 
     *  @deprecated Use iterator(hs) instead.
     */
    public static final Enumeration elements(final HandlerSet hs) {
	return new IteratorEnumerator(iterator(hs));
    }
    /** Returns an iterator over the <code>HANDLER</code>s in the given
     *  <code>HandlerSet</code>. */
    public static final Iterator iterator(final HandlerSet hs) {
	return new UnmodifiableIterator() {
	    HandlerSet hsp = hs;
	    public boolean hasNext() { return (hsp!=null); }
	    public Object  next() {
		try { HANDLER h=hsp.h; hsp=hsp.next; return h; }
		catch (NullPointerException e)
		{ throw new NoSuchElementException(); }
	    }
	};
    }
    /** Determines if a given <code>HandlerSet</code> contains a given
     *  <code>HANDLER</code>. */
    public static final boolean contains(HandlerSet hs, HANDLER h) {
	return (hs==null)?false:(hs.h==h)?true:contains(hs.next, h);
    }
}
