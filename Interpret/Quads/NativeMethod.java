// NativeMethod.java, created Mon Dec 28 10:07:27 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

import java.util.Hashtable;
/**
 * <code>NativeMethod</code> is an abstract superclass of all
 * native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NativeMethod.java,v 1.1.2.2 1998-12-30 04:39:40 cananian Exp $
 */
abstract class NativeMethod  {
    /** The implemented method. */
    abstract HMethod getMethod();
    /** Invoke the method. */
    abstract Object invoke(StaticState ss, Object[] params)
	throws InterpretedThrowable;
}
