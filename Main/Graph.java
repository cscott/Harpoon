// Graph.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>Graph</code> is a command-line graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.12.2.6 1999-02-25 21:13:35 cananian Exp $
 */

public abstract class Graph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
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

	HClass cls = HClass.forName(args[n++]);
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
	else harpoon.Util.Graph.printDomTree(true,hc,out,title);
    }

}
