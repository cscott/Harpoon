// AllocationStrategy.java, created Mon Feb 15  3:38:00 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.IR.Tree.Exp;

/* <b>FILL ME IN</b>
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AllocationStrategy.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
interface AllocationStrategy {
    public Exp memAlloc(Exp size);
}
