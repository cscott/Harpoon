package harpoon.Backend.Generic;

import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationInfo;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
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
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

/**
 *  The DefaultFrame class implements the abstract methods of the 
 *  Frame class.  It is intended mostly for testing purposes, and 
 *  will have to be fixed up a bit if needed for general use.
 *
 *  @author  Duncan Bryce <duncan@lcs.mit.edu>
 *  @version $Id: DefaultFrame.java,v 1.1.2.8 1999-03-12 20:46:38 duncan Exp $
 */
public class DefaultFrame extends Frame implements DefaultAllocationInfo {

    private AllocationStrategy  m_allocator;
    private int                 m_nextPtr;
    private OffsetMap           m_offsetMap;
    private Temp[]              m_registers;
    private TempFactory         m_tempFactory;
  
    public DefaultFrame() {
        m_allocator   = new DefaultAllocationStrategy(this);
	m_nextPtr     = 0x0fff0000;  // completely arbitrary
        m_offsetMap   = new OffsetMap32(null); 
    }

    public DefaultFrame(OffsetMap map, AllocationStrategy st) {
	m_allocator = st;
	m_nextPtr   = 0;
	m_offsetMap = map;
    }
	
    public Frame newFrame(String scope) {
        DefaultFrame fr = new DefaultFrame(m_offsetMap, m_allocator);
        fr.m_registers = new Temp[16];
        fr.m_tempFactory = Temp.tempFactory(scope);
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
        return m_registers;
    }

    public Temp[] getGeneralRegisters() {
        return m_registers;
    }

    public TempFactory tempFactory() { 
        return m_tempFactory;
    }

    public TempFactory regTempFactory() {
        return m_tempFactory;
    }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts) {
        Util.assert(tf != null, "tf is null");
        Stm prologue = null;
        Stm move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 16; i++) {
            move =  new MOVE(tf, src, 
                        new TEMP(tf, src, Type.INT, paramdsts[i]),
                        new TEMP(tf, src, Type.INT, m_registers[i]));
            if (prologue == null) {
                prologue = move;
            } else {
                prologue = new SEQ(tf, src, move, prologue);
            }
        }
        return prologue;
    }

    public Instr[] procLiveOnExit(Instr[] body) {
        return body;
    }

    public Instr[] procAssemDirectives(Instr[] body) {
        Util.assert((body != null) && (body.length > 0));
        Instr[] newbody = new Instr[body.length + 4];
        HCodeElement src = body[0];
        InstrFactory inf = ((Instr)src).getFactory();
        newbody[0] = new Instr(inf, src, ".text", null, null);
        newbody[1] = new Instr(inf, src, ".align 0", null, null);
        newbody[2] = new Instr(inf, src, ".global " + 
                        inf.getMethod().getName() + ":", null, null);
        newbody[3] = new Instr(inf, src, inf.getMethod().getName() + ":",
                        null, null);
        System.arraycopy(body, 0, newbody, 4, body.length);
        return newbody;
    }

    /* Implementation of the DefaultAllocationInfo interface.
     * NOTE that this is not really a realistic implementation,
     * rather, it is a placeholder that allows me to test other
     * parts of the code.  
     */
    public Stm callGC(TreeFactory tf, HCodeElement src) { 
        return new CALL(tf, src, 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
		        new NAME(tf, src, new Label("RUNTIME_GC")),
			null); 
    }
    
    public Exp getMemLimit(TreeFactory tf, HCodeElement src) { 
        return new CONST(tf, src, 4000000);  // again, completely arbitrary
    }
  
    public MEM getNextPtr(TreeFactory tf, HCodeElement src) { 
	 return new MEM(tf, src, Type.INT,
			new CONST(tf, src, m_nextPtr)); 
    }
    
    public Stm exitOutOfMemory(TreeFactory tf, HCodeElement src) {
        return new CALL(tf, src, 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
		        new NAME(tf, src, new Label("RUNTIME_OOM")),
			null); 
    }
}





