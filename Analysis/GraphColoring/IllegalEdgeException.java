// IllegalEdgeException.java, created Thu Jan 14 15:34:50 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>IllegalEdgeException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: IllegalEdgeException.java,v 1.1.2.5 2001-06-17 22:29:38 cananian Exp $
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
