// Node.java, created Wed Jan 13 15:54:49 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>Node</code> is an abstract representation of a node for use
 * with the Graph object.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Node.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $ */

public class Node  {

    /** Creates a <code>Node</code>. */
    public Node() {
        
    }
    
    /** Modifiability check.
	effects: if <code>this</code> is allowed to be modified,
	returns true.  Else returns false. 
    */
    public boolean isModifiable() {
	return true;
    }

}
