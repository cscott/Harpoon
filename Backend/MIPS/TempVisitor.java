// TempVisitor.java, created Wed Aug  4 14:58:46 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Temp.Temp;

/**
 * <code>TempVisitor</code> is an extension of
 * <code>TempVisitor</code> for handling extensions of
 * <code>Temp</code> local to the StrongARM backend.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TempVisitor.java,v 1.1.2.1 2000-06-26 18:37:13 witchel Exp $
 */
public abstract class TempVisitor {
    
    public abstract void visit(Temp t);

    public void visit(TwoWordTemp t) {
	visit((Temp)t);
    }
}
