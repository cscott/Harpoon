// IllegalEdgeException.java, created Thu Jan 14 15:34:50 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>IllegalEdgeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: IllegalEdgeException.java,v 1.1.2.4 1999-08-04 05:52:21 cananian Exp $
 */

public class IllegalEdgeException extends RuntimeException {
    
    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException() {
        super();
    }

    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException(String s) {
        super(s);
    }
    
}
