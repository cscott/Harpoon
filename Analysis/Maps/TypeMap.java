// TypeMap.java, created Wed Aug 19 01:02:27 1998 by cananian
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;

/**
 * <code>TypeMap</code> is a mapping from temporaries to their types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypeMap.java,v 1.1 1998-09-13 23:57:13 cananian Exp $
 */
public interface TypeMap  { 
    /** 
     * Return the type of a given temporary. 
     * @param hc The <code>HCode</code> containing <code>t</code>.
     * @param t The temporary to examine.
     * @return the static type of <code>t</code>.
     */
    public HClass typeMap(HCode hc, Temp t);
}
