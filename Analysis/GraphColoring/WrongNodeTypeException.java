// WrongNodeTypeException.java, created Wed Jan 13 16:36:07 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>WrongNodeTypeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: WrongNodeTypeException.java,v 1.1.2.4 1999-08-04 05:52:21 cananian Exp $
 */

public class WrongNodeTypeException extends RuntimeException {
    
    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException() {
        super();
    }

    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException(String s) {
        super(s);
    }
    
}
