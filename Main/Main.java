// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
package harpoon.Main;

import harpoon.ClassFile.*;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.1 1998-08-07 13:38:13 cananian Exp $
 */

public final class Main  {
    // hide away constructor.
    private Main() { }

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static final void main(String args[]) {
	HClass interfaceClasses[] = new HClass[args.length];
	for (int i=0; i<args.length; i++)
	    interfaceClasses[i] = HClass.forName(args[i]);
	// Do something intelligent with these classes. XXX
    }
}
