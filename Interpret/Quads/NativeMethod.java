// NativeMethod.java, created Mon Dec 28 10:07:27 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

import java.util.Hashtable;
/**
 * <code>NativeMethod</code> is an abstract superclass of all
 * native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NativeMethod.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
abstract class NativeMethod  {
    /** The implemented method. */
    abstract HMethod getMethod();
    /** Invoke the method. */
    abstract Object invoke(StaticState ss, Object[] params, Object closure)
	throws InterpretedThrowable;
}
