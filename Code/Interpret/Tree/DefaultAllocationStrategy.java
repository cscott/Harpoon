// DefaultAllocationStrategy.java, created Fri Feb 12  3:01:41 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

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
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.util.Arrays;
import java.util.List;

/**
 * A simple-minded version of Appel's fast-allocation strategy
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: DefaultAllocationStrategy.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
class DefaultAllocationStrategy implements AllocationStrategy {

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
	LABEL        l0, l1, l2, l3, l4;
	NAME         gc, exit_oom;
	TEMP         triedGC; // INT type
	TEMP         memLimit, newMemPtr, nextPtr, resultPtr, tmp;
	HCodeElement src = size;
	TreeFactory  tf  = size.getFactory();
	Stm[]        stms;

	triedGC   = new TEMP(tf,src,Type.INT,     new Temp(tf.tempFactory()));
	nextPtr   = new TEMP(tf,src,Type.POINTER, info.getNextPtr());
	memLimit  = new TEMP(tf,src,Type.POINTER, info.getMemLimit());
	newMemPtr = new TEMP(tf,src,Type.POINTER, new Temp(tf.tempFactory()));
	resultPtr = new TEMP(tf,src,Type.POINTER, new Temp(tf.tempFactory()));
	tmp       = new TEMP(tf,src,Type.POINTER, new Temp(tf.tempFactory()));

	l0        = new LABEL(tf, src, new Label(), false);
	l1        = new LABEL(tf, src, new Label(), false);
	l2        = new LABEL(tf, src, new Label(), false);
	l3        = new LABEL(tf, src, new Label(), false);
	l4        = new LABEL(tf, src, new Label(), false);
	gc        = new NAME(tf, src, info.GC());
	exit_oom  = new NAME(tf, src, info.exitOutOfMemory());
      
	stms = new Stm[] { 
	    // triedGC <-- 0; 
	    new MOVE(tf, src, triedGC, new CONST(tf, src, (int)0)),

	    // newMemPtr <-- info.next_ptr() + size
	    new MOVE
	    (tf, src, newMemPtr, 
	     new BINOP
	     (tf, src, Type.POINTER, Bop.ADD,
	      nextPtr, 
	      size)),
	
	    // LABEL 0 
	    l0,

	    // Is (limit > next + N) ?
	    new CJUMP
	    (tf, src, 
	     new BINOP
	     (tf, src, Type.POINTER, Bop.CMPGT,
	      memLimit, 
	      newMemPtr),
	     l1.label,  // There's enough space
	     l2.label), // Not enough space!
      
	
	    // LABEL 2
	    l2,

	    // (limit > next + N) == FALSE.
	    // If (triedGC!=0), then perform garbage collection and try again.
	    // Otherwise, we're out of memory.
	    new CJUMP
	    (tf, src, triedGC, 
	     l3.label,   // If tried GC already, out of mem
	     l4.label),  // If mem alloc fails, call GC
	    
	    // LABEL 3
	    l3,
	
	    // Throw OutOfMemoryError
	    new NATIVECALL(tf, src, tmp, exit_oom, null),
	
	    // LABEL 4
	    l4,
	
	    // triedGC <-- 1
	    new MOVE(tf, src, triedGC, new CONST(tf, src, 1)),
	    
	    // call the garbage collector
	    new NATIVECALL(tf, src, tmp, gc, null),
	
	    // try to allocate memory again
	    new JUMP(tf, src, l0.label),

	    // LABEL 1
	    l1,

	    // There is enough memory to allocate.  
	    // Increment the "next" ptr, and MOVE the result to a useful
	    // place
	    //
	    new MOVE(tf, src, resultPtr, nextPtr),
	    new MOVE(tf, src, nextPtr, newMemPtr)
	};
	
	// Combine the Stm objects into one ESEQ object, and return it.
	//
	return new ESEQ(tf, src, Stm.toStm(Arrays.asList(stms)), resultPtr);
    }
    
}
