// Renameable.java, created Wed Sep 16 02:16:17 1998 by cananian
package harpoon.IR.Properties;

import harpoon.Temp.TempMap;
/**
 * <code>Renameable</code> defines an interface for renaming temporaries
 * in an intermediate representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Renameable.java,v 1.1 1998-09-16 06:31:30 cananian Exp $
 */

public interface Renameable  {
    /** Rename all used variables in this <code>HCodeElement</code> 
     *  according to mapping <code>tm</code>. */
    public void rename(TempMap tm);
}
