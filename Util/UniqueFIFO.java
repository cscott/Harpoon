// UniqueFIFO.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Hashtable;
import java.util.EmptyStackException;
/**
 * The <code>UniqueFIFO</code> class represents a first-in-first-out
 * list of <b>unique</b> objects.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UniqueFIFO.java,v 1.4.2.2 1999-06-17 21:00:53 cananian Exp $
 * @deprecated Does not conform to the JDK 1.2 Collection API.
 */

public class UniqueFIFO extends FIFO implements Worklist {

    Hashtable uniq = new Hashtable();

    /**
     * Determines whether this fifo contains an object.
     * @return <code>true</code> if the fifo contains <code>item</code>,
     *         <code>false</code> otherwise.
     */
    public boolean contains(Object item) {
	return uniq.containsKey(item);
    }

    /** 
     * Pushes an item onto the front of this fifo, if it is unique.
     * Otherwise, does nothing.
     * @param item the item to be pushed onto this stack.
     */
    public synchronized void push(Object item) {
	if (!uniq.containsKey(item)) {
	    uniq.put(item, item);
	    super.push(item);
	}
    }
    /**
     * Removes the object at the back of this fifo and returns that
     * object as the value of this function.
     * @return The object at the end of the fifo.
     * @exception EmptyStackException if this fifo is empty.
     */
    public synchronized Object pull() {
	Object obj = super.pull();
	uniq.remove(obj);
	return obj;
    }
    /**
     * Looks at the object at the back of this fifo without removing it.
     * @return the object at the end of this fifo.
     * @exception EmptyStackException if this fifo is empty.
     */
    public synchronized Object peek() {
	return super.peek();
    }
}
