// LightRelation.java, created Fri Jun 30 11:11:17 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import java.io.Serializable;

import harpoon.Util.Collections.LinearSet;

/**
 * <code>LightRelation</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: LightRelation.java,v 1.1.2.2 2000-07-02 08:37:52 salcianu Exp $
 */
public class LightRelation extends AbstrRelationMapBased
    implements Serializable {

    /** Creates a <code>RelationLight</code>. */
    public LightRelation() {
        map = new LightMap();
    }


    protected Relation getEmptyRelation() {
	return new LightRelation();
    }
    

    public boolean add(Object key, Object value) {
	hashCode = 0;
	Collection vals = getValues2(key);
	if(vals == null)
	    map.put(key, vals = new LinearSet());
	return vals.add(value);
    }


    public boolean addAll(Object key, Collection values) {
	hashCode = 0;
	if((values == null) || values.isEmpty())
	    return false;
	Collection vals = getValues2(key);
	if(vals == null)
	    map.put(key, vals = new LinearSet());
	return vals.addAll(values);
    }


    public void removeAll(Object key, Collection values) {
	hashCode = 0;
	Collection vals = getValues2(key);
	if((vals == null) || vals.isEmpty()) return;

	for(Iterator it = values.iterator(); it.hasNext(); )
	    vals.remove(it.next());
    }


    public Object clone() {
	LightRelation newrel = (LightRelation) super.clone();
	Map newmap = (Map) ((LightMap) map).clone();
	for(Iterator it = newmap.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    LinearSet newvals = 
		(LinearSet) ((LinearSet) entry.getValue()).clone();
	    entry.setValue(newvals);
	}
	return newrel;
    }

}
