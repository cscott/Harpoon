// NodeNotColoredException.java, created Wed Jan 13 17:30:19 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotColoredException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotColoredException.java,v 1.1.2.2 1999-01-19 16:08:00 pnkfelix Exp $
 */

public class NodeNotColoredException extends Exception {
    
    /** Creates a <code>NodeNotColoredException</code>. */
    public NodeNotColoredException() {
        super();
    }

    /** Creates a <code>NodeNotColoredException</code>. */
    public NodeNotColoredException(String s) {
        super(s);
    }
    
}
