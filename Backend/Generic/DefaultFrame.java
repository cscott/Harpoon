package harpoon.Backend.Generic;

import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationInfo;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 *  The DefaultFrame class implements the abstract methods of the 
 *  Frame class.  It is intended mostly for testing purposes, and 
 *  will have to be fixed up a bit if needed for general use.
 *
 *  @author  Duncan Bryce <duncan@lcs.mit.edu>
 *  @version $Id: DefaultFrame.java,v 1.1.2.3 1999-02-17 07:15:11 andyb Exp $
 */
public class DefaultFrame implements Frame, DefaultAllocationInfo {

    private AllocationStrategy  m_allocator;
    private int                 m_nextPtr;
    private OffsetMap           m_offsetMap;
    private Temp[]              m_registers;
    private TempFactory         m_tempFactory;

    public DefaultFrame() {
        m_allocator   = new DefaultAllocationStrategy(this);
	m_nextPtr     = 0x0fff0000;  // completely arbitrary
        m_offsetMap   = new OffsetMap32(null);
	m_tempFactory = Temp.tempFactory("global");
	m_registers = new Temp[16];
	for (int i=0; i<16; i++) 
	    m_registers[i] = new Temp(m_tempFactory, "register_");
    }

    public Exp malloc(Exp size) {
        return m_allocator.malloc(size);
    }
    
    public OffsetMap offsetMap() {
        return m_offsetMap;
    }

    public boolean pointersAreLong() {
        return false;
    }

    public Temp RV() {
        return registers()[0];
    }

    public Temp RX() {
        return registers()[1];
    }

    public Temp FP() {
        return registers()[2];
    }

    public Temp[] registers() {
        return m_registers;
    }

    public TempFactory tempFactory() { 
        return m_tempFactory;
    }

    /* Implementation of the DefaultAllocationInfo interface.
     * NOTE that this is not really a realistic implementation,
     * rather, it is a placeholder that allows me to test other
     * parts of the code.  
     */
    public Stm GC(TreeFactory tf, HCodeElement src) { 
        return new CALL(tf, src, 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
		        new NAME(tf, src, new Label("RUNTIME_GC")),
			null); 
    }
    
    public Exp mem_limit(TreeFactory tf, HCodeElement src) { 
        return new CONST(tf, src, 4000000);  // again, completely arbitrary
    }
  
    public MEM next_ptr(TreeFactory tf, HCodeElement src) { 
        return new MEM(tf, src, Type.INT,
		       new CONST(tf, src, m_nextPtr));
    }
    
    public Stm out_of_memory(TreeFactory tf, HCodeElement src) {
        return new CALL(tf, src, 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
			new TEMP(tf, src, 
				 Type.POINTER, new Temp(tf.tempFactory())), 
		        new NAME(tf, src, new Label("RUNTIME_OOM")),
			null); 
    }

}

