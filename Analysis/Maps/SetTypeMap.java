// SetTypeMap.java, created Wed Nov  4 17:03:04 1998 by marinov
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.Analysis.TypeInference.SetHClass;
import harpoon.ClassFile.*;
/**
 * <code>SetTypeMap</code> is a mapping from temporaries to their concrete types,
 * i.e. the sets of all exact classes whose instances the temporary may hold
 * during execution.
 * (For details see Ole Agesen's PhD Thesis, pp. 4-8.)
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SetTypeMap.java,v 1.1.2.1 1998-12-03 08:11:29 marinov Exp $
 */

public interface SetTypeMap {
    
    /** Return the concrete type of a given temporary.
     * @param c The <code>HCode</code> containing <code>t</code>.
     * @param t The temporary to examine.
     * @return the concrete type of <code>t</code>. 
     */
    public SetHClass setTypeMap(HCode c, Temp t);
    
}
