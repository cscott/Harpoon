// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
package harpoon.Temp;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;

/**
 * <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.2 1998-09-11 13:12:51 cananian Exp $
 */

public interface TypeMap  { 
    /** Return the type of a given temporary. */
    public HClass typeMap(HMethod m, Temp t);
}
