// CachingArrayInfo.java, created Sun Apr  2 18:36:36 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.TypeInference;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;

/**
 * <code>CachingArrayInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingArrayInfo.java,v 1.1.2.4 2001-06-17 22:37:16 cananian Exp $
 */
public class CachingArrayInfo extends ArrayInfo
    implements java.io.Serializable {
    
    Map cache = new HashMap();

    /** Creates a <code>CachingArrayInfo</code>. */
    public CachingArrayInfo() {
    }

    /** Returns the set of <code>AGET</code> instructions from hcode
	that access arrays of non primitive objects. */
    public Set getInterestingAGETs(HMethod hm, HCode hcode){
	Set retval = (Set) cache.get(hcode);
	if(retval == null){
	    retval = super.getInterestingAGETs(hm, hcode);
	    cache.put(hcode, retval);
	}

	return retval;
    }

    /** Clears the cache. */
    public void clear(){
	cache.clear();
    }
}
