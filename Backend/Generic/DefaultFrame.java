// DefaultFrame.java, created Mon Feb 15  3:36:39 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Backend.Allocation.AllocationInfo;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CALL;
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

/**
 *  The DefaultFrame class implements the abstract methods of the 
 *  Frame class.  It is intended mostly for testing purposes, and 
 *  will have to be fixed up a bit if needed for general use.
 *
 *  @author  Duncan Bryce <duncan@lcs.mit.edu>
 *  @version $Id: DefaultFrame.java,v 1.1.2.21 1999-08-04 06:30:53 cananian Exp $
 */
public class DefaultFrame extends Frame implements AllocationInfo {

    private AllocationStrategy  m_allocator;
    private Temp                m_nextPtr;
    private Temp                m_memLimit;
    private OffsetMap           m_offsetMap;
    private Temp[]              m_registers;
    private TempFactory         m_tempFactory;
  
    public DefaultFrame() {
	throw new Error("Default constructor not impl");
    }

    public DefaultFrame(OffsetMap map) {
	this(map, null);
    }
	
    public DefaultFrame(OffsetMap map, AllocationStrategy st) {
	m_allocator   = st==null?new DefaultAllocationStrategy(this):st;
	m_tempFactory = Temp.tempFactory("");
	m_nextPtr     = new Temp(m_tempFactory);
	m_memLimit    = new Temp(m_tempFactory);
	if (map==null) throw new Error("Must specify OffsetMap");
	else m_offsetMap = map;
    }
	
    public Frame newFrame(String scope) {
        DefaultFrame fr = new DefaultFrame(m_offsetMap, m_allocator);
        fr.m_registers = new Temp[16];
        fr.m_tempFactory = Temp.tempFactory(scope);
	fr.m_nextPtr     = new Temp(fr.m_tempFactory);
	fr.m_memLimit    = new Temp(fr.m_tempFactory);
        for (int i = 0; i < 16; i++)
            fr.m_registers[i] = new Temp(fr.m_tempFactory, "register_");
        return fr;
    }

    public Exp memAlloc(Exp size) {
        return m_allocator.memAlloc(size);
    }
    
    public OffsetMap getOffsetMap() {
        return m_offsetMap;
    }

    public boolean pointersAreLong() {
        return false;
    }

    public Temp FP() {
        return getAllRegisters()[2];
    }

    public Temp[] getAllRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, m_registers);
    }

    public Temp[] getGeneralRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, m_registers);
    }

    public TempFactory tempFactory() { 
        return m_tempFactory;
    }

    public TempFactory regTempFactory() {
        return m_tempFactory;
    }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts, int[] paramtypes) {
        Util.assert(tf != null, "tf is null");
        Stm prologue = null;
        Stm move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 16; i++) {
            move =  new MOVE(tf, src, 
                        new TEMP(tf, src, paramtypes[i], paramdsts[i]),
                        new TEMP(tf, src, paramtypes[i], m_registers[i]));
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
	Instr dir1, dir2, dir3, dir4;

        dir1 = new Instr(inf, src, ".text", null, null);
        dir2 = new Instr(inf, src, ".align 0", null, null);
        dir3 = new Instr(inf, src, ".global " + 
                        inf.getMethod().getName() + ":", null, null);
        dir4 = new Instr(inf, src, inf.getMethod().getName() + ":",
                        null, null);

	Instr.insertInstrBefore(body, dir1);
	Instr.insertInstrAfter(dir1, dir2);
	Instr.insertInstrAfter(dir2, dir3);
	Instr.insertInstrAfter(dir3, dir4);
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


    /** Stub added by FSK */
    public List makeLoad(Temp reg, int offset, Instr template) {
	Util.assert(false, "DefaultFrame.makeLoad() Not implemented");
	return null;
    }

    /** Stub added by FSK */
    public List makeStore(Temp reg, int offset, Instr template) {
	Util.assert(false, "DefaultFrame.makeStore() Not implemented");
	return null;
    }

    /** Stub added by FSK */
    public Iterator suggestRegAssignment(Temp t, Map regfile) {
	Util.assert(false, "DefaultFrame.suggestRegAssigment() Not implemented");
	return null;
    }

    /** Not implemented. */
    public GenericCodeGen codegen() { 
	Util.assert(false, "DefaultFrame.codegen() Not implemented");
	return null;
    }

}





