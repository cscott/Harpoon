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
 * @version $Id: Relinker.java,v 1.1.2.1 2000-01-10 21:36:28 cananian Exp $
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
    
    /** Globally replace all references to <code>oldClass</code> with
     *  references to <code>newClass</code>, which may or may not have
     *  the same name.  The following constraint must hold:<pre>
     *  oldClass.getLinker()==newClass.getLinker()==this
     *  </pre>
     *  @exception RelinkError if there are outstanding references to
     *             fields or methods of <code>oldClass</code> which
     *             do not exist in <code>newClass</code>.
     */
    public void relink(HClass oldClass, HClass newClass) {
	Util.assert(oldClass.getLinker()==this);
	Util.assert(newClass.getLinker()==this);
	/* FIXME */
    }
}
