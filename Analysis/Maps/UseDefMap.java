// UseDefMap.java, created Sun Sep 13 23:11:39 1998 by cananian
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * A <code>UseDefMap</code> is a mapping from temporaries to the
 * <code>HCodeElements</code> that define them.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UseDefMap.java,v 1.1 1998-09-14 05:21:46 cananian Exp $
 */

public interface UseDefMap  {
    /**
     * Return the <code>HCodeElement</code>s that use <code>Temp t</code>.
     * @param hc The <code>HCode</code> containing <code>t</code>.
     *           The <code>HCodeElement</code>s in <code>hc</code> must
     *           implement <code>harpoon.IR.Properties.UseDef</code>.
     * @param t  The temporary to examine.
     * @return an array of <code>HCodeElement</code>s where
     *         <code>HCodeElement.use()</code> includes <code>t</code>.
     */
    HCodeElement[] useMap(HCode hc, Temp t);
    /**
     * Return the <code>HCodeElement</code>s that define <code>Temp t</code>.
     * @param hc The <code>HCode</code> containing <code>t</code>.
     *           The <code>HCodeElement</code>s in <code>hc</code> must
     *           implement <code>harpoon.IR.Properties.UseDef</code>.
     * @param t  The temporary to examine.
     * @return an array of <code>HCodeElement</code>s where
     *         <code>HCodeElement.def()</code> includes <code>t</code>.
     */
    HCodeElement[] defMap(HCode hc, Temp t);
}
