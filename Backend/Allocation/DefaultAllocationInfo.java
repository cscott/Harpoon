package harpoon.Backend.Allocation;

import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Stm;
import harpoon.Temp.Temp;

/** 
 * The DefaultAllocationInfo interface specifies information assumed to be
 * needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DefaultAllocationInfo.java,v 1.1.2.2 1999-02-12 08:01:41 duncan Exp $
 */
public interface DefaultAllocationInfo {

  /** Returns code to call the garbage collection function */
  public Stm GC();

  /** Returns the highest legal address in memory */
  public Exp mem_limit();  

  /** Returns a pointer to the value of the next free address in memory */
  public MEM next_ptr();

  /** Contains code to exit with an OutOfMemoryError */
  public Stm out_of_memory();
}
  
