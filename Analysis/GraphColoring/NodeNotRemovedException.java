// NodeNotRemovedException.java, created Wed Jan 13 14:54:54 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>NodeNotRemovedException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotRemovedException.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $
 */

public class NodeNotRemovedException extends Exception {
    
    /** Creates a <code>NodeNotRemovedException</code>. */
    public NodeNotRemovedException() {
        super();
    }

    /** Creates a <code>NodeNotRemovedException</code>. */
    public NodeNotRemovedException(String s) {
        super(s);
    }
    
}
