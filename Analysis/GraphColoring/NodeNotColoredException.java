// NodeNotColoredException.java, created Wed Jan 13 17:30:19 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotColoredException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotColoredException.java,v 1.1.2.4 1999-08-04 05:52:21 cananian Exp $
 */

public class NodeNotColoredException extends RuntimeException {
    
    /** Creates a <code>NodeNotColoredException</code>. */
    public NodeNotColoredException() {
        super();
    }

    /** Creates a <code>NodeNotColoredException</code>. */
    public NodeNotColoredException(String s) {
        super(s);
    }
    
}
