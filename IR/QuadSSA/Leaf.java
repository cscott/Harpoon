// Leaf.java, created Thu Aug  6 04:26:49 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * A <code>Leaf</code> is either a (wrapped) {@link LeafTemp Temp}
 * or a {@link LeafConst constant value}.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Leaf.java,v 1.1 1998-08-07 09:56:38 cananian Exp $
 * @see LeafTemp
 * @see LeafConst
 */

public abstract class Leaf  {
    /** Require subclasses to re-implement <code>toString</code> method. */
    public abstract String toString();
}
