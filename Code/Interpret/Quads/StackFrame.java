// StackFrame.java, created Mon Dec 28 01:34:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HMethod;

/**
 * <code>StackFrame</code> implements the interpreted stack frame.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StackFrame.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
abstract class StackFrame  {
    abstract HMethod getMethod();
    abstract String  getSourceFile();
    abstract int     getLineNumber();

    public String toString() {
	return
	    getMethod().getDeclaringClass().getName() + "." +
	    getMethod().getName() +
	    "("+getSourceFile()+":"+getLineNumber()+")";
    }
}
