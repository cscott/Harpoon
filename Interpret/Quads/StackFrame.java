// StackFrame.java, created Mon Dec 28 01:34:43 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

/**
 * <code>StackFrame</code> implements the interpreted stack frame.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StackFrame.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
abstract class StackFrame  {
    abstract HMethod getMethod();
    abstract String  getSourceFile();
    abstract int     getLineNumber();
}
