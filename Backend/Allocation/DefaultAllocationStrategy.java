package harpoon.Backend.Allocation;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

/**
 * A simple-minded version of Appel's fast-allocation strategy
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: DefaultAllocationStrategy.java,v 1.1.2.4 1999-07-28 20:09:27 duncan Exp $
 */
public class DefaultAllocationStrategy implements AllocationStrategy {

    private AllocationInfo info;

    public DefaultAllocationStrategy(AllocationInfo info) {
	this.info = info;
    }

    /** 
   *  Returns a <code>Stm</code> object which allocates a block of memory 
   *  of the specified size.   
   */
    public Exp memAlloc(Exp size)
    {
	LABEL       l0, l1, l2, l3, l4;
	Stm         s0, s1, s2, s3, s4, s5;
	TEMP        triedGC; // INT type
	TEMP        newMemPtr, nextPtr, resultPtr;
	HCodeElement src = size;
	TreeFactory  tf  = size.getFactory();

	triedGC   = new TEMP(tf,src,Type.INT,     new Temp(tf.tempFactory()));
	nextPtr   = new TEMP(tf,src,Type.POINTER, info.getNextPtr());
	newMemPtr = new TEMP(tf,src,Type.POINTER, new Temp(tf.tempFactory()));
	resultPtr = new TEMP(tf,src,Type.POINTER, new Temp(tf.tempFactory()));

	l0 = new LABEL(tf, src, new Label());
	l1 = new LABEL(tf, src, new Label());
	l2 = new LABEL(tf, src, new Label());
	l3 = new LABEL(tf, src, new Label());
	l4 = new LABEL(tf, src, new Label());
      
	// triedGC <-- 0; 
	// newMemPtr <-- info.next_ptr() + size
	//
	s0 = new SEQ(tf, src, 
		     new MOVE(tf, src, triedGC, new CONST(tf, src, (int)0)),
		     new MOVE(tf, src, newMemPtr, 
			      new BINOP(tf, src, Type.POINTER, Bop.ADD,
					nextPtr, 
					size)));

	// Is (limit > next + N) ?
	//
	s1 = new SEQ
	    (tf, src, l0,
	     new CJUMP(tf, src, 
		       new BINOP(tf, src, Type.POINTER, Bop.CMPGT,
				 info.getMemLimit(tf, src),
				 newMemPtr),
		       l1.label,   // There's enough space
		       l2.label)); // Not enough space!
      
	
	// (limit > next + N) == FALSE.
	// If (triedGC != 0), then perform garbage collection and try again.
	// Otherwise, we're out of memory.
	//
	s2 = new SEQ(tf, src, l2,
		     new CJUMP(tf, src, triedGC,
			       l3.label,    // If tried GC already, out of mem
			       l4.label));  // If mem alloc fails, call GC
      
	// Throw OutOfMemoryError
	//
	s3 = new SEQ(tf, src, l3,
		     info.exitOutOfMemory(tf, src));

	// triedGC <-- 1
	// call the garbage collector
	// try to allocate memory again
	//
	s4 = new SEQ(tf, src, l4,
		     new SEQ(tf, src, new MOVE(tf, src,
					       triedGC,
					       new CONST(tf, src, (int)1)),
			     new SEQ(tf, src, info.callGC(tf, src),
				     new JUMP(tf, src, l0.label))));

	// There is enough memory to allocate.  
	// Increment the "next" ptr, and MOVE the result to a useful
	// place
	//
	s5 = new SEQ(tf, src, 
		     l1,
		     new SEQ(tf, src, 
			     new MOVE(tf, src, resultPtr, nextPtr), 
			     new MOVE(tf, src, nextPtr, newMemPtr)));
		     
	// Combine the Stm objects into one ESEQ object, and return it.
	//
	return new ESEQ
	    (tf, src, 
	     new SEQ(tf, src, s0, 
		     new SEQ(tf, src, s1,
			     new SEQ(tf, src, s2,
				     new SEQ(tf, src, s3,
					     new SEQ(tf, src, s4, s5))))),
	     resultPtr);
    }
    
}
