// StackFrame.java, created Mon Dec 28 01:34:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HMethod;

/**
 * <code>StackFrame</code> implements the interpreted stack frame.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StackFrame.java,v 1.1.2.4 2000-01-13 23:48:13 cananian Exp $
 */
abstract class StackFrame extends Debug {
    abstract HMethod getMethod();
    abstract String  getSourceFile();
    abstract int     getLineNumber();
}
