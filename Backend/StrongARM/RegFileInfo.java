// RegFileInfo.java, created Sat Sep 11 00:43:20 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.LocationFactory.Location;
import harpoon.Backend.Generic.RegFileInfo.VRegAllocator;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode.PrintCallback;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Data;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.ListFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

/**
 * <code>RegFileInfo</code> encapsulates information about the
 * StrongARM register set.  This object also implements
 * <code>Generic.LocationFactory</code>, allowing the creation of
 * global registers for the use of the runtime.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.27 2001-01-24 19:33:40 cananian Exp $
 */
public class RegFileInfo
    extends harpoon.Backend.Generic.RegFileInfo 
    implements harpoon.Backend.Generic.LocationFactory
{
    final Temp[] reg;
    final Set callerSaveRegs;
    final Set calleeSaveRegs;
    final Set liveOnExitRegs;
    final Temp[] regGeneral; 
    final TempFactory regtf;
    Set oneWordAssigns, twoWordAssigns;
    
    final Temp FP;  // Frame pointer
    final Temp IP;  // Scratch register 
    final Temp SP;  // Stack pointer
    final Temp LR;  // Link register
    final Temp PC;  // Program counter

    /** maxRegIndex returns an upper bound on the indexes that will be
	returned by the <code>MachineRegLoc</code>s for this backend.
	The indexes will fall in the range 
	0 &lt;= index &lt; maxRegIndex().
	This implementation returns 16, which includes system specific
	registers such as PC and SP; this may be an over-conservative
	value in need of revision!
     */
    public int maxRegIndex() { return 16; }

    /** Creates a <code>RegFileInfo</code>. 
     */
    public RegFileInfo() {
	reg = new Temp[16];
	regGeneral = new Temp[11];
	callerSaveRegs = new LinearSet(4);
	calleeSaveRegs = new LinearSet(9);
	liveOnExitRegs = new LinearSet(5);
        regtf = new TempFactory() {
            private int i = 0;
            private final String scope = "strongarm-registers";
	    
            /* StrongARM has 16 general purpose registers.
             * Special notes on ones we set aside:
             *  r11 = fp
             *  r12 = ip
             *  r13 = sp
             *  r14 = lr
             *  r15 = pc (yes that's right. you can access the 
             *              program counter like any other register)
             */
            private final String[] names = {"r0", "r1", "r2", "r3", "r4", "r5",
                                            "r6", "r7", "r8", "r9", "r10", 
                                            "fp", "ip", "sp", "lr", "pc"};
	    
            public String getScope() { return scope; }
            protected synchronized String getUniqueID(String suggestion) {
                Util.assert(i < names.length, "Don't use the "+
			    "TempFactory of Register Temps");
		i++;
                return names[i-1];
            }
        };

	class RegTemp extends Temp implements MachineRegLoc {
	    int offset;
	    RegTemp(TempFactory tf, int offset) {
		super(tf);
		this.offset = offset;
	    }
	    public int kind() { return MachineRegLoc.KIND; }
	    public int regIndex() { return offset; }
	    
	}

        for (int i = 0; i < 16; i++) {
            reg[i] = new RegTemp(regtf, i);
            if (i < 11) regGeneral[i] = reg[i];
        }
	
	FP = reg[11];
	IP = reg[12];
	SP = reg[13];
	LR = reg[14];
	PC = reg[15];
	
        liveOnExitRegs.add(reg[0]);  // return value
        liveOnExitRegs.add(reg[1]); // (possible) long word return value
        liveOnExitRegs.add(FP);
        liveOnExitRegs.add(SP);
        liveOnExitRegs.add(PC);

	// callee clobbers r0,r1,r2,r3,ip,lr
	for(int i=0; i<4; i++) {
	    callerSaveRegs.add(reg[i]);
	}
	callerSaveRegs.add(IP);
	callerSaveRegs.add(LR);
	
	// callee saves r4-r10,fp,sp
	for(int i=4; i<11; i++) {
	    calleeSaveRegs.add(reg[i]);
	}
	calleeSaveRegs.add(FP);
	calleeSaveRegs.add(SP);

	oneWordAssigns = new HashSet();
	for (int i=0; i<regGeneral.length; i++) {
	    Temp[] assign = new Temp[] { regGeneral[i] };
	    oneWordAssigns.add(Arrays.asList(assign));
	}
	oneWordAssigns = Collections.unmodifiableSet(oneWordAssigns);
	twoWordAssigns = new HashSet();
	for (int i=0; i<regGeneral.length-1; i++) {
	    Temp[] assign = new Temp[] { regGeneral[i] ,
					 regGeneral[i+1] };
	    twoWordAssigns.add(Arrays.asList(assign));
	}
	twoWordAssigns = Collections.unmodifiableSet(twoWordAssigns);
    }
    
    public Temp[] getAllRegisters() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg); 
    }

    public Temp getRegister(int index) {
	return reg[index];
    }

    public Temp[] getGeneralRegisters() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, regGeneral); 
    }

    private TempFactory regTempFactory() { return regtf; }

    public boolean isRegister(Temp t) {
	return t.tempFactory() == regTempFactory();
    }

    public List expand(Temp temp) {
	if (temp instanceof TwoWordTemp) {
	    TwoWordTemp tt = (TwoWordTemp) temp;
	    return Arrays.asList(new Temp[]
				 { tt.getLow(), tt.getHigh() });
	} else {
	    return super.expand(temp);
	}
    }

    static class MyVRegAllocator extends VRegAllocator {
	// not necessarily right approach... retry coding with
	// TwoWordTemps etc for Vregs... (because we don't need to
	// expose the need for multiple registers/temp until the last
	// part, right?)
	static final int numVregs = 11; // number of vregs in each set
	static int vregCtr = 0;
	
	// vregs maintains the constant set of virtual registers
	// two word temps draw two vregs from 'vregs', one word
	// temps draw one vreg, etc...
	Temp[] vregs;
	
	// vr2twt maintains a map from vregs to the two word temps
	// currently holding that vreg
	Map vr2twt;

	TempFactory vregtf;
	
	MyVRegAllocator() {
	    vregtf = new TempFactory() {
		private int i=0;
		private final String scope = 
		   "strongarm-virtual-registers";
		public String getScope() { return scope; }
		protected synchronized
		   String getUniqueID(String suggestion) {
		       return "vr"+vregCtr++;
		   }
	    };
	    
	    vr2twt = new HashMap();
	    vregs = new Temp[numVregs];
	    for(int i=0; i<vregs.length; i++) {
		vregs[i] = new Temp(vregtf);
	    }
	    
	}
	
	/** Returns a virtual register from the pool of virtual
	    registers maintained by this.
	    Note that regfile can contain keys that are machine
	    registers or that are virtual registers drawn from the
	    pool maintained by this.
	*/
	public Temp vreg(Temp t, Map regfile) throws SpillException { 
	    final ArrayList spills = new ArrayList();
	    if (t instanceof TwoWordTemp) {
		for (int i=0; i<vregs.length-1; i++) {
		    if (vregFree(regfile, vregs[i]) &&
			vregFree(regfile, vregs[i+1])) {
			Temp t2 = new TwoWordTemp
			    (vregtf, vregs[i], vregs[i+1]);
			vr2twt.put(vregs[i], t2);
			vr2twt.put(vregs[i+1], t2);
			return t2;
		    } else {
			// TODO: suggest spill

			return null;

		    }
		}
	    } else {

	    }
	    
	    return null;
	}

	private boolean vregFree(Map rf, Temp vreg) {
	    return (!rf.containsKey(vreg) &&
		    (vr2twt.containsKey(vreg)?
		     !rf.containsKey(vr2twt.get(vreg)):
		     true));
	}

    }

    public VRegAllocator allocator() {
	return new MyVRegAllocator();
    }

    public Set getRegAssignments(Temp t) {
	if (t instanceof TwoWordTemp) {
	    return twoWordAssigns;
	} else {
	    return oneWordAssigns;
	}
    }

    public Iterator suggestRegAssignment(Temp t, final Map regFile) 
	throws SpillException {
	final ArrayList suggests = new ArrayList();
	final ArrayList spills = new ArrayList();
	
	if (t instanceof TwoWordTemp) {
	    // double word, find two registers ( the strongARM
	    // doesn't require them to be in a row, but its 
	    // simpler to search for adjacent registers )
	    // FSK: forcing alignment to solve regalloc problem
	    for (int i=0; i<regGeneral.length-1; i+=2) {
		Temp[] assign = new Temp[] { regGeneral[i] ,
					     regGeneral[i+1] };
		if ((regFile.get(assign[0]) == null) &&
		    (regFile.get(assign[1]) == null)) {
		    suggests.add(Arrays.asList(assign));
		} else {
		    Set s = new LinearSet(2);
		    s.add(assign[0]);
		    s.add(assign[1]);
		    spills.add(s);
		}
	    }

	} else {
	    // single word, find one register
	    for (int i=0; i<regGeneral.length; i++) {
		if ((regFile.get(regGeneral[i]) == null)) {
		    suggests.add(ListFactory.singleton(regGeneral[i]));
		} else {
		    Set s = new LinearSet(1);
		    s.add(regGeneral[i]);
		    spills.add(s);
		}
	    }
	}
	if (suggests.isEmpty()) {
	    throw new SpillException() {
		public Iterator getPotentialSpills() {
		    // System.out.println("RFI: Spills.size() "+spills.size());
		    return spills.iterator();
		}
	    };
	}
	return suggests.iterator();
    }

    public Set liveOnExit() {
	return Collections.unmodifiableSet(liveOnExitRegs);
    }
    
    public Set callerSave() { 
	return Collections.unmodifiableSet(callerSaveRegs);
    }
    
    public Set calleeSave() { 
	return Collections.unmodifiableSet(calleeSaveRegs);
    }
    

    // LocationFactory interface.

    /** Allocate a global register of the specified type and return a
     *  handle to it.
     *  @param type a <code>IR.Tree.Type</code> specifying the type
     *              of the register.
     */
    public Location allocateLocation(final int type) {
	Util.assert(Type.isValid(type), "invalid type");
	Util.assert(!makeLocationDataCalled,
		    "allocateLocation() may not be called after "+
		    "makeLocationData() has been called.");
	Util.assert(type!=Type.LONG && type!=Type.DOUBLE,
		    "doubleword locations not implemented by this "+
		    "LocationFactory");
	// all other types of locations need a single register.

	// FSK: in theory, we could support arbitrary numbers of 
	// allocations by switching to mem locations.  But I don't
	// want to try to implement that yet.  
	Util.assert(regtop > 4, "allocated WAY too many locations, something's wrong");

	final Temp allocreg = reg[regtop--];

	// take this out of callersave, calleesave, etc.
	calleeSaveRegs.remove(allocreg);
	callerSaveRegs.remove(allocreg);
	liveOnExitRegs.remove(allocreg);

	return new Location() {
	    public Exp makeAccessor(TreeFactory tf, HCodeElement source) {
		return new TEMP(tf, source, type, allocreg);
	    }
	};
    }

    /** The index of the next register to be allocated. */
    private int regtop=10;

    // since we're just making global registers, we don't need to
    // allocate the storage anywhere.

    /** Create an <code>HData</code> which allocates static space for
     *  any <code>LocationFactory.Location</code>s that have been created.
     *  As this implementation only allocates global registers, the
     *  <code>HData</code> returned is always empty. */
    public HData makeLocationData(final Frame f) {
	// make sure we don't call allocateLocation after this.
	makeLocationDataCalled=true;
	// return an empty HData.
	return new Data("location-data",f) {
	    /** Global data, so <code>HClass</code> is <code>null</code>. */
	    public HClass getHClass() { return null; }
	    /** Empty tree, so root element is <code>null</code>. */
	    public HDataElement getRootElement() { return null; }
	    /** Tell a human reader that there is no data here. */
	    public void print(java.io.PrintWriter pw, PrintCallback cb) {
		pw.println("--- no data ---");
	    }
	};
    }
    private boolean makeLocationDataCalled=false;
}
