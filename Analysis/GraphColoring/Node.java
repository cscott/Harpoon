// Node.java, created Wed Jan 13 15:54:49 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>Node</code> is an abstract representation of a node for use
 * with the Graph object.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Node.java,v 1.1.2.3 1999-01-22 18:19:04 pnkfelix Exp $ */

public abstract class Node  {

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

    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public abstract String toString();
}
