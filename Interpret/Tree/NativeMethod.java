// NativeMethod.java, created Mon Dec 28 10:07:27 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HMethod;

import java.util.Hashtable;
/**
 * <code>NativeMethod</code> is an abstract superclass of all
 * native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NativeMethod.java,v 1.2 2002-02-25 21:06:01 cananian Exp $
 */
abstract class NativeMethod  {
    /** The implemented method. */
    abstract HMethod getMethod();
    /** Invoke the method. */
    abstract Object invoke(StaticState ss, Object[] params)
	throws InterpretedThrowable;
}
