// NodeNotPresentInGraphException.java, created Wed Jan 13 14:36:37 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeNotPresentInGraphException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotPresentInGraphException.java,v 1.1.2.3 1999-01-19 23:51:13 pnkfelix Exp $
 */

public class NodeNotPresentInGraphException extends RuntimeException {
    
    /** Creates a <code>NodeNotPresentInGraphException</code>. */
    public NodeNotPresentInGraphException() {
        super();
    }

    /** Creates a <code>NodeNotPresentInGraphException</code>. */
    public NodeNotPresentInGraphException(String s) {
        super(s);
    }
    
}
