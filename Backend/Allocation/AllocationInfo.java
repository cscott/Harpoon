package harpoon.Backend.Allocation;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

/** 
 * The <code>AllocationInfo</code> interface specifies information assumed 
 * to be needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AllocationInfo.java,v 1.1.2.2 1999-07-28 21:34:42 duncan Exp $
 */
public interface AllocationInfo {

    /** Returns the label of the garbage collection function */
    public Label GC();
    
    /** Returns a pointer to the highest available address in memory.  */
    public Temp  getMemLimit();
    
    /** Returns a pointer to the next free address in memory */
    public Temp  getNextPtr();
    
    /** Returns the label of a function which exits with an out of memory 
     *  error */
    public Label exitOutOfMemory();
}
  
