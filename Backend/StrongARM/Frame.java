// Frame.java, created Tue Feb 16 22:29:44 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Util.LinearSet;
import harpoon.Util.ListFactory;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>Frame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix Klock <pnkfelix@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.13 1999-10-20 20:34:13 cananian Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame {
    private final harpoon.Backend.Generic.Runtime   runtime;
    private final RegFileInfo regFileInfo; 
    private final InstrBuilder instrBuilder;
    private final CodeGen codegen;
    
    public Frame(HMethod main, ClassHierarchy ch, CallGraph cg) { 
	super();
	regFileInfo = new RegFileInfo();
	
	// FSK: grr.  I need to fix something.  right now the CodeGen
	// ctor needs regFileInfo set in 'this' Frame, which, while
	// doable, is a TERRIBLE design choice 
	codegen = new CodeGen(this);

	harpoon.Backend.Runtime1.AllocationStrategy as = // pick strategy
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy("_malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, ch, cg);
	instrBuilder = new InstrBuilder(regFileInfo);
    }

    public boolean pointersAreLong() { return false; }

    /** Returns a <code>StrongARM.CodeGen</code>. 
	Since no state is maintained in the returned
	<code>StrongARM.CodeGen</code>, the same one is returned on
	every call to this method.
     */
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
	// regfileinfo holds the location factory implementation for this frame
	return regFileInfo;
    }

    public harpoon.Backend.Generic.InstrBuilder getInstrBuilder() { 
	return instrBuilder; 
    }
}
