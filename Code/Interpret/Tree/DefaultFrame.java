// DefaultFrame.java, created Mon Feb 15  3:36:39 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Generic.InstrBuilder;
import harpoon.Backend.Generic.TempBuilder;
import harpoon.Backend.Generic.LocationFactory;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.Runtime;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *  The DefaultFrame class implements the abstract methods of the 
 *  Frame class.  It is intended mostly for testing purposes, and 
 *  will have to be fixed up a bit if needed for general use.
 *
 *  @author  Duncan Bryce <duncan@lcs.mit.edu>
 *  @version $Id: DefaultFrame.java,v 1.4 2002-04-10 03:05:56 cananian Exp $
 */
public class DefaultFrame extends harpoon.Backend.Generic.Frame
    implements AllocationInfo {

    private ClassHierarchy      m_classHierarchy;
    private AllocationStrategy  m_allocator;
    private Temp                m_nextPtr;
    private Temp                m_memLimit;
    private OffsetMap           m_offsetMap;
    private Runtime             m_runtime;
    private TempFactory         m_tempFactory;
    private static Temp[]       registers;
    private static TempFactory  regTempFactory;
    private final Linker	linker;

    static {
        regTempFactory = new TempFactory() {
            private int i = 0;
            private final String scope = "registers";
            private final String[] names = {"r0", "r1", "r2", "r3", "r4", "r5",
                                            "r6", "r7", "r8", "r9", "r10", 
                                            "fp", "ip", "sp", "lr", "pc"};

            public String getScope() { return scope; }
            protected synchronized String getUniqueID(String suggestion) {
                assert i < names.length : "Don't use the "+
			    "TempFactory of Register bound Temps";
		i++;
                return names[i-1];
            }
        };
	registers = new Temp[16];
        for (int i = 0; i < 16; i++) {
            registers[i] = new Temp(regTempFactory);
        }
    }


    public DefaultFrame(HMethod main, ClassHierarchy ch, OffsetMap map, AllocationStrategy st) {
	linker = main.getDeclaringClass().getLinker();
	m_classHierarchy = ch;
	m_allocator   = st==null?new DefaultAllocationStrategy(this):st;
	m_tempFactory = Temp.tempFactory("");
	m_nextPtr     = new Temp(m_tempFactory);
	m_memLimit    = new Temp(m_tempFactory);
	if (map==null) throw new Error("Must specify OffsetMap");
	else m_offsetMap = map;
	m_runtime = new harpoon.Backend.Runtime1.Runtime(this, null, main, false);
	setClassHierarchy(ch);
    }

    public Linker getLinker() { return linker; }

    /** Returns a <code>Tree.Exp</code> object which represents a pointer
     *  to a newly allocated block of memory, of the specified size.  
     *  Generates code to handle garbage collection, and OutOfMemory errors.
     */
    public Exp memAlloc(Exp size) {
        return m_allocator.memAlloc(size);
    }
    
    public OffsetMap getOffsetMap() {
        return m_offsetMap;
    }
    public Runtime getRuntime() {
	return m_runtime;
    }

    public boolean pointersAreLong() {
        return false;
    }

    public TempFactory tempFactory() { 
        return m_tempFactory;
    }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts, int[] paramtypes) {
        assert tf != null : "tf is null";
        Stm prologue = null;
        Stm move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 16; i++) {
            move =  new MOVE(tf, src, 
                        new TEMP(tf, src, paramtypes[i], paramdsts[i]),
                        new TEMP(tf, src, paramtypes[i], registers[i]));
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
	assert body != null;

        HCodeElement src = body;
        InstrFactory inf = ((Instr)src).getFactory();
	Instr dir1, dir2, dir3, dir4;

        dir1 = new Instr(inf, src, ".text", null, null);
        dir2 = new Instr(inf, src, ".align 0", null, null);
        dir3 = new Instr(inf, src, ".global " + 
                        inf.getMethod().getName() + ":", null, null);
        dir4 = new Instr(inf, src, inf.getMethod().getName() + ":",
                        null, null);

	dir1.insertAt(new InstrEdge(body.getPrev(), body));
	dir2.insertAt(new InstrEdge(dir1, body));
	dir3.insertAt(new InstrEdge(dir2, body));
	dir4.insertAt(new InstrEdge(dir3, body));
	return dir1;
    }

    /* Implementation of the DefaultAllocationInfo interface.
     * NOTE that this is not really a realistic implementation,
     * rather, it is a placeholder that allows me to test other
     * parts of the code.  
     */
    public Label exitOutOfMemory() { return new Label("RUNTIME_OOM"); }
    public Label GC()              { return new Label("RUNTIME_GC"); } 
    public Temp  getMemLimit()     { return m_memLimit; }
    public Temp  getNextPtr()      { return m_nextPtr; }


    /** Not implemented. */
    public harpoon.Backend.Generic.CodeGen getCodeGen() { 
	assert false : "DefaultFrame.getCodeGen() Not implemented";
	return null;
    }

    /** Stub added by FSK. */
    public InstrBuilder getInstrBuilder() {
	return null;
    }
    /** Stub added by FSK. */
    public TempBuilder getTempBuilder() {
	return null;
    }
    /** Stub added by CSA. */
    public LocationFactory getLocationFactory() {
	return null;
    }
    /** Stub added by FSK. */
    public RegFileInfo getRegFileInfo() {
	return m_regfileinfo;
    }
    private final RegFileInfo m_regfileinfo = new RegFileInfo() {
	public Set liveOnExit() { return java.util.Collections.EMPTY_SET; }
	public Set callerSave() { assert false : "die"; return null; }
	public Set calleeSave() { assert false : "die"; return null; }
	public TempFactory regTempFactory() { return regTempFactory; }
	public boolean isRegister(Temp t) {
	    return t.tempFactory() == regTempFactory();
	}
	public Iterator suggestRegAssignment(Temp t, Map regfile) {
	    /* stub */
	    return null;
	}
	public Temp[] getAllRegisters() {
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, registers);
	}
	public Temp[] getGeneralRegisters() {
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, registers);
	}
	public Temp FP() {
	    return getRegister(2);
	}
    };
    /** There are no plans to ever have the <code>Tree</code>
	interpreter support <code>GCInfo</code>, so 
	<code>getGCInfo</code> will always return null.
    */
    public GCInfo getGCInfo() {
	return null;
    }
    public HCodeFactory getCodeFactory(HCodeFactory hcf) { return null; }
}






