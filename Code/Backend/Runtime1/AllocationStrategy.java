// AllocationStrategy.java, created Mon Feb 15  3:38:00 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.TreeFactory;

/** An <code>AllocationStrategy</code> is a particular implementation
 * of a memory allocation routine.
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AllocationStrategy.java,v 1.3 2003-04-19 01:03:55 salcianu Exp $
 */
public abstract class AllocationStrategy implements java.io.Serializable {
    /** Return a <code>Tree.Exp</code> created with the given
     *  <code>TreeFactory</code> that returns a pointer to a piece of
     *  memory <code>length</code> bytes long. */
    public abstract Exp memAlloc(TreeFactory tf, HCodeElement source,
				 DerivationGenerator dg,
				 AllocationProperties ap,
				 Exp length);
}
