// NodeNotColoredException.java, created Wed Jan 13 17:30:19 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotColoredException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: NodeNotColoredException.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
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
