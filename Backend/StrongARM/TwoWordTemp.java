// TwoWordTemp.java, created Thu Jul 22 17:46:30 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * <code>TwoWordTemp</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TwoWordTemp.java,v 1.1.2.2 1999-08-04 19:58:59 pnkfelix Exp $
 */
public class TwoWordTemp extends Temp {
    
    private Temp low;
    private Temp high;

    /** Creates a <code>TwoWordTemp</code>. */
    public TwoWordTemp(TempFactory tf) {
	super(tf);
	low = new Temp(tf);
	high = new Temp(tf);
    }

    /** Returns the <code>Temp</code> representing the low order bits
	of <code>this</code>. 
    */ 
    public Temp getLow() {
	return low;
    }

    /** Returns the <code>Temp</code> representing the high order bits
	of <code>this</code>. 
    */ 
    public Temp getHigh() {
	return high; 
    }

    public void accept(SATempVisitor v) {
	v.visit(this);
    }
}
