// NodeNotRemovedException.java, created Wed Jan 13 14:54:54 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotRemovedException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotRemovedException.java,v 1.1.2.4 1999-08-04 05:52:21 cananian Exp $
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
