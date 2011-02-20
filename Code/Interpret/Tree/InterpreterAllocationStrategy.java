// InterpreterAllocationStrategy.java, created Sat Mar 27 17:05:08 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

/**
 * An allocation strategy designed specifically for use by the Tree 
 * interpreter.  Probably shouldn't be used for anything else.  
 *
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterpreterAllocationStrategy.java,v 1.2 2002-02-25 21:05:57 cananian Exp $
 */
class InterpreterAllocationStrategy implements AllocationStrategy {

    public InterpreterAllocationStrategy() { }

    public Exp memAlloc(Exp size) { 
	TreeFactory tf = size.getFactory();

	TEMP rv = new TEMP(tf, size, Type.POINTER, new Temp(tf.tempFactory()));

	return 
	    new ESEQ
	    (tf, size, 
	     new NATIVECALL
	     (tf, size, rv,  
	      new NAME(tf, size, new Label("RUNTIME_MALLOC")),
	      new ExpList(size, null)),
	     rv);
    }
}
