// NodeNotRemovedException.java, created Wed Jan 13 14:54:54 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotRemovedException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotRemovedException.java,v 1.1.2.2 1999-01-19 16:08:00 pnkfelix Exp $
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
