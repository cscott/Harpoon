// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.8.2.6 1999-02-09 03:59:30 cananian Exp $
 */
public abstract class Main extends harpoon.IR.Registration {

    /** The compiler should be invoked with the names of classes to view.
     *  An optional "-code codename" option allows you to specify which
     *  codeview to use.
     */
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HCodeFactory hcf = // default code factory.
	    harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     );
	int n=0;  // count # of args/flags processed.
	for (; n < args.length ; n++) {
	    if (args[n].startsWith("-code")) {
		if (++n >= args.length)
		    throw new Error("-code option needs codename");
		hcf = HMethod.getCodeFactory(args[n]);
		if (hcf==null)
		    throw new Error("Invalid codename: "+args[n]);
	    } else break; // no more command-line options.
	}
	// rest of command-line options are class names.
	HClass interfaceClasses[] = new HClass[args.length-n];
	for (int i=0; i<args.length-n; i++)
	    interfaceClasses[i] = HClass.forName(args[n+i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
		HCode hc = hcf.convert(hm[j]);
		if (hc!=null) hc.print(out);
	    }
	}
    }
}
