// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ProfileMain.java,v 1.1.2.1 1998-12-01 12:36:44 cananian Exp $
 */

public abstract class ProfileMain extends harpoon.IR.Registration {

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	HClass interfaceClasses[] = new HClass[args.length];
	for (int i=0; i<args.length; i++)
	    interfaceClasses[i] = HClass.forName(args[i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
		HCode hc = hm[j].getCode("quad-ssa");

		// added by mfoltz
		harpoon.Analysis.QuadSSA.Profile.optimize(hc);

		hc.print(out);
	    }
	}
    }
}
