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
 * <code>RegFileInfo</code> contains architecture specific information
 * about the registers for the Sparc architecture.  It also implements
 * the LocationFactory interface for allocating and tracking registers
 * which are used for tracking global data.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: RegFileInfo.java,v 1.1.2.2 1999-11-02 22:09:01 andyb Exp $
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

    public RegFileInfo() {

        /* Sparc registers:
         *   %g0 - %g7: global, general registers. %g0 is zero register.
         *   %o0 - %o7: registers for local data and arguments to called
         *              subroutines. %o6 is stack pointer, %o7 is called
         *              subroutine return address.
         *   %l0 - %l7: local variables
         *   %i0 - %i7: registers for incoming subroutine arguments.
         *              %i6 is frame pointer and %i7 is subroutine return
         *              address.
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
 
        /* AAA - still need to do liveOnExit, callerSave, and calleeSave
         * for real here */

        callerSaveRegs = new LinearSet(2);
        calleeSaveRegs = new LinearSet(2);
        liveOnExitRegs = new LinearSet(2);

        // liveOnExitRegs.add(reg[i]);
        // callerSaveRegs.add(reg[i]);
        // calleeSaveRegs.add(reg[i]);
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

    /* AAA - SpillException could go here - do I need to override? */

    public Temp[] getAllRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, reg);
    }

    public Temp getRegister(int index) { return getAllRegisters()[index]; }

    public Temp[] getGeneralRegisters() {
        return (Temp[]) Util.safeCopy(Temp.arrayFactory, generalRegs);
    }

    // implementing LocationFactory

    /* AAA - to do */
    public Location allocateLocation(final int type) {
        return null;
    }

    /* AAA - to do */
    public HData makeLocationData(final Frame f) {
        return null;
    }
}
