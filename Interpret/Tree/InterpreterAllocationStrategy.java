// InterpreterAllocationStrategy.java, created Tue Apr 27 18:05:08 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

/* <b>FILL ME IN</b>.
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterpreterAllocationStrategy.java,v 1.1.2.3 1999-08-04 05:52:35 cananian Exp $
 */
public class InterpreterAllocationStrategy implements AllocationStrategy {

    public InterpreterAllocationStrategy() { }

    public Exp memAlloc(Exp size) { 
	TreeFactory tf = size.getFactory();

	TEMP rv = new TEMP(tf, size, Type.POINTER, new Temp(tf.tempFactory()));
	TEMP rx = new TEMP(tf, size, Type.POINTER, new Temp(tf.tempFactory()));

	return 
	    new ESEQ
	    (tf, size, 
	     new CALL
	     (tf, size, rv, rx, 
	      new NAME(tf, size, new Label("RUNTIME_MALLOC")),
	      new ExpList(size, null)),
	     rv);
    }
}
