// ClassMethodMap.java, created Fri Oct  8 15:06:25 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Backend.Maps.MethodMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A <code>ClassMethodMap</code> is a method map for virtual methods of
 * an object (not static, not private, not constructors).  It uses the
 * natural ordering (under <code>Comparable</code> of the methods when
 * possible and caches results for efficiency.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassMethodMap.java,v 1.4 2002-04-10 03:02:22 cananian Exp $
 */
public class ClassMethodMap extends MethodMap {
    
    /** Creates a <code>ClassMethodMap</code>. */
    public ClassMethodMap() { /* no special initialization. */ }

    public int methodOrder(HMethod hm) {
	// method must be a virtual class method and thus
	// 1) not an interface method
	// 2) not static, not private, and not a constructor.
	assert !hm.isInterfaceMethod();
	assert isVirtual(hm);
	// check cache first.
	if (cache.containsKey(hm)) return ((Integer)cache.get(hm)).intValue();
	// we actually compute numbers for a whole class at a time.
	HClass hc = hm.getDeclaringClass();
	HClass sc = hc.getSuperclass();
	HMethod[] prenumbered = (sc==null)?new HMethod[0]:sc.getMethods();
	HMethod[] tobenumbered= hc.getMethods();
	int prenonstatic=0, tobenonstatic=0;
	// identify and remove methods matching signatures of methods
	// already numbered (because they belong to the parent)
	Map presigs = new HashMap();
	for (int i=0; i<prenumbered.length; i++) {
	    if (!isVirtual(prenumbered[i])) continue;
	    // not static, not private, and not a constructor...
	    prenonstatic++;
	    String sig = prenumbered[i].getName() +
		prenumbered[i].getDescriptor();
	    presigs.put(sig, prenumbered[i]);
	}
	// create remainder list of class-local (not inherited) methods.
	// (also filter out static methods)
	List remainder = new ArrayList();
	for (int i=0; i<tobenumbered.length; i++) {
	    if (!isVirtual(tobenumbered[i])) continue;
	    // not static, not private, and not a constructor...
	    tobenonstatic++;
	    String sig = tobenumbered[i].getName() +
		tobenumbered[i].getDescriptor();
	    if (!presigs.containsKey(sig))
		remainder.add(tobenumbered[i]);
	    else if (!cache.containsKey(tobenumbered[i]))
		// number inherited methods the same way they are numbered
		// in the parent.
		cache.put(tobenumbered[i],
			  new Integer(methodOrder((HMethod)presigs.get(sig))));
	}
	// now sort the class-local methods
	Collections.sort(remainder);
	// and make a coherent numbering.
	int num = prenonstatic;
	for (Iterator it=remainder.iterator(); it.hasNext(); )
	    cache.put(it.next(), new Integer(num++));
	assert num==tobenonstatic : num + "!=" + tobenonstatic;
	// by this point the method should have a numbering in the cache.
	return ((Integer)cache.get(hm)).intValue();
    }
    private final Map cache = new HashMap();

    /** Determine if a method is "virtual" -- that is, handled by this
     *  map (and included in the class dispatch table). */
    private static boolean isVirtual(HMethod hm) {
	if (hm.isStatic()) return false;
	if (Modifier.isPrivate(hm.getModifiers())) return false;
	if (hm instanceof HConstructor) return false;
	return true;
    }
}
