// HInitializerSyn.java, created Tue Jan 11 02:40:46 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.lang.reflect.Modifier;
/**
 * An <code>HInitializerSyn</code> is a mutable representation of
 * a class initializer method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HInitializerSyn.java,v 1.1.4.1 2000-01-13 23:47:47 cananian Exp $
 */
class HInitializerSyn extends HMethodSyn implements HInitializer {
    
    /** Create a new class initializer in class <code>parent</code>. */
    public HInitializerSyn(HClassSyn parent) {
	super(parent, "<clinit>", "()V");
	this.modifiers = Modifier.STATIC | Modifier.FINAL;
    }
    
    // can't really change any of the properties of a class initializer.
    public HMethodMutator getMutator() { return null; }
   
    public int hashCode() { return HInitializerImpl.hashCode(this); }
}
