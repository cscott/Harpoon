// TempBuilder.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details
package harpoon.Backend.Sparc;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.IR.Tree.Typed;

import java.util.Set;
import java.util.HashSet;

/**
 * <code>TempBuilder</code> for creating Temps for the Sparc architecture,
 * and providing an interface for querying whether these Temps require
 * two words of storage or use floating point registers.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: TempBuilder.java,v 1.1.2.2 1999-11-02 22:09:01 andyb Exp $
 */
public class TempBuilder extends harpoon.Backend.Generic.TempBuilder {
    private Set twoWordTemps; 
    private Set floatingTemps;

    public TempBuilder() {
        twoWordTemps = new HashSet();
        floatingTemps = new HashSet();
    }

    public Temp makeTemp(Typed typed, TempFactory tf) {
        Temp temp = new Temp(tf);
        
        if (typed.isDoubleWord()) {
            twoWordTemps.add(temp);
        }
        if (typed.isFloatingPoint()) {
            floatingTemps.add(temp);
        } 
        return temp;
    }

    // Sparc Backend specific functions
    // Since the Temps are created here, I couldn't think of a better
    //  place to store and query this information - andyb

    public boolean isTwoWord(Temp temp) {
        return twoWordTemps.contains(temp); 
    }

    public boolean isFloatingPoint(Temp temp) {
        return floatingTemps.contains(temp);
    }
}
