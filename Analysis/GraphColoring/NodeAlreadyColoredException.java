// NodeAlreadyColoredException.java, created Thu Jan 14 17:15:00 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>NodeAlreadyColoredException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeAlreadyColoredException.java,v 1.1.2.1 1999-01-14 23:17:49 pnkfelix Exp $
 */

public class NodeAlreadyColoredException extends Exception {
    
    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException() {
        super();
    }

    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException(String s) {
        super(s);
    }
    
}
