// HPointer.java, created Thu Dec 10 23:41:19 1998 by cananian
package harpoon.ClassFile;

/**
 * The <code>HPointer</code> interface allows us to resolve
 * pointers to <code>HClass</code>es transparently (and allows us
 * to demand-load class files, instead of doing them all at once).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HPointer.java,v 1.1.2.1 1998-12-11 06:54:52 cananian Exp $
 */
abstract class HPointer  {
    /** Returns a genuine HClass from the (possible) pointer. */
    abstract HClass actual();
    abstract String getName();
    abstract String getDescriptor();
}
