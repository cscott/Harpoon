// FIFO.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.EmptyStackException;
/**
 * The <code>FIFO</code> class represents a first-in-first-out
 * list of objects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FIFO.java,v 1.4.2.2 1999-06-17 21:00:53 cananian Exp $
 * @deprecated Use java.util.List instead.
 */

public class FIFO {

    class List {
	Object item;
	List next;
	List(Object item, List next) {
	    this.item = item; this.next = next;
	}
    }

    List head = null;
    List tail = null;

    /** 
     * Pushes an item onto the front of this fifo.
     * @param item the item to be pushed onto this stack.
     */
    public synchronized void push(Object item) {
	if (tail==null) { // empty.
	    tail = head = new List(item, null);
	} else {
	    tail.next = new List(item, null);
	    tail = tail.next;
	}
    }
    /**
     * Removes the object at the back of this fifo and returns that
     * object as the value of this function.
     * @return The object at the end of the fifo.
     * @exception EmptyStackException if this fifo is empty.
     */
    public synchronized Object pull() {
	Object obj = peek();

	head = head.next;
	if (head==null) tail=null;

	return obj;
    }
    /**
     * Looks at the object at the back of this fifo without removing it.
     * @return the object at the end of this fifo.
     * @exception EmptyStackException if this fifo is empty.
     */
    public synchronized Object peek() {
	if (head==null) throw new EmptyStackException();
	return head.item;
    }
    /**
     * Tests if this stack is empty.
     * @return <code>true</code> if this stack is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return (head==null);
    }
    /**
     * Copies contents of a FIFO into an array.
     */
    public void copyInto(Object[] oa) {
	int i=0;
	for (List l = head; l != null; l = l.next)
	    oa[i++] = l.item;
    }
    /**
     * Returns an enumeration of the contents of the FIFO.
     */
    public Enumeration elements() {
	return new Enumeration() {
	    List l = head;
	    public boolean hasMoreElements() { return (l!=null); }
	    public Object  nextElement() {
		Object o = l.item;
		l = l.next;
		return o;
	    }
	};
    }
}
