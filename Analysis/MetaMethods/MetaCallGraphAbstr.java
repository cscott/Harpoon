// MetaCallGraphAbstr.java, created Mon Mar 13 16:03:18 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import harpoon.IR.Quads.CALL;

/**
 * <code>MetaCallGraphAbstr</code> Abstract implementation of the
 <code>MetaCallGraph</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: MetaCallGraphAbstr.java,v 1.1.2.1 2000-03-18 01:55:14 salcianu Exp $
 */
public abstract class MetaCallGraphAbstr implements MetaCallGraph {

    // Map<MetaMethod,MetaMethod[]>
    protected final Map callees1_cmpct = new HashMap();
    // Map<MetaMethod,Map<CALL,MetaMethod[]>>
    protected final Map callees2_cmpct = new HashMap();

    private final MetaMethod[] empty_array = new MetaMethod[0];

    /** Returns the meta methods that can be called by <code>mm</code>. */
    public MetaMethod[] getCallees(MetaMethod mm){
	MetaMethod[] retval = (MetaMethod[]) callees1_cmpct.get(mm);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }

    /** Returns the meta methods that can be called by <code>mm</code>
	at the call site <code>q</code>. */
    public MetaMethod[] getCallees(MetaMethod mm, CALL cs){
	Map map = (Map) callees2_cmpct.get(mm);
	if(map == null)
	    return new MetaMethod[0];
	MetaMethod[] retval = (MetaMethod[]) map.get(cs);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }

    /** Returns the set of all the call sites in the code of the meta-method
	<code>mm</code>. */
    public Set getCallSites(MetaMethod mm){
	Map map = (Map) callees2_cmpct.get(mm);
	if(map == null)
	    return Collections.EMPTY_SET;
	return map.keySet();
    }

    // set of all the encountered meta methods
    protected final Set all_meta_methods = new HashSet();

    /** Returns the set of all the meta methods that might be called during the
	execution of the program. */
    public Set getAllMetaMethods(){
	return all_meta_methods;
    }
    
}
