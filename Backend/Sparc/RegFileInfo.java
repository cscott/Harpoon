// RegFileInfo.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.LocationFactory.Location;
import harpoon.ClassFile.HData;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.LinearSet;
import harpoon.Util.Util;

import java.lang.String;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * AAA - <code>RegFileInfo</code> 
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.1 1999-11-02 07:07:04 andyb Exp $
 */
public class RegFileInfo 
  extends harpoon.Backend.Generic.RegFileInfo 
  implements harpoon.Backend.Generic.LocationFactory
{
    final TempFactory regtf;
    final Temp[] reg;
    final Temp[] generalRegs;
    final Set callerSaveRegs;
    final Set calleeSaveRegs;
    final Set liveOnExitRegs;

    public RegFileInfo() {
        regtf = new TempFactory() {
            private int i = 0;
            private final String scope = "sparc-registers";
            private final String[] names = {"r0", "r1"};

            public String getScope() { return scope; }
            public synchronized String getUniqueID(String suggestion) {
                Util.assert(i < names.length, "Already created all of "+
			    "the Register bound Temps!!!");
	        i++;
                return names[i-1];
            }
        };
      
        reg = new Temp[2]; 
        generalRegs = new Temp[2];
        callerSaveRegs = new LinearSet(2);
        calleeSaveRegs = new LinearSet(2);
        liveOnExitRegs = new LinearSet(2);

        for (int i = 0; i < 2; i++) {
            reg[i] = new Temp(regtf);
            generalRegs[i] = reg[i];
            liveOnExitRegs.add(reg[i]);
            callerSaveRegs.add(reg[i]);
            liveOnExitRegs.add(reg[i]);
        }

        // add registers to liveOnExit, calleeSave, and callerSave
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

    public TempFactory regTempFactory() { return regtf; }

    /* AAA - to do */
    public Iterator suggestRegAssignment(Temp t, final Map regFile)
        throws harpoon.Backend.Generic.RegFileInfo.SpillException {

        return null;
    }

    // SpillException goes here ??

    public Temp[] getAllRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg);
    }

    public Temp getRegister(int index) { return getAllRegisters()[index]; }

    public Temp[] getGeneralRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, generalRegs);
    }

    // implementing LocationFactory

    public Location allocateLocation(final int type) {
        return null;
    }

    public HData makeLocationData(final Frame f) {
        return null;
    }
}
