// LET.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>LET</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment can be either a constant or another
 * temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LET.java,v 1.1 1998-08-05 11:11:48 cananian Exp $
 */

public class LET extends Quad {
    
    /** Creates a <code>LET</code>. */
    public LET() {
    }
    
}
