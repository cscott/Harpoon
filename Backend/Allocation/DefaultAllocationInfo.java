package harpoon.Backend.Allocation;

import harpoon.Temp.Temp;

/** 
 * The DefaultAllocationInfo interface specifies information assumed to be
 * needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DefaultAllocationInfo.java,v 1.1.2.1 1999-02-11 19:30:09 duncan Exp $
 */
public interface DefaultAllocationInfo {

  /** Returns a pointer to the garbage collection function */
  public Temp GC();

  /** Returns the highest legal address in memory */
  public Temp mem_limit();  

  /** Returns a pointer to the value of the next free address in memory */
  public Temp next_ptr();

}
  
