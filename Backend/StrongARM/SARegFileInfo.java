// SARegFileInfo.java, created Sat Sep 11 00:43:20 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.LinearSet;
import harpoon.Util.ListFactory;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;

/**
 * <code>SARegFileInfo</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SARegFileInfo.java,v 1.1.2.1 1999-09-11 05:43:19 pnkfelix Exp $
 */
public class SARegFileInfo extends RegFileInfo {

    // FSK wants to relinquish author ship of the first half of this
    // file, since it was just cut-and-pasted out of the
    // hack-once-known-as-SAFrame

    static Temp[] reg = new Temp[16];
    private static Temp[] regLiveOnExit = new Temp[5];
    private static Temp[] regGeneral = new Temp[11];
    private static TempFactory regtf;

    static final Temp TP;  // Top of memory pointer
    static final Temp HP;  // Heap pointer
    static final Temp FP;  // Frame pointer
    static final Temp IP;  // Scratch register 
    static final Temp SP;  // Stack pointer
    static final Temp LR;  // Link register
    static final Temp PC;  // Program counter

    static {
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

	TP = reg[9];
	HP = reg[10];
	FP = reg[11];
	IP = reg[12];
	SP = reg[13];
	LR = reg[14];
	PC = reg[15];

        regLiveOnExit[0] = reg[0];  // return value
        regLiveOnExit[1] = reg[1]; // return exceptional value
        regLiveOnExit[2] = FP;
        regLiveOnExit[3] = SP;
        regLiveOnExit[4] = PC;
        // offmap = new OffsetMap32(null);
    }
    
    /** Creates a <code>SARegFileInfo</code>. */
    public SARegFileInfo() {
        
    }

    public Temp FP() { return reg[11]; }

    public Temp[] getAllRegisters() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg); 
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

    public Iterator suggestRegAssignment(Temp t, final Map regFile) throws SpillException {
	final ArrayList suggests = new ArrayList();
	final ArrayList spills = new ArrayList();
	if (t instanceof TwoWordTemp) {
	    // double word, find two registers (the strongARM
	    // doesn't require them to be in a row, but its faster
	    // to search for adjacent registers for now.  Later we
	    // can change the system to make the iterator do a
	    // lazy-evaluation and dynamically create all pairs as
	    // requested.  
	    for (int i=0; i<regGeneral.length-1; i++) {
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
		    return spills.iterator();
		}
	    };
	}
	return suggests.iterator();
    }

    /** Returns the live registers on exit from a method for the
	strong-arm. 
    */ 
    public Set liveOnExit() {
	HashSet hs = new HashSet();
	hs.addAll(Arrays.asList(new Temp[]{ reg[0], TP, HP, FP,
						IP, SP, LR, PC }));
	return hs;
    }
    
}
