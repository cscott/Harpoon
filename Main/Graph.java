// Graph.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>Graph</code> is a command-line graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.12 1998-10-17 03:08:53 marinov Exp $
 */

public abstract class Graph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	HClass cls = HClass.forName(args[0]);
	HMethod hm[] = cls.getDeclaredMethods();
	HMethod m = null;
	for (int i=0; i<hm.length; i++)
	    if (hm[i].getName().equals(args[1])) {
		m = hm[i];
		break;
	    }

	String codetype = "quad-ssa";
	if (args.length > 3)
	    codetype = args[3];
	HCode hc = m.getCode(codetype);

	String title = m.getName();
	if ((args.length<=2)||(args[2]=="CFG")) harpoon.Util.Graph.printCFG(hc,out,title);
	else if (args[2]=="dom") harpoon.Util.Graph.printDomTree(hc,out,title);
	else harpoon.Util.Graph.printDomTree(true,hc,out,title);
    }

}
