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
 * @version $Id: Graph.java,v 1.11 1998-10-16 01:12:18 marinov Exp $
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
	String[] setup = null;
        String type = (args.length>2) ? args[2] : null;
        harpoon.Util.Graph.printGraph(hc,out,title,setup,type);
    }

}
