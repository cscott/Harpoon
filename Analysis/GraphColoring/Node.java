// Node.java, created Wed Jan 13 15:54:49 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>Node</code> is an abstract representation of a node for use
 * with the Graph object.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Node.java,v 1.1.2.6 2001-06-17 22:29:39 cananian Exp $ */

public abstract class Node  {

    /** Creates a <code>Node</code>. */
    public Node() {
        
    }
    
    /** Modifiability check.
	<BR> <B>effects:</B> If <code>this</code> is allowed to be
	                     modified, returns true.  Else returns
			     false.
    */
    public boolean isModifiable() {
	return true;
    }

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public abstract String toString();
}
