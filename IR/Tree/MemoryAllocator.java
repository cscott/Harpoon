package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Qop;
import harpoon.IR.LowQuad.LQop;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

abstract class MemoryAllocator 
{
  /**
   *  Returns a <code>Stm</code> object which contains code to call the
   *  garbage collector.   
   */
  abstract public Stm    call_GC();

  /**
   *  Returns a <code>Stm</code> object which increments the next free
   *  location in memory by N.   
   */
  abstract public Stm    inc_next_ptr(Exp N);

  /**
   *  Returns a temporary variable which points to the highest location 
   *  in memory in which data can be allocated.    
   */
  abstract public TEMP  mem_limit(); // POINTER type

  /**
   *  Returns a temporary variable which points to the next free location 
   *  in memory.     
   */
  abstract public TEMP  next_ptr(); // POINTER type

  /**
   *  Returns a <code>Stm</code> object which contains code executed 
   *  when the program runs out of memory.   
   */
  abstract public Stm    out_of_memory();


  /** 
   *  Returns a <code>Stm</code> object which allocates a block of memory 
   *  of the specified size.   
   */
  public Stm allocateMemory(TreeFactory tf, HCodeElement src,
			    TEMP ptr, Exp size)
    {
      LABEL  l0, l1, l2, l3, l4;
      Stm    s0, s1, s2, s3, s4, s5;
      TEMP   triedGC; // INT type

      triedGC = new TEMP(tf, src,
			 Typed.INT, new Temp(ptr.temp.tempFactory()));
      l0 = new LABEL(tf, src, new Label());
      l1 = new LABEL(tf, src, new Label());
      l2 = new LABEL(tf, src, new Label());
      l3 = new LABEL(tf, src, new Label());
      l4 = new LABEL(tf, src, new Label());
      
      //
      // triedGC <-- 0
      //
      s0 = new MOVE(tf, src, triedGC, new CONST(tf, src, (int)0));

      // Is (limit > next + N) ?
      //
      s1 = new SEQ(tf, src, l0,
		   new CJUMP(tf, src, new BINOP(tf, src,
				       Typed.POINTER,
				       Bop.CMPGT,
				       mem_limit(),
				       new BINOP(tf, src,
						 Typed.POINTER,
						 Bop.ADD,
						 size,
						 next_ptr())),
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
		   out_of_memory());

      // triedGC <-- 1
      // call the garbage collector
      // try to allocate memory again
      //
      s4 = new SEQ(tf, src, l4,
		   new SEQ(tf, src, new MOVE(tf, src,
					     triedGC,
					     new CONST(tf, src, (int)1)),
			   new SEQ(tf, src, call_GC(),
				   new JUMP(tf, src, l0.label))));

      // There is enough memory to allocate.  
      // Increment the "next" ptr, and MOVE the result to a useful
      // place
      //
      s5 = new SEQ(tf, src, l1,
		   new SEQ(tf, src, new MOVE(tf, src, ptr, next_ptr()),
			   inc_next_ptr(size)));

      // Combine the Stm objects into one SEQ object, and return it.
      //
      return new SEQ(tf, src, s1,
		     new SEQ(tf, src, s2,
			     new SEQ(tf, src, s3,
				     new SEQ(tf, src, s4, s5))));
    }

}
