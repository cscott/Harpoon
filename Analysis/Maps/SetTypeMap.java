// SetTypeMap.java, created Wed Nov  4 17:03:04 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.Temp.Temp;
import harpoon.Analysis.TypeInference.SetHClass;
import harpoon.ClassFile.HCode;
/**
 * <code>SetTypeMap</code> is a mapping from temporaries to their concrete types,
 * i.e.<!--this comment indicates to javadoc that the preceding period was 
 * not the end of the sentence--> the sets of all exact classes whose
 * instances the temporary may hold during execution.
 * (For details see Ole Agesen's PhD Thesis, pp. 4-8.)
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SetTypeMap.java,v 1.2 2002-02-25 20:58:10 cananian Exp $
 */

public interface SetTypeMap {
    
    /** Return the concrete type of a given temporary.
     * @param c The <code>HCode</code> containing <code>t</code>.
     * @param t The temporary to examine.
     * @return the concrete type of <code>t</code>. 
     */
    public SetHClass setTypeMap(HCode c, Temp t);
    
}
