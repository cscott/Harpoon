// RegFileInfo.java, created Sat Sep 11 00:43:20 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.LocationFactory.Location;
import harpoon.ClassFile.HClass;
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
import harpoon.Util.LinearSet;
import harpoon.Util.ListFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;

/**
 * <code>RegFileInfo</code> encapsulates information about the
 * StrongARM register set.  This object also implements
 * <code>Generic.LocationFactory</code>, allowing the creation of
 * global registers for the use of the runtime.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.10 1999-12-03 23:52:08 pnkfelix Exp $
 */
public class RegFileInfo
    extends harpoon.Backend.Generic.RegFileInfo 
    implements harpoon.Backend.Generic.LocationFactory
{
    // FSK wants to relinquish author ship of the first half of this
    // file, since it was just cut-and-pasted out of the
    // hack-once-known-as-SAFrame

    final Temp[] reg;
    final Set callerSaveRegs;
    final Set calleeSaveRegs;
    final Set liveOnExitRegs;
    final Temp[] regGeneral; 
    final TempFactory regtf;
    
    final Temp FP;  // Frame pointer
    final Temp IP;  // Scratch register 
    final Temp SP;  // Stack pointer
    final Temp LR;  // Link register
    final Temp PC;  // Program counter

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
			    "TempFactory of Register bound Temps");
		i++;
                return names[i-1];
            }
        };
        for (int i = 0; i < 16; i++) {
            reg[i] = new Temp(regtf);
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
	callerSaveRegs.add(reg[12]);
	callerSaveRegs.add(reg[14]);
	
	// callee saves r4-r11,sp
	for(int i=4; i<12; i++) {
	    calleeSaveRegs.add(reg[i]);
	}
	calleeSaveRegs.add(reg[13]);
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

    public TempFactory regTempFactory() { return regtf; }

    public int getSize(Temp temp) {
	if (temp instanceof TwoWordTemp) {
	    return 2;
	} else {
	    return 1;
	}
    }

    public Iterator suggestRegAssignment(Temp t, final Map regFile) 
	throws harpoon.Backend.Generic.RegFileInfo.SpillException {
	final ArrayList suggests = new ArrayList();
	final ArrayList spills = new ArrayList();
	
	// macro renaming for clean code
	final Temp PRECOLORED = 
	    harpoon.Backend.Generic.RegFileInfo.PREASSIGNED;

	if (t instanceof TwoWordTemp) {
	    // double word, find two registers ( the strongARM
	    // doesn't require them to be in a row, but its 
	    // simpler to search for adjacent registers )
	    for (int i=0; i<regGeneral.length-1; i++) {
		Temp[] assign = new Temp[] { regGeneral[i] ,
					     regGeneral[i+1] };
		if ((regFile.get(assign[0]) == null) &&
		    (regFile.get(assign[1]) == null)) {
		    suggests.add(Arrays.asList(assign));
		} else {
		    // don't add precolored registers to potential
		    // spills. 
		    if ( regFile.get(assign[0]) != PRECOLORED &&
			 regFile.get(assign[1]) != PRECOLORED) {

			Set s = new LinearSet(2);
			s.add(assign[0]);
			s.add(assign[1]);
			spills.add(s);
		    }
		}
	    }

	} else {
	    // single word, find one register
	    for (int i=0; i<regGeneral.length; i++) {
		if ((regFile.get(regGeneral[i]) == null)) {
		    suggests.add(ListFactory.singleton(regGeneral[i]));
		} else {
		    Set s = new LinearSet(1);
		    
		    // don't add precolored registers to potential
		    // spills. 
		    if ( regFile.get(regGeneral[i]) != PRECOLORED ) {
			s.add(regGeneral[i]);
			spills.add(s);
		    }
		}
	    }
	}
	if (suggests.isEmpty()) {
	    throw new harpoon.Backend.Generic.RegFileInfo.SpillException() {
		public Iterator getPotentialSpills() {
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
	    public void print(java.io.PrintWriter pw) {
		pw.println("--- no data ---");
	    }
	};
    }
    private boolean makeLocationDataCalled=false;
}
