// NoFactorySetException.java, created Thu Jan 14 20:55:46 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NoFactorySetException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: NoFactorySetException.java,v 1.1.2.4 2001-06-17 22:29:39 cananian Exp $
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
