// AllocationStrategy.java, created Mon Mar 15  3:38:00 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Allocation;

import harpoon.IR.Tree.Exp;

/* <b>FILL ME IN</b>
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AllocationStrategy.java,v 1.1.2.4 1999-08-04 05:52:24 cananian Exp $
 */
public interface AllocationStrategy {
    public Exp memAlloc(Exp size);
}
