// Frame.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

/**
 * <code>Sparc.Frame</code> contains architecture specific info
 * for the Sparc Backend.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.4 2000-01-13 23:47:41 cananian Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame
{
    private final RegFileInfo regFileInfo;
    private final InstrBuilder instrBuilder;
    private final CodeGen codegen;
    private final harpoon.Backend.Generic.Runtime runtime;
    private final TempBuilder tempBuilder;
    private final Linker linker;

    public Frame(HMethod main, ClassHierarchy ch, CallGraph cg) {
	super();
	linker = main.getDeclaringClass().getLinker();
	tempBuilder = new TempBuilder();
	regFileInfo = new RegFileInfo(tempBuilder);
	instrBuilder = new InstrBuilder(regFileInfo, tempBuilder);

	codegen = new CodeGen(this);

	harpoon.Backend.Runtime1.AllocationStrategy as =
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy("_malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, ch, cg);
    }

    public Linker getLinker() { return linker; }

    public boolean pointersAreLong() { return false; }

    public harpoon.Backend.Generic.CodeGen getCodeGen() {
	return codegen;
    }

    public harpoon.Backend.Generic.Runtime getRuntime() {
	return runtime;
    }

    public harpoon.Backend.Generic.RegFileInfo getRegFileInfo() {
	return regFileInfo;
    } 

    public harpoon.Backend.Generic.LocationFactory getLocationFactory() {
	return regFileInfo;
    }

    public harpoon.Backend.Generic.InstrBuilder getInstrBuilder() { 
	return instrBuilder; 
    }

    public harpoon.Backend.Generic.TempBuilder getTempBuilder() {
	return tempBuilder;
    }
}
