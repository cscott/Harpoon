// NullAllocationStrategy.java, created Wed Oct 13 12:55:23 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
/**
 * <code>NullAllocationStrategy</code> just returns a null pointer
 * when asked for memory.  It's just a stub, really.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NullAllocationStrategy.java,v 1.1.2.1 1999-10-13 17:16:50 cananian Exp $
 */
public class NullAllocationStrategy extends AllocationStrategy {
    /** Creates a <code>NullAllocationStrategy</code>. */
    public NullAllocationStrategy() { }

    public Exp memAlloc(TreeFactory tf, HCodeElement source, Exp length) {
	return new CONST(tf, source); // null pointer.
    }
}
