// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
package harpoon.Temp;

/**
 * <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.1 1998-08-19 05:09:07 cananian Exp $
 */

public interface TypeMap  { 
    /** Return the type of a given temporary. */
    public harpoon.ClassFile.HClass typeMap(harpoon.Temp.Temp t);
}
