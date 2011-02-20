// Backend.java, created Fri Mar 28 13:18:46 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend;

import harpoon.Backend.Runtime1.AllocationStrategyFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HMethod;

/**
 * <code>Backend</code> is a convenient, top-level class for our many
 * backends.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: Backend.java,v 1.2 2003-03-28 20:26:22 salcianu Exp $ */
public abstract class Backend {

    /** StrongARM backend */
    public static final String STRONGARM = "strongarm".intern();
    /** MIPS backend */
    public static final String MIPS      = "mips".intern();
    /** SPARC backend */
    public static final String SPARC     = "sparc".intern();
    /** PreciseC backend.  In this case, the compiler generates C
        files, full of data layout information to support precise
        garbage collectors. */
    public static final String PRECISEC  = "precisec".intern();
    /** MIPS with support for last line accesses is tag unchecked */
    public static final String MIPSYP    = "mipsyp".intern();
    /** MIPS with support for direct address registers */
    public static final String MIPSDA    = "mipsda".intern();


    /** Create a frame object, given the name of a backend.

	@param backendName string name of the backend

	@param mainMethod  main method of the compiled program

	@param asFact factory that produces the
	<code>AllocationStrategy</code> for compiling allocation
	sites.  Currently, relevant only for the <code>PreciseC</code>
	backend.*/
    public static Frame getFrame(String backendName, HMethod mainMethod,
				 AllocationStrategyFactory asFact)
    {
	backendName = backendName.intern();

	// TODO: use reflection
	if (backendName == STRONGARM)
	    return new harpoon.Backend.StrongARM.Frame(mainMethod);
	if (backendName == SPARC)
	    return new harpoon.Backend.Sparc.Frame(mainMethod);
	if (backendName == MIPS)
	    return new harpoon.Backend.MIPS.Frame(mainMethod);
	if (backendName == MIPSYP)
	    return new harpoon.Backend.MIPS.Frame(mainMethod, "yp");
	if (backendName == MIPSDA)
	    return new harpoon.Backend.MIPS.Frame(mainMethod, "da");
	if (backendName == PRECISEC)
	    return new harpoon.Backend.PreciseC.Frame(mainMethod, asFact);
	throw new Error("Unknown Backend: " + backendName);
    }

    /** Create a frame object, given the name of a backend. */
    public static Frame getFrame(String backendName, HMethod mainMethod)
    {
	return getFrame(backendName, mainMethod, null);
    }
}
