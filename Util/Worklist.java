// Worklist.java, created Sat Sep 12 19:38:31 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * A <code>Worklist</code> is a unique set.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Worklist.java,v 1.2.2.3 1999-02-05 23:09:05 pnkfelix Exp $
 */
public interface Worklist  {
   
    /** Pushes an item onto the Worklist if it is not already there. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>item</code> is not already an
	                     element of <code>this</code>, adds
			     <code>item</code> to this.  Else does
			     nothing. 
     */
    public void push(Object item);

    /** Removes an item from the Worklist and return it. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>item</code> is an element of
	                     <code>this</code>, removes
			     <code>item</code> from this.  Else does
			     nothing.     
    */
    public Object pull();

    /** Determines if the Worklist contains an item.
	<BR> <B>effects:</B> If <code>item</code> is an element of 
	                     <code>this</code>, returns true.
			     Else returns false.
    */
    public boolean contains(Object item);

    /** Determines if there are any more items left in the Worklist. 
	<BR> <B>effects:</B> If <code>this</code> has any elements,
	                     returns true.  Else returns false.
    */
    public boolean isEmpty();
}

