// AllocationInfo.java, created Wed Jul 28 16:08:24 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Temp.Label;
import harpoon.Temp.Temp;

/** 
 * The <code>AllocationInfo</code> interface specifies information assumed 
 * to be needed for most memory allocation schemes.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: AllocationInfo.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
interface AllocationInfo {

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
  
