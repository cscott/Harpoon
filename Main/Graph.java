// Graph.java, created Mon Sep 14 22:18:14 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Collections;
/**
 * <code>Graph</code> is a command-line graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.13 2002-02-25 21:06:05 cananian Exp $
 */

public abstract class Graph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	// USAGE: Graph -pass <codename> <classname> <methodname> <opt>
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	Linker linker = Loader.systemLinker;
	HCodeFactory hcf = // default code factory.
	    harpoon.IR.Bytecode.Code.codeFactory();
	int n=0;  // count # of args/flags processed.
	for (; n < args.length ; n++) {
	    if (args[n].startsWith("-pass")) {
		if (++n >= args.length)
		    throw new Error("-pass option needs codename");
		hcf = Options.cfFromString(args[n], hcf);
	    } else break; // no more command-line options.
	}

	HClass cls = linker.forName(args[n++]);
	HMethod hm[] = cls.getDeclaredMethods();
	HMethod m = null;
	for (int i=0; i<hm.length; i++)
	    if (hm[i].getName().equals(args[n])) {
		m = hm[i]; n++;
		break;
	    }
	if (m==null) throw new Error("Couldn't find method "+args[n-1]);

	HCode hc = hcf.convert(m);

	String title = m.getName();
	if ((args.length<=n)||(args[n].equals("CFG")))
	    harpoon.Util.Graph.printCFG(hc,out,title);
	else if (args[n].equals("dom"))
	    harpoon.Util.Graph.printDomTree(hc,out,title);
	else if (args[n].equals("post"))
	    harpoon.Util.Graph.printDomTree(true,hc,out,title);
	else if (args[n].startsWith("hier"))
	    harpoon.Util.Graph.printClassHierarchy(out, m, new QuadClassHierarchy(linker, Collections.singleton(m), new CachingCodeFactory(hcf)));
	else if (args[n].startsWith("BBCFG"))
	    harpoon.Util.Graph.printBBCFG(m, hcf, out);
    }

}
