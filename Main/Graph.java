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
 * @version $Id: Graph.java,v 1.12.2.4 1999-02-04 07:18:25 duncan Exp $
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

	HCodeFactory hcf = null;
	if (args.length <=3 || args[3].equals("quad-ssa"))
	    hcf = harpoon.IR.Quads.QuadSSA.codeFactory();
	if (args.length > 3 && args[3].equals("quad-no-ssa"))
	    hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory();
	if (args.length > 3 && args[3].equals("quad-with-try"))
	    hcf = harpoon.IR.Quads.QuadWithTry.codeFactory();
	if (args.length > 3 && args[3].equals("low-quad-ssa"))
	    hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory();
	if (args.length > 3 && args[3].equals("low-quad-no-ssa"))
	    hcf = harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory();

	HCode hc = hcf.convert(m);

	String title = m.getName();
	if ((args.length<=2)||(args[2].equals("CFG"))) harpoon.Util.Graph.printCFG(hc,out,title);
	else if (args[2].equals("dom")) harpoon.Util.Graph.printDomTree(hc,out,title);
	else harpoon.Util.Graph.printDomTree(true,hc,out,title);
    }

}
