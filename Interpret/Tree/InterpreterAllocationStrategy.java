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
 * @author Duncan Bryce
 * @version $Id: InterpreterAllocationStrategy.java,v 1.1.2.2 1999-08-04 04:34:11 cananian Exp $
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
