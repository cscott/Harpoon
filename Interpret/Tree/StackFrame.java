// StackFrame.java, created Mon Dec 28 01:34:43 1998 by cananian
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HMethod;

/**
 * <code>StackFrame</code> implements the interpreted stack frame.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StackFrame.java,v 1.1.2.2 1999-05-10 00:01:16 duncan Exp $
 */
abstract class StackFrame extends HCLibrary {
    abstract HMethod getMethod();
    abstract String  getSourceFile();
    abstract int     getLineNumber();
}
