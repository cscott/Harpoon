// TempBuilder.java, created Tue Nov  2  2:07:04 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details
package harpoon.Backend.Sparc;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.IR.Tree.Typed;
import harpoon.Util.Util;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * <code>TempBuilder</code> for creating Temps for the Sparc architecture,
 * and providing an interface for querying whether these Temps require
 * two words of storage or use floating point registers.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: TempBuilder.java,v 1.3 2002-02-26 22:44:45 cananian Exp $
 */
public class TempBuilder extends harpoon.Backend.Generic.TempBuilder {
    private Set twoWordTemps; 
    private Set floatingTemps;
    private Map lowToHighMap;

    public TempBuilder() {
	twoWordTemps = new HashSet();
	floatingTemps = new HashSet();
	lowToHighMap = new HashMap();
    }

    public Temp makeTemp(Typed typed, TempFactory tf) {
	Temp temp = new Temp(tf);
	
	if (typed.isDoubleWord()) {
	    twoWordTemps.add(temp);
	    Temp high = new Temp(tf);
	    lowToHighMap.put(temp, high);
	}
	if (typed.isFloatingPoint()) {
	    floatingTemps.add(temp);
	} 
	return temp;
    }

    // Sparc Backend specific functions
    // Since the Temps are created here, I couldn't think of a better
    //	place to store and query this information - andyb

    boolean isTwoWord(Temp temp) {
	return twoWordTemps.contains(temp); 
    }

    Temp getLow(Temp temp) {
	return temp;
    }

    Temp getHigh(Temp temp) {
	Util.ASSERT(isTwoWord(temp));
	return (Temp) lowToHighMap.get(temp);
    }

    boolean isFloatingPoint(Temp temp) {
	return floatingTemps.contains(temp);
    }
}
