// ObjectNotModifiableException.java, created Thu Jan 14 16:15:42 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>ObjectNotModifiableException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ObjectNotModifiableException.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
 */

public class ObjectNotModifiableException extends RuntimeException {
    
    /** Creates a <code>ObjectNotModifiableException</code>. */
    public ObjectNotModifiableException() {
        super();
    }
    
    /** Creates a <code>ObjectNotModifiableException</code>. */
    public ObjectNotModifiableException(String s) {
        super(s);
    }
}
