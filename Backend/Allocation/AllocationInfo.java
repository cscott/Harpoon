package harpoon.Backend.Allocation;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Temp;

/** 
 * The DefaultAllocationInfo interface specifies information assumed to be
 * needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AllocationInfo.java,v 1.1.2.1 1999-07-28 20:08:24 duncan Exp $
 */
public interface AllocationInfo {

  /** Returns code to call the garbage collection function */
  public Stm callGC(TreeFactory tf, HCodeElement src);

  /** Returns the highest legal address in memory */
  public Exp getMemLimit(TreeFactory tf, HCodeElement src);  

  /** Returns a pointer to the value of the next free address in memory */
  public Temp getNextPtr();

  /** Contains code to exit with an OutOfMemoryError */
  public Stm exitOutOfMemory(TreeFactory tf, HCodeElement src);
}
  
