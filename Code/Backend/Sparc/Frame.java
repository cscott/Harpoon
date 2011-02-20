// Frame.java, created Tue Nov  2  2:07:04 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Backend.Generic.GCInfo;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

/**
 * <code>Sparc.Frame</code> contains architecture specific info
 * for the Sparc Backend.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Frame.java,v 1.2 2002-02-25 21:02:37 cananian Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame
{
    private final RegFileInfo regFileInfo;
    private final InstrBuilder instrBuilder;
    private final CodeGen codegen;
    private final harpoon.Backend.Generic.Runtime runtime;
    private final TempBuilder tempBuilder;
    private final Linker linker;
    private GCInfo gcInfo; // should really be final

    public Frame(HMethod main) {
	super();
	linker = main.getDeclaringClass().getLinker();
	tempBuilder = new TempBuilder();
	regFileInfo = new RegFileInfo(tempBuilder);
	instrBuilder = new InstrBuilder(regFileInfo, tempBuilder);

	harpoon.Backend.Runtime1.AllocationStrategy as =
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy(this,
								  "malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, false);
	codegen = new CodeGen(this);
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

    public harpoon.Backend.Generic.GCInfo getGCInfo() {
	return gcInfo;
    }
    public HCodeFactory getCodeFactory(HCodeFactory hcf) {
	return Code.codeFactory(hcf, this);
    }
}
