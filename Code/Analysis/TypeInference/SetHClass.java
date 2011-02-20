// SetHClass.java, created Wed Nov  4 17:21:09 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import harpoon.ClassFile.HClass;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>SetHClass</code> represents concrete type.
 * Right now it is just a <code>Set</code> of <code>HClass</code>es,
 * but optimized ways of representing cone of classes
 * (i.e. class and all its subclasses) can be considered.
 *
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: SetHClass.java,v 1.2 2002-02-25 21:00:38 cananian Exp $
 */

public class SetHClass extends HashSet {
    public SetHClass() { }
    public SetHClass(HClass c) { add(c); }
    public SetHClass(SetHClass s) { super(s); }
    /** finds the union of this set and the parameter and returns true 
     *  if some elements were added. 
     */
    boolean union(SetHClass s) { return addAll(s); }

    SetHClass getComponentType() {
	SetHClass s = new SetHClass();
	for (Iterator i=iterator(); i.hasNext(); ) {
	    HClass c = ((HClass)i.next()).getComponentType();
	    if (c!=null) s.add(c);
	}
	return s;
    }
    SetHClass copy() {
	return new SetHClass(this);
    }
    Enumeration elements() { return Collections.enumeration(this); }
}
