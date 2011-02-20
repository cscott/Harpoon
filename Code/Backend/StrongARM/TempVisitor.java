// TempVisitor.java, created Wed Aug  4 14:58:46 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;

/**
 * <code>TempVisitor</code> is an extension of
 * <code>TempVisitor</code> for handling extensions of
 * <code>Temp</code> local to the StrongARM backend.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TempVisitor.java,v 1.2 2002-02-25 21:02:50 cananian Exp $
 */
public abstract class TempVisitor {
    
    public abstract void visit(Temp t);

    public void visit(TwoWordTemp t) {
	visit((Temp)t);
    }
}
