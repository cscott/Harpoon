// SAFrame.java, created Tue Feb 16 22:29:44 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Allocation.AllocationInfo;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Backend.Generic.InstrBuilder;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.ClassFile.HCodeElement;
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
 * <code>SAFrame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix Klock <pnkfelix@mit.edu>
 * @version $Id: SAFrame.java,v 1.1.2.41 1999-09-11 17:58:00 cananian Exp $
 */
public class SAFrame extends Frame implements AllocationInfo {
    private AllocationStrategy mas;
    private final OffsetMap offmap;
    private final Runtime   runtime;
    private final SARegFileInfo regFileInfo; 
    private final SAInstrBuilder instrBuilder;

    harpoon.Backend.Generic.CodeGen codegen;
    
    public SAFrame(ClassHierarchy ch) { 
	super();
        mas = new DefaultAllocationStrategy(this);
	codegen = new CodeGen(this);
	runtime = new harpoon.Backend.Runtime1.Runtime();
	offmap = new OffsetMap32(ch, runtime.nameMap());
	regFileInfo = new SARegFileInfo();
	instrBuilder = new SAInstrBuilder(regFileInfo);
    }

    public boolean pointersAreLong() { return false; }


    /* Generic version of the next six methods copied from 
     * DefaultFrame for now */

    public Label exitOutOfMemory() { return new Label("_EXIT_OOM"); }
    public Label GC()              { return new Label("_RUNTIME_GC"); }
    public Temp  getMemLimit()     { return SARegFileInfo.TP; } 
    public Temp  getNextPtr()      { return SARegFileInfo.HP; }
    public Exp memAlloc(Exp size) { return mas.memAlloc(size); }
    public OffsetMap getOffsetMap() { return offmap; }
    public Runtime getRuntime() { return runtime; }


    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts, int[] paramtypes) { 
        Stm prologue = null, move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 4; i++) {
            move = new MOVE(tf, src,
                            new TEMP(tf, src, paramtypes[i], paramdsts[i]),
                            new TEMP(tf, src, paramtypes[i], SARegFileInfo.reg[i]));
            if (prologue == null) {
                prologue = move;
            } else {
                prologue = new SEQ(tf, src, move, prologue);
            }
        }
        return prologue;
    }

    public Instr procLiveOnExit(Instr body) { 
        return body; 
    }

    public Instr procAssemDirectives(Instr body) { 
        Util.assert(body != null);

        HCodeElement src = body;
        InstrFactory inf = ((Instr)src).getFactory();
	Instr dir1, dir2, dir3, dir4, dir5, dir6, dir7;

        dir1 = new InstrDIRECTIVE(inf, src, ".text");
        dir2 = new InstrDIRECTIVE(inf, src, ".align 0");
        dir3 = new InstrDIRECTIVE(inf, src, ".global " + 
                        offmap.label(inf.getMethod()));
        /* this should be a label */
        dir4 = new InstrLABEL(inf, src, 
                        offmap.label(inf.getMethod()) + ":",
                        offmap.label(inf.getMethod()));
        dir5 = new Instr(inf, src, "mov ip, sp", null, null);
        dir6 = new Instr(inf, src, "stmfd sp!, {fp, ip, lr, pc}",
                              null, null);
        dir7 = new Instr(inf, src, "sub fp, ip, #4", null, null);

	// Instr.insertInstrBefore(body, dir1);
	dir1.insertAt(new InstrEdge(body.getPrev(), body));
	dir2.insertAt(new InstrEdge(dir1, body));
	dir3.insertAt(new InstrEdge(dir2, body));
	dir4.insertAt(new InstrEdge(dir3, body));
	dir5.insertAt(new InstrEdge(dir4, body));
	dir6.insertAt(new InstrEdge(dir5, body));
	dir7.insertAt(new InstrEdge(dir6, body));

	return dir1;
    }


    /** Returns a <code>StrongARM.CodeGen</code>. 
	Since no state is maintained in the returned
	<code>StrongARM.CodeGen</code>, the same one is returned on
	every call to this method.
     */
    public harpoon.Backend.Generic.CodeGen codegen() { 
	return codegen;
    }


    public RegFileInfo getRegFileInfo() { return regFileInfo; }

    public InstrBuilder getInstrBuilder() { return instrBuilder; }
}
