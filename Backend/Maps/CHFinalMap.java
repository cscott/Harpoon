// CHFinalMap.java, created Fri Oct 20 23:09:43 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import net.cscott.jutil.WorkSet;

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
 * @version $Id: CHFinalMap.java,v 1.3 2004-02-08 01:57:37 cananian Exp $
 */
public class CHFinalMap extends DefaultFinalMap
    implements java.io.Serializable {
    private final ClassHierarchy ch;
    
    /** Creates a <code>CHFinalMap</code>. */
    public CHFinalMap(ClassHierarchy ch) { this.ch = ch; }

    public boolean isFinal(HClass hc) {
	return super.isFinal(hc) || ch.children(hc).size()==0;
    }
    public boolean isFinal(HMethod hm) {
	// abstract methods are never final, even if no reachable methods
	// implement it. (this deals with unexecutable method calls)
	if (Modifier.isAbstract(hm.getModifiers())) return false;
	// at least as precise as DefaultFinalMap
	if (super.isFinal(hm)) return true;
	// call non-virtual methods final.
	if (hm.isStatic() ||
	    Modifier.isPrivate(hm.getModifiers()) ||
	    hm instanceof HConstructor) return true;
	// next bit is time consuming.  check cache first.
	if (cache==null) cache = new HashMap();
	if (!cache.containsKey(hm)) {
	    // if no overrides, this is final.
	    cache.put(hm, new Boolean(ch.overrides(hm).size()==0));
	}
	return ((Boolean) cache.get(hm)).booleanValue();
    }
    private transient Map cache = null;
}
