// SetHClass.java, created Wed Nov  4 17:21:09 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.Util.HashSet;

import java.util.Enumeration;
import harpoon.ClassFile.*;
/**
 * <code>SetHClass</code> represents concrete type.
 * Right now it is just a <code>Set</code> of <code>HClass</code>es,
 * but optimized ways of representing cone of classes
 * (i.e. class and all its subclasses) can be considered.
 *
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SetHClass.java,v 1.1.2.3 1999-08-04 05:52:24 cananian Exp $
 */

public class SetHClass extends HashSet {
    public SetHClass() { }
    public SetHClass(HClass c) { h.put(c, c); }
    /** finds the union of this set and the parameter and returns true 
     *  if some elements were added. 
     */
    boolean union(SetHClass s) {
	boolean r = false;
	for (Enumeration e=s.h.keys(); e.hasMoreElements(); ) {
	    Object o = e.nextElement();
	    if (!h.containsKey(o)) {
		h.put(o, o);
		r = true;
	    }
	}
	return r;
    }
    SetHClass getComponentType() {
	SetHClass s = new SetHClass();
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    HClass c = ((HClass)e.nextElement()).getComponentType();
	    if (c!=null) s.h.put(c, c);
	}
	return s;
    }
    SetHClass copy() {
	SetHClass s = new SetHClass();
	for (Enumeration e=h.keys(); e.hasMoreElements(); ) {
	    HClass c = (HClass)e.nextElement();
	    if (c!=null) s.h.put(c, c);
	}
	return s;	
    }
}
