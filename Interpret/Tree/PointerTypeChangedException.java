// PointerTypeChangedException.java, created Sat Mar 27 17:05:09 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

/**
 * This exception is thrown when the type of a <code>Pointer</code>
 * within the Tree interpreter is changed.  This is necessary because 
 * the type of a <code>Pointer</code> cannot be determined when it is
 * created.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: PointerTypeChangedException.java,v 1.2 2002-02-25 21:06:01 cananian Exp $
 */
public class PointerTypeChangedException extends RuntimeException {
    /** The <code>Pointer</code> whose type is changed. */
    public Pointer ptr;

    /** Class constructor */
    public PointerTypeChangedException(Pointer ptr) {
	super();
	this.ptr = ptr;
    }
}
