// InstrVisitor.java, created Mon Apr  5 17:24:45 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

/**
 * <code>InstrVisitor</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InstrVisitor.java,v 1.1.2.1 1999-04-05 21:36:36 pnkfelix Exp $
 */
public abstract class InstrVisitor  {
    
    /** Creates a <code>InstrVisitor</code>. */
    public InstrVisitor() {
        
    }
    
    public abstract void visit(Instr i);
}
