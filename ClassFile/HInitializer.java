// HInitializer.java, created Sat Nov 28 13:54:33 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;

import java.lang.reflect.Modifier;

/**
 * An <code>HInitializer</code> provides information about a class
 * initializer method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HInitializer.java,v 1.3 2002-04-10 03:04:15 cananian Exp $
 * @see HMethod
 * @see HConstructor
 */

public interface HInitializer extends HMethod {
    /**
     * Returns the name of this class initializer, as a string.  This is
     * always the string "<code>&lt;clinit&gt;</code>".
     */
    public String getName();
    
    /** Determines whether this <code>HInitializer</code> is an interface
     *  method.
     *  @return false for all class initializers (even class initializers
     *          belonging to interface classes)
     */
    public boolean isInterfaceMethod();

    /**
     * Returns a hashcode for this class initializer.  This hashcode is
     * computed as the exclusive-or of the hashcodes of the initializer's
     * declaring class and the string "<code>&lt;clinit&gt;</code>".
     */
    public int hashCode();

    /** Array factory: returns new <code>HInitializer[]</code>. */
    public static final ArrayFactory<HInitializer> arrayFactory =
      Factories.hinitializerArrayFactory;
}
