// EdgeNotPresentException.java, created Wed Jan 13 18:13:13 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>EdgeNotPresentException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: EdgeNotPresentException.java,v 1.1.2.5 2001-06-17 22:29:38 cananian Exp $
 */

public class EdgeNotPresentException extends RuntimeException {
    
    /** Creates a <code>EdgeNotPresentException</code>. */
    public EdgeNotPresentException() {
        super();
    }

    /** Creates a <code>EdgeNotPresentException</code>. */
    public EdgeNotPresentException(String s) {
        super(s);
    }
    
}
