// NativeStackFrame.java, created Mon Dec 28 17:22:54 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HMethod;

/**
 * <code>NativeStackFrame</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NativeStackFrame.java,v 1.1.2.2 1999-01-22 23:53:19 cananian Exp $
 */
final class NativeStackFrame extends StackFrame {
    final HMethod method;
    NativeStackFrame(HMethod method) { this.method = method; }
    HMethod getMethod() { return method; }
    String  getSourceFile() { return "--native--"; }
    int     getLineNumber() { return 0; }
}
