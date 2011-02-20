// NodeNotRemovedException.java, created Wed Jan 13 14:54:54 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotRemovedException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: NodeNotRemovedException.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
 */

public class NodeNotRemovedException extends RuntimeException {
    
    /** Creates a <code>NodeNotRemovedException</code>. */
    public NodeNotRemovedException() {
        super();
    }

    /** Creates a <code>NodeNotRemovedException</code>. */
    public NodeNotRemovedException(String s) {
        super(s);
    }
    
}
