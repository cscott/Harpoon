// CompleteClazNumbering.java, created Sun Mar 10 21:01:51 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.Util.HClassUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>CompleteClazNumbering</code> extends PreOrderClazNumbering
 * to create a numbering valid for all classes in the given classhierarchy.
 * Single-inheritance instantiated classes are placed first, in the order
 * given by <code>PreOrderClazNumbering</code>, and multiple-inheritance
 * instantiated classes are placed next.  Lastly, we'll number all
 * non-instantiated classes.  This was we can use the lowest part of
 * the numbering to implement 'instanceOf' using the 
 * <code>PreOrderClazNumbering</code>, use a slightly-larger part of
 * the numbering to compactly encode the classes which can actually
 * tag an instantiated object, and still have a numbering which is
 * valid for all classes in the hierarchy (and thus is good for
 * sorting and such).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CompleteClazNumbering.java,v 1.1.2.1 2002-03-11 04:40:51 cananian Exp $
 */
public class CompleteClazNumbering extends ClazNumbering {
    private final Map<HClass,Integer> map = new HashMap<HClass,Integer>();
    private final int min, max;
    
    /** Creates a <code>CompleteClazNumbering</code>. */
    public CompleteClazNumbering(ClassHierarchy ch, PreOrderClazNumbering pocn)
    {
	this.min = pocn.minNumber();
	int n = pocn.maxNumber()+1;
	// start with the instantiated classes.
	for (Iterator<HClass> it=ch.instantiatedClasses().iterator();
	     it.hasNext(); ) {
	    HClass hc = it.next();
	    assert !hc.isInterface();
	    if (HClassUtil.baseClass(hc).isInterface())
		map.put(hc, new Integer(n++));
	    else
		map.put(hc, new Integer(pocn.clazNumber(hc)));
	}
	assert map.size()==n-min;
	// now do all the rest.
	for (Iterator<HClass> it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    if (!map.containsKey(hc))
		map.put(hc, new Integer(n++));
	}
	assert map.size()==n-min;
	this.max = n-1;
	assert this.min==Collections.min(map.values()).intValue();
	assert this.max==Collections.max(map.values()).intValue();
    }
    public CompleteClazNumbering(ClassHierarchy ch) {
	this(ch, new PreOrderClazNumbering(ch));
    }
    public int clazNumber(HClass hc) {
	assert map.containsKey(hc);
	return map.get(hc).intValue();
    }
    public int minNumber() {
	return min;
    }
    public int maxNumber() {
	return max;
    }
}
