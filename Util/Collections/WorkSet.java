// WorkSet.java, created Tue Feb 23 01:18:37 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Worklist;

/**
 * A <code>WorkSet</code> is a <code>Set</code> offering constant-time
 * access to the first/last element inserted, and an iterator whose speed
 * is not dependent on the total capacity of the underlying hashtable.
 * <p>Conforms to the JDK 1.2 Collections API.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: WorkSet.java,v 1.7 2004-02-08 01:56:38 cananian Exp $
 */
public class WorkSet<E> extends net.cscott.jutil.WorkSet<E> implements Worklist<E> {
    public WorkSet() { super(); }
    /** Constructs a new, empty <code>WorkSet</code> with the specified
     *  initial capacity and default load factor. */
    public WorkSet(int initialCapacity) { super(initialCapacity); }
    /** Constructs a new, empty <code>WorkSet</code> with the specified
     *  initial capacity and the specified load factor. */
    public WorkSet(int initialCapacity, float loadFactor) {
	super(initialCapacity, loadFactor);
    }
    /** Constructs a new <code>WorkSet</code> with the contents of the
     *  specified <code>Collection</code>. */
    public WorkSet(java.util.Collection<? extends E> c) {
	super(c);
    }

    /** Looks at the object as the top of this <code>WorkSet</code>
     *  (treating it as a <code>Stack</code>) without removing it
     *  from the set/stack. */
    public E peek() { return getLast(); }

    /** Removes some item from this and return it (Worklist adaptor
	method). 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If there exists an <code>Object</code>,
	                     <code>item</code>, that is an element of
			     <code>this</code>, removes
			     <code>item</code> from <code>this</code>
			     and returns <code>item</code>. Else does
			     nothing.
    */
    public E pull() { return removeLast(); }

    /** Removes the item at the top of this <code>WorkSet</code>
     *  (treating it as a <code>Stack</code>) and returns that object
     *  as the value of this function. */
    public E pop() { return removeLast(); }

    /** Pushes item onto the top of this <code>WorkSet</code> (treating
     *  it as a <code>Stack</code>), if it is not already there.
     *  If the <code>item</code> is already in the set/on the stack,
     *  then this method does nothing.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>item</code> is not already an
	                     element of <code>this</code>, adds
			     <code>item</code> to <code>this</code>.
			     Else does nothing. 
    */
    public void push(E item) {
	this.add(item);
    }
}
