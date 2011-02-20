// TempBuilder.java, created Thu Oct 21 17:58:01 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.IR.Tree.Typed;

/**
 * <code>TempBuilder</code> for StrongARM.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TempBuilder.java,v 1.2 2002-02-25 21:02:50 cananian Exp $
 */
public class TempBuilder extends harpoon.Backend.Generic.TempBuilder {
    
    /** Creates a <code>TempBuilder</code>. */
    public TempBuilder() {
        
    }
    
    public Temp makeTemp(Typed t, TempFactory tf) {
	if (t.isDoubleWord()) {
	    return new TwoWordTemp(tf);
	} else {
	    return new Temp(tf);
	}
    }
}
