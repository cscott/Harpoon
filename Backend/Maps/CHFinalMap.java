// CHFinalMap.java, created Fri Oct 20 23:09:43 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Util.WorkSet;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
/**
 * <code>CHFinalMap</code> is a slightly smarter <code>FinalMap</code>
 * that, given a <code>ClassHierarchy</code> for context, aggressively
 * makes methods final if the <code>ClassHierarchy</code> doesn't contain
 * a reachable method which overrides it.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CHFinalMap.java,v 1.1.2.1 2000-10-21 20:05:58 cananian Exp $
 */
public class CHFinalMap extends DefaultFinalMap {
    private final ClassHierarchy ch;
    
    /** Creates a <code>CHFinalMap</code>. */
    public CHFinalMap(ClassHierarchy ch) { this.ch = ch; }

    public boolean isFinal(HClass hc) {
	return super.isFinal(hc) || ch.children(hc).size()==0;
    }
    public boolean isFinal(HMethod hm) {
	if (super.isFinal(hm)) return true;
	// call non-virtual methods final.
	if (hm.isStatic() ||
	    Modifier.isPrivate(hm.getModifiers()) ||
	    hm instanceof HConstructor) return true;
	// next bit is time consuming.  check cache first.
	if (cache.containsKey(hm))
	    return ((Boolean) cache.get(hm)).booleanValue();
	// go through all children of the declaring class, looking for
	// a method which overrides this.
	WorkSet ws = new WorkSet(ch.children(hm.getDeclaringClass()));
	while (!ws.isEmpty()) {
	    HClass hc = (HClass) ws.pop();
	    try {
		hc.getDeclaredMethod(hm.getName(), hm.getDescriptor());
		// not final, we found a method that overrides it.
		cache.put(hm, new Boolean(false));
		return false; 
	    } catch (NoSuchMethodError nsme) {
		// keep looking for subclasses that declare method:
		// add all subclasses of this one to the worklist.
		ws.addAll(ch.children(hc));
	    }
	}
	// this method is final.
	cache.put(hm, new Boolean(true));
	return true;
    }
    private final Map cache = new HashMap();
}
