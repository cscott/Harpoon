// UncolorableGraphException.java, created Wed Jan 13 14:22:42 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>UncolorableGraphException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: UncolorableGraphException.java,v 1.1.2.2 1999-08-04 05:52:21 cananian Exp $
 */

public class UncolorableGraphException extends Exception {
    
    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException() {
        super();
    }

    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException(String s) {
        super(s);
    }
    
}
