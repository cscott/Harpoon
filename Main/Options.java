// Options.java, created Tue Jul 20 17:31:26 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Backend.Runtime1.AllocationStrategyFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

/**
 * <code>Options</code> contains the values of the current runtime
 * environment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Options.java,v 1.5 2003-02-11 21:49:20 salcianu Exp $
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
	if (name=="to-quad-ssa")
	    return harpoon.IR.Quads.QuadSSA.codeFactory(hcf);
	if (name=="to-quad-ssi")
	    return harpoon.IR.Quads.QuadSSI.codeFactory(hcf);
	if (name=="to-low-quad")
	    return harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
	if (name=="to-low-quad-ssa")
	    return harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	if (name=="to-low-quad-ssi")
	    return harpoon.IR.LowQuad.LowQuadSSI.codeFactory(hcf);
	if (name=="scc-opt")
	    return harpoon.Analysis.Quads.SCC.SCCOptimize.codeFactory(hcf);
	if (name=="ssi-stats")
	    return harpoon.Analysis.Quads.SSIStats.codeFactory(hcf);
	if (name=="type-switch-remover")
	    return new harpoon.Analysis.Quads.TypeSwitchRemover(hcf).codeFactory();
	if (name=="new-mover")
	    return new harpoon.Analysis.Quads.NewMover(hcf).codeFactory();
	else throw new Error("Unknown code factory type: "+name);
    }

    /** Create a frame object, given the name of a backend.

	@param backendName string name of the backend

	@param mainMethod  main method of the compiled program

	@param asFact factory that produces the
	<code>AllocationStrategy</code> for compiling allocation
	sites.  Currently, relevant only for the <code>PreciseC</code>
	backend.*/
    public static Frame frameFromString(String backendName, HMethod mainMethod,
					AllocationStrategyFactory asFact)
    {
	backendName = backendName.toLowerCase().intern();
	if (backendName == "strongarm")
	    return new harpoon.Backend.StrongARM.Frame(mainMethod);
	if (backendName == "sparc")
	    return new harpoon.Backend.Sparc.Frame(mainMethod);
	if (backendName == "mips")
	    return new harpoon.Backend.MIPS.Frame(mainMethod);
	if (backendName == "mipsyp")
	    return new harpoon.Backend.MIPS.Frame(mainMethod, "yp");
	if (backendName == "mipsda")
	    return new harpoon.Backend.MIPS.Frame(mainMethod, "da");
	if (backendName == "precisec")
	    return new harpoon.Backend.PreciseC.Frame(mainMethod, asFact);
	throw new Error("Unknown Backend: "+backendName);
    }

    /** Create a frame object, given the name of a backend. */
    public static Frame frameFromString(String backendName, HMethod mainMethod)
    {
	return frameFromString(backendName, mainMethod, null);
    }
}
