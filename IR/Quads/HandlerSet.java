// HandlerSet.java, created Wed Dec 23 02:37:47 1998 by cananian
package harpoon.IR.Quads;

import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * A <code>HandlerSet</code> is a linked list of handlers, for purposes
 * of comparison.  See the <code>equals()</code> method.  Used by
 * both <code>Translate</code> and <code>Print</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HandlerSet.java,v 1.1.2.1 1998-12-23 22:18:48 cananian Exp $
 */
final class HandlerSet {
    final private HANDLER h;
    final private HandlerSet next;
    HandlerSet(HANDLER h, HandlerSet next) {
	Util.assert(h!=null /*&& !contains(next, h)*/);
	this.h = h; this.next = next;
    }
    /** Determines if this <code>HandlerSet</code> contains a given
     *  <code>HANDLER</code>. */
    public final boolean contains(HANDLER h) { return contains(this, h); }
    /** Returns an enumeration of the <code>HANDLER</code>s in this
     *  <code>HandlerSet</code>. */
    public final Enumeration elements() { return elements(this); }
    /** Determines if an object is equal to this <code>HandlerSet</code>. */
    public final boolean equals(Object o) {
	return (o instanceof HandlerSet) ? equals(this, (HandlerSet)o) : false;
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
    /** Returns an enumeration of the <code>HANDLER</code>s in this
     *  <code>HandlerSet</code>. */
    public static final Enumeration elements(final HandlerSet hs) {
	return new Enumeration() {
	    HandlerSet hsp = hs;
	    public boolean hasMoreElements() { return (hsp!=null); }
	    public Object  nextElement() {
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
