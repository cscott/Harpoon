// RegFileInfo.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.LocationFactory.Location;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Data;
import harpoon.IR.Tree.Exp;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.LinearSet;
import harpoon.Util.ListFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>RegFileInfo</code> contains architecture specific information
 * about the registers for the Sparc architecture.  It also implements
 * the LocationFactory interface for allocating and tracking registers
 * which are used for tracking global data.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.5 1999-11-23 09:39:05 andyb Exp $
 */
public class RegFileInfo 
  extends harpoon.Backend.Generic.RegFileInfo 
  implements harpoon.Backend.Generic.LocationFactory
{
    private final TempFactory regtf;
    private final Temp[] reg;
    private final Temp[] generalRegs;
    private final Set callerSaveRegs;
    private final Set calleeSaveRegs;
    private final Set liveOnExitRegs;
    private final Temp SP, FP;
    private final TempBuilder tb;

    public RegFileInfo(TempBuilder tb) {
	this.tb = tb;

	/* Sparc registers:
	 *   %g0 - %g7: global, general registers. %g0 is zero register.
	 *   %o0 - %o7: registers for local data and arguments to called
	 *		subroutines. %o6 is stack pointer, %o7 is called
	 *		subroutine return address.
	 *   %l0 - %l7: local variables
	 *   %i0 - %i7: registers for incoming subroutine arguments.
	 *		%i6 is frame pointer and %i7 is subroutine return
	 *		address.
	 */
	regtf = new TempFactory() {
	    private int i = 0;
	    private final String scope = "sparc-registers";
	    private final String[] names = {"%g0", "%g1", "%g2", "%g3",
		"%g4", "%g5", "%g6", "%g7", "%o0", "%o1", "%o2", "%o3",
		"%o4", "%o5", "%sp", "%o7", "%l0", "%l1", "%l2", "%l3",
		"%l4", "%l5", "%l6", "%l7", "%i0", "%i1", "%i2", "%i3",
		"%i4", "%i5", "%fp", "%i7" };

	    public String getScope() { return scope; }
	    public synchronized String getUniqueID(String suggestion) {
		Util.assert(i < names.length, "Already created all of "+
			    "the Register bound Temps!!!");
		i++;
		return names[i-1];
	    }
	};

	reg = new Temp[32]; 
	generalRegs = new Temp[27];

	int j = 0;
	for (int i = 0; i < 32; i++) {
	    reg[i] = new Temp(regtf);
	    if ((i != 0) && (i != 14) && (i != 15) && (i != 30) && (i != 31)) {
		generalRegs[j] = reg[i];
		j++;
	    }
	}

	SP = reg[14];
	FP = reg[30];
 
	// live on exit: %fp, %sp, %i0: return value, %i7: return address
	//		 %g0: zero, always live
	liveOnExitRegs = new LinearSet(5);  
	liveOnExitRegs.add(FP);
	liveOnExitRegs.add(SP);
	liveOnExitRegs.add(reg[0]);
	liveOnExitRegs.add(reg[24]);
	liveOnExitRegs.add(reg[31]);
 
	// caller saved: clobbered by callee
	// %g0-%g7, %o0-%o5, %o7 
	callerSaveRegs = new LinearSet(15);
	for (int i = 0; i < 16; i++)
	    if (i != 14)  /* i = 14 -> reg[14] -> %sp */
		callerSaveRegs.add(reg[i]);

	// callee needs to save these itself
	// none - all callee saving is done by the magic save
	// instruction, go sparc go woohoo
	calleeSaveRegs = new LinearSet(0);
    }

    // Sparc backend specific helpers...

    final Temp SP() { return SP; }
    final Temp FP() { return FP; }

    // And now for the implementation of Generic.RegFileInfo
	       
    public Set liveOnExit() { 
	return Collections.unmodifiableSet(liveOnExitRegs); 
    }
 
    public Set callerSave() { 
	return Collections.unmodifiableSet(callerSaveRegs); 
    }

    public Set calleeSave() { 
	return Collections.unmodifiableSet(calleeSaveRegs); 
    }

    public TempFactory regTempFactory() { return regtf; }

    public Iterator suggestRegAssignment(Temp t, final Map regFile)
	throws harpoon.Backend.Generic.RegFileInfo.SpillException {

	final ArrayList suggests = new ArrayList();
	final ArrayList spills = new ArrayList();

	if (tb.isTwoWord(t)) {
	    for (int i = 0; i < generalRegs.length - 1; i++) {
		Temp[] assign = new Temp[] { generalRegs[i],
					     generalRegs[i+1] };
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
	    for (int i = 0; i < generalRegs.length; i++) {
		if ((regFile.get(generalRegs[i]) == null)) {
		    suggests.add(ListFactory.singleton(generalRegs[i]));
		} else {
		    Set s = new LinearSet(1);
		    s.add(generalRegs[i]);
		    spills.add(s);
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

    public Temp[] getAllRegisters() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg);
    }

    public Temp getRegister(int index) { return reg[index]; }

    public Temp[] getGeneralRegisters() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, generalRegs);
    }

    // implementing LocationFactory

    // start allocating registers from %g7 down to %g1
    private int regtop = 7;
    private boolean makeLocationDataCalled = false;

    public Location allocateLocation(final int type) {
	Util.assert(Type.isValid(type), "Invalid type");
	Util.assert(type != Type.LONG && type != Type.DOUBLE,
		    "Doubleword global locations not implemented");
	Util.assert(!makeLocationDataCalled,
		    "Cannot allocate location - already ran makeLocationData");

	// Currently just uses the %g registers - nothing fancy yet.
	Util.assert(regtop > 0, "Sorry, can't do any more global locations");
    
	final Temp allocreg = reg[regtop--];

	calleeSaveRegs.remove(allocreg);
	callerSaveRegs.remove(allocreg);
	liveOnExitRegs.remove(allocreg);

	return new Location() {
	    public Exp makeAccessor(TreeFactory tf, HCodeElement source) {
		return new TEMP(tf, source, type, allocreg);
	    }
	};
    }

    // not allocating memory for these yet, so just return an empty 
    // HData for now
    public HData makeLocationData(final Frame f) {
	makeLocationDataCalled = true;
	return new Data("location-data", f) {
	    public HClass getHClass() { return null; }
	    public HDataElement getRootElement() { return null; }
	    public void print(java.io.PrintWriter pw) {
		pw.println("--- no data ---");
	    }
	};
    }
}
