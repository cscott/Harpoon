// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.2 1998-09-01 23:48:30 cananian Exp $
 */

public final class Main  {
    // hide away constructor.
    private Main() { }

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out);

	HClass interfaceClasses[] = new HClass[args.length];
	for (int i=0; i<args.length; i++)
	    interfaceClasses[i] = HClass.forName(args[i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
		HCode hc = harpoon.IR.QuadSSA.Code.
		    convertFrom(hm[j].getCode("bytecode"));
		hc.print(out);
	    }
	}
    }
}
