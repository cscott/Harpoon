// InstrMEM.java, created Mon Apr  5 17:27:57 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * <code>InstrMEM</code> is used to represent memory operations in
 * assembly-level instruction representations.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InstrMEM.java,v 1.1.2.2 1999-05-25 16:45:13 andyb Exp $ 
 */
public class InstrMEM extends Instr {
    
    /** Creates a <code>InstrMEM</code>. */
    public InstrMEM(InstrFactory inf, HCodeElement codeSrc, String assem, 
		    Temp[] dst, Temp[] tempSrc) {
        super(inf, codeSrc, assem, dst, tempSrc);
    }

    /** Accept a visitor */
    public void visit(InstrVisitor v) { v.visit(this); }    
}
