// IllegalEdgeException.java, created Thu Jan 14 15:34:50 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>IllegalEdgeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: IllegalEdgeException.java,v 1.1.2.1 1999-01-14 23:16:29 pnkfelix Exp $
 */

public class IllegalEdgeException extends Exception {
    
    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException() {
        super();
    }

    /** Creates a <code>IllegalEdgeException</code>. */
    public IllegalEdgeException(String s) {
        super(s);
    }
    
}
