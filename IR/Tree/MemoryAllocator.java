package harpoon.IR.Tree;

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
  public Stm allocateMemory(TEMP ptr, Exp size)
    {
      LABEL  l0, l1, l2, l3, l4;
      Stm    s0, s1, s2, s3, s4, s5;
      TEMP   triedGC; // INT type

      triedGC = new TEMP(Typed.INT, new Temp(ptr.temp.tempFactory()));
      l0 = new LABEL(new Label());
      l1 = new LABEL(new Label());
      l2 = new LABEL(new Label());
      l3 = new LABEL(new Label());
      l4 = new LABEL(new Label());
      
      //
      // triedGC <-- 0
      //
      s0 = new MOVE(triedGC, new CONST((int)0));

      // Is (limit > next + N) ?
      //
      s1 = new SEQ(l0,
		   new CJUMP(new BINOP(Typed.POINTER,
				       Bop.CMPGT,
				       mem_limit(),
				       new BINOP(Typed.POINTER,
						 Bop.ADD,
						 size,
						 next_ptr())),
			     l1.label,   // There's enough space
			     l2.label)); // Not enough space!
      
	
      // (limit > next + N) == FALSE.
      // If (triedGC != 0), then perform garbage collection and try again.
      // Otherwise, we're out of memory.
      //
      s2 = new SEQ(l2,
		   new CJUMP(triedGC,
			     l3.label,    // If tried GC already, out of mem
			     l4.label));  // If mem alloc fails, call GC
      
      // Throw OutOfMemoryError
      //
      s3 = new SEQ(l3,
		   out_of_memory());

      // triedGC <-- 1
      // call the garbage collector
      // try to allocate memory again
      //
      s4 = new SEQ(l4,
		   new SEQ(new MOVE(triedGC, new CONST((int)1)),
			   new SEQ(call_GC(),
				   new JUMP(l0.label))));

      // There is enough memory to allocate.  
      // Increment the "next" ptr, and MOVE the result to a useful
      // place
      //
      s5 = new SEQ(l1,
		   new SEQ(new MOVE(ptr, next_ptr()),
			   inc_next_ptr(size)));

      // Combine the Stm objects into one SEQ object, and return it.
      //
      return new SEQ(s1,
		     new SEQ(s2,
			     new SEQ(s3,
				     new SEQ(s4, s5))));
    }

}
