// StackFrame.java, created Mon Dec 28 01:34:43 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HMethod;

/**
 * <code>StackFrame</code> implements the interpreted stack frame.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StackFrame.java,v 1.1.2.2 1999-01-22 23:53:19 cananian Exp $
 */
abstract class StackFrame  {
    abstract HMethod getMethod();
    abstract String  getSourceFile();
    abstract int     getLineNumber();
}
