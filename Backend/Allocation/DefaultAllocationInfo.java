package harpoon.Backend.Allocation;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Temp;

/** 
 * The DefaultAllocationInfo interface specifies information assumed to be
 * needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DefaultAllocationInfo.java,v 1.1.2.3 1999-02-15 08:38:00 duncan Exp $
 */
public interface DefaultAllocationInfo {

  /** Returns code to call the garbage collection function */
  public Stm GC(TreeFactory tf, HCodeElement src);

  /** Returns the highest legal address in memory */
  public Exp mem_limit(TreeFactory tf, HCodeElement src);  

  /** Returns a pointer to the value of the next free address in memory */
  public MEM next_ptr(TreeFactory tf, HCodeElement src);

  /** Contains code to exit with an OutOfMemoryError */
  public Stm out_of_memory(TreeFactory tf, HCodeElement src);
}
  
