// AbstrRelationMapBased.java, created Fri Jun 30 11:17:10 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

import java.util.Set;
import java.util.Map;

import java.io.Serializable;


/**
 * <code>AbstrRelationMapBased</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: AbstrRelationMapBased.java,v 1.1.2.1 2000-07-01 23:09:43 salcianu Exp $
 */
public abstract class AbstrRelationMapBased extends AbstrRelation
    implements Serializable {
    
    // A map from keys to sets of values.
    protected Map map = null;
    

    public void removeKey(Object key) {
	hashCode = 0;
	map.remove(key);
    }

    
    public Set getValues(Object key) {
	return (Set) map.get(key);
    }


    public Set keys() {
	return map.keySet();
    }
    
}
