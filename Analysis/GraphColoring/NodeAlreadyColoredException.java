// NodeAlreadyColoredException.java, created Thu Jan 14 17:15:00 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeAlreadyColoredException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeAlreadyColoredException.java,v 1.1.2.3 1999-01-19 23:51:12 pnkfelix Exp $
 */

public class NodeAlreadyColoredException extends RuntimeException {
    
    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException() {
        super();
    }

    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException(String s) {
        super(s);
    }
    
}
