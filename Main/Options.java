// Options.java, created Tue Jul 20 17:31:26 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HCodeFactory;
/**
 * <code>Options</code> contains the values of the current runtime
 * environment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Options.java,v 1.1.2.5 2000-02-25 00:54:21 cananian Exp $
 */
public class Options {
    /** Stream for writing statistics. */
    public static java.io.PrintWriter statWriter = null;
    /** Stream for writing profiling data. */
    public static java.io.PrintWriter profWriter = null;

    /** Make a code factory to implement a pass, given a string name. */
    public static HCodeFactory cfFromString(String name, HCodeFactory hcf) {
	name = name.intern();
	if (name=="to-quad-with-try")
	    return harpoon.IR.Quads.QuadWithTry.codeFactory(hcf);
	if (name=="to-quad")
	    return harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	if (name=="to-quad-ssi")
	    return harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	if (name=="to-low-quad")
	    return harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
	if (name=="to-low-quad-ssi")
	    return harpoon.IR.LowQuad.LowQuadSSI.codeFactory(hcf);
	if (name=="scc-opt")
	    return harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	if (name=="ssi-stats")
	    return harpoon.Analysis.Quads.SSIStats.codeFactory(hcf);
	else throw new Error("Unknown code factory type: "+name);
    }
}
