// Options.java, created Tue Jul 20 17:31:26 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Backend.Backend;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>Options</code> contains the values of the current runtime
 * environment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Options.java,v 1.6 2003-03-28 20:20:19 salcianu Exp $
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


    /** Returns the Backend ID (one of the constants defined in
	harpoon.Backend.Backend) that corresponds to the string
	representation <code>backendName</code>.  E.g., it returns
	<code>Backend.SPARC</code> for &quot;sparc&quot; etc. */
    static String getBackendID(String backendName) {
	return backendName.toLowerCase().intern();
    }

}
