// HInitializer.java, created Sat Nov 28 13:54:33 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

import java.lang.reflect.Modifier;

/**
 * An <code>HInitializerImpl</code> is a basic implemention of
 * <code>HInitializer</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HInitializerImpl.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 * @see HInitializer
 */

abstract class HInitializerImpl extends HMethodImpl implements HInitializer {
    HInitializerImpl() { name="<clinit>"; returnType=HClass.Void; }
    
    /**
     * Returns the name of this class initializer, as a string.  This is
     * always the string "<code>&lt;clinit&gt;</code>".
     */
    public String getName() { return "<clinit>"; }
    
    public boolean isInterfaceMethod() { return false; }

    /**
     * Returns a hashcode for this class initializer.  This hashcode is
     * computed as the exclusive-or of the hashcodes of the initializer's
     * declaring class and the string "<code>&lt;clinit&gt;</code>".
     */
    public int hashCode() { return hashCode(this); }
    // factored out for re-use
    static int hashCode(HInitializer hi) {
	return hi.getDeclaringClass().hashCode() ^ hi.getName().hashCode();
    }
}
