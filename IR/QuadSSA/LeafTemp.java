// LeafTemp.java, created Thu Aug  6 04:30:25 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>LeafTemp</code> objects wrap {@link harpoon.Temp.Temp Temp} objects
 * as quadruple leaves.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LeafTemp.java,v 1.1 1998-08-07 09:56:38 cananian Exp $
 */

public class LeafTemp extends Leaf {
    /** The <code>Temp</code> that this <code>LeafTemp</code> wraps. */
    public Temp temp;
    /** Creates a <code>LeafTemp</code> from a <code>Temp</code>. */
    public LeafTemp(Temp t) { this.temp = t; }
    /** Show underlying Temp. */
    public String toString() { return temp.toString(); }
}
