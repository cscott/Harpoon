// InstrMOVEproxy.java, created Tue Aug 22 15:30:46 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;

import java.util.Arrays;
import java.util.List;
/**
 * <code>InstrMOVEproxy</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: InstrMOVEproxy.java,v 1.1.2.1 2000-08-23 06:33:25 pnkfelix Exp $
 */
class InstrMOVEproxy extends Instr {
    
    /** Creates a <code>InstrMOVEproxy</code>. */
    public InstrMOVEproxy(Instr src) {
	super(src.getFactory(), src, 
	      "", 
	      // " @proxy "+src.defC()+" <- "+src.useC(),
	      //src.getAssem(),
	      (Temp[])src.def().clone(), 
	      (Temp[])src.use().clone());
    }
    
}
