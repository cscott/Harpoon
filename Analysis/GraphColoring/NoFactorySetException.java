// NoFactorySetException.java, created Thu Jan 14 20:55:46 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NoFactorySetException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NoFactorySetException.java,v 1.1.2.3 1999-08-04 05:52:21 cananian Exp $
 */

public class NoFactorySetException extends RuntimeException {
    
    /** Creates a <code>NoFactorySetException</code>. */
    public NoFactorySetException() {
        super();
    }
    
    /** Creates a <code>NoFactorySetException</code>. */
    public NoFactorySetException(String s) {
        super(s);
    }    
}
