// PreOrderClazNumbering.java, created Sun Mar 10 05:24:15 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.Util.HClassUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>PreOrderClazNumbering</code> will number single-inheritance
 * instantiated classes (i.e. not interfaces or arrays of interfaces)
 * such that all classes numbered in an interval [a,b] will be
 * instances of the first common superclass of A and B, where
 * <code>clazNumber(A)==a</code> and <code>clazNumber(B)==b</code>.
 * This is useful for implementing quick 'instanceOf' tests;
 * to determine if an object is an instanceof class A, you can
 * simply see if the object falls within the interval
 * <code>[classNumber(A),maxChildNumber(A)]</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PreOrderClazNumbering.java,v 1.2 2002-04-10 03:03:43 cananian Exp $
 */
public class PreOrderClazNumbering extends ClazNumbering {
    private final Map<HClass,Integer> minmap = new HashMap<HClass,Integer>();
    private final Map<HClass,Integer> maxmap = new HashMap<HClass,Integer>();
    private final int max;
    
    /** Creates a <code>PreOrderClazNumbering</code>. */
    public PreOrderClazNumbering(ClassHierarchy ch) {
	Set<HClass> all = ch.instantiatedClasses();
	HClass root = Collections.min(all, new Comparator<HClass>() {
	    public int compare(HClass a, HClass b) {
		// major sort key is inheritance.
		if (a.isInstanceOf(b) && !b.isInstanceOf(a)) return 1;
		if (b.isInstanceOf(a) && !a.isInstanceOf(b)) return -1;
		// break ties by sorting lexicographically.
		return a.getDescriptor().compareTo(b.getDescriptor());
	    }
	});
	assert root.getName().equals("java.lang.Object");
	this.max = number(-1, root, ch);

    }
    private int number(int n, HClass hc, ClassHierarchy ch) {
	assert !(minmap.containsKey(hc) || maxmap.containsKey(hc)) :
	    "at entrance to number(), we shouldn't have a mapping for "+hc;
	if (HClassUtil.baseClass(hc).isInterface())
	    return n; // only single-inheritance tree.
	if (ch.instantiatedClasses().contains(hc)) n++;
	minmap.put(hc, new Integer(n));
	for (Iterator<HClass> it=ch.children(hc).iterator(); it.hasNext(); )
	    n=number(n, it.next(), ch);
	maxmap.put(hc, new Integer(n));
	assert minmap.containsKey(hc) && maxmap.containsKey(hc) :
	    "at exit to number(), we *should* have a mapping for "+hc;
	return n;
    }

    /** Returns the number associated with the given <code>HClass</code>. */
    public int clazNumber(HClass hc) {
	assert minmap.containsKey(hc) :
	    "clazNumber() not valid for the non-single-inheritance class "
	    +hc;
	return minmap.get(hc).intValue();
    }
    /** Returns the maximum number associated with an instantiatable
     *  child of the given <code>HClass</code>. */
    public int maxChildNumber(HClass hc) {
	assert maxmap.containsKey(hc) :
	    "maxChildNumber() not valid for the non-single-inheritance class "
	    +hc;
	return maxmap.get(hc).intValue();
    }
    /** Returns the smallest number which this <code>ClazNumbering</code>
     *  will associate with any <code>HClass</code>. */
    public int minNumber() { return 0; }
    /** Returns the largest number which this <code>ClazNumbering</code>
     *  will associate with any <code>HClass</code>. */
    public int maxNumber() { return max; }

}
