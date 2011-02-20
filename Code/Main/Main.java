// Main.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <code>Main</code> is the command-line interface to the compiler.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Main.java,v 1.10 2003-07-10 02:01:38 cananian Exp $
 */
public abstract class Main extends harpoon.IR.Registration {

    /** The compiler should be invoked with the names of classes to view.
     *  An optional "-code codename" option allows you to specify which
     *  codeview to use.
     */
    public static void main(String args[]) throws java.io.IOException {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	Linker linker = Loader.systemLinker;
	HCodeFactory hcf = // default code factory.
	    harpoon.IR.Bytecode.Code.codeFactory();
	int n=0;  // count # of args/flags processed.
	for (; n < args.length ; n++) {
	    if (args[n].startsWith("-pass")) {
		if (++n >= args.length)
		    throw new Error("-pass option needs codename");
		hcf = Options.cfFromString(args[n], hcf, linker);
	    } else if (args[n].startsWith("-stat")) {
		String filename = "./phisig.data";
		if (args[n].startsWith("-stat:"))
		    filename = args[n].substring(6);
		try {
		    Options.statWriter = 
			new PrintWriter(new FileWriter(filename), true);
		} catch (IOException e) {
		    throw new Error("Could not open " + filename +
				    " for statistics: " + e.toString());
		}
	    } else break; // no more command-line options.
	}
	// rest of command-line options are class names.
	HClass interfaceClasses[] = new HClass[args.length-n];
	for (int i=0; i<args.length-n; i++)
	    interfaceClasses[i] = linker.forName(args[n+i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
		HCode hc = hcf.convert(hm[j]);
		if (hc!=null) hc.print(out);
	    }
	}
	if (Options.profWriter!=null) Options.profWriter.close();
	if (Options.statWriter!=null) Options.statWriter.close();
    }
}
