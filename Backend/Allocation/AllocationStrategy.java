package harpoon.Backend.Allocation;

import harpoon.IR.Tree.Exp;

/* <b>FILL ME IN</b>
 * @author Duncan Bryce
 * @version $Id: AllocationStrategy.java,v 1.1.2.3 1999-08-04 04:34:02 cananian Exp $
 */
public interface AllocationStrategy {
    public Exp memAlloc(Exp size);
}
