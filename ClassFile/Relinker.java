// Relinker.java, created Mon Dec 27 19:05:58 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;
/**
 * A <code>Relinker</code> object is a <code>Linker</code> where one
 * can globally replace references to a certain class with references
 * to another, different, class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Relinker.java,v 1.1.2.2 2000-01-11 12:33:45 cananian Exp $
 */
public class Relinker extends Linker {
    protected final Linker linker;

    /** Creates a <code>Relinker</code>. */
    public Relinker(Linker linker) {
	this.linker = linker;
    }
    protected HClass forDescriptor0(String descriptor) {
	return linker.forDescriptor(descriptor);
    }
    
    /** Creates a mutable class with the given name which is based on
     *  the given template class.  The name <b>need not</b> be unique.
     *  If a class with the given name already exists, all references
     *  to the existing class are changed to point to the new mutable
     *  class returned by this method. */
    public HClass createMutableClass(String name, HClass template) {
	try {
	    linker.createMutableClass(name, template);
	    return forName(name); // wrap w/ proxy class.
	} catch (DuplicateClassException e) {
	    HClass newClass = new HClassSyn(this, name, template);
	    HClass oldClass = forName(name); // get existing proxy class
	    if (oldClass.equals(template))
		newClass.hasBeenModified=false; // exact copy of oldClass
	    relink(oldClass, newClass);
	    return oldClass;
	}
    }

    /** Globally replace all references to <code>oldClass</code> with
     *  references to <code>newClass</code>, which may or may not have
     *  the same name.  The following constraint must hold:<pre>
     *  oldClass.getLinker()==newClass.getLinker()==this
     *  </pre><p>
     *  <b>WARNING:</b> the <code>hasBeenModified()</code> method of
     *  <code>HClass</code>is not reliable after calling 
     *  <code>relink()</code> if <code>oldClass.getName()</code> is not the
     *  same as <code>newClass.getName()</code>.  The value returned
     *  by <code>HClass.hasBeenModified()</code> will not reflect changes
     *  due to the global replacement of <code>oldClass</code> with
     *  <code>newClass</code> done by this <code>relink()</code>.</p>
     *  @exception RelinkError if there are outstanding references to
     *             fields or methods of <code>oldClass</code> which
     *             do not exist in <code>newClass</code>.
     */
    public void relink(HClass oldClass, HClass newClass) {
	Util.assert(oldClass.getLinker()==this);
	Util.assert(newClass.getLinker()==this);
	/* FIXME: not implemented. */
    }
}
