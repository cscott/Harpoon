// NodeNotPresentInGraphException.java, created Wed Jan 13 14:36:37 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>NodeNotPresentInGraphException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NodeNotPresentInGraphException.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $
 */

public class NodeNotPresentInGraphException extends Exception {
    
    /** Creates a <code>NodeNotPresentInGraphException</code>. */
    public NodeNotPresentInGraphException() {
        super();
    }

    /** Creates a <code>NodeNotPresentInGraphException</code>. */
    public NodeNotPresentInGraphException(String s) {
        super(s);
    }
    
}
