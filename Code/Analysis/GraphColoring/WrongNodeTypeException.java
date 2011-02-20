// WrongNodeTypeException.java, created Wed Jan 13 16:36:07 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>WrongNodeTypeException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: WrongNodeTypeException.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
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
