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
 * @version $Id: DefaultAllocationInfo.java,v 1.1.2.4 1999-02-26 22:46:46 andyb Exp $
 */
public interface DefaultAllocationInfo {

  /** Returns code to call the garbage collection function */
  public Stm callGC(TreeFactory tf, HCodeElement src);

  /** Returns the highest legal address in memory */
  public Exp getMemLimit(TreeFactory tf, HCodeElement src);  

  /** Returns a pointer to the value of the next free address in memory */
  public MEM getNextPtr(TreeFactory tf, HCodeElement src);

  /** Contains code to exit with an OutOfMemoryError */
  public Stm exitOutOfMemory(TreeFactory tf, HCodeElement src);
}
  
