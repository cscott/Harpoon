// InstrMOVEproxy.java, created Tue Aug 22 15:30:46 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import java.util.Arrays;
import java.util.List;
/**
 * <code>InstrMOVEproxy</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: InstrMOVEproxy.java,v 1.1.2.4 2000-10-04 19:20:58 pnkfelix Exp $
 */
public class InstrMOVEproxy extends Instr {
    
    /** Creates a <code>InstrMOVEproxy</code>. */
    public InstrMOVEproxy(Instr src) {
	super(src.getFactory(), src, 
	      "", 
	      // " @proxy "+src.defC()+" <- "+src.useC(),
	      // " @proxy "+src.getAssem(),
	      (Temp[])src.def().clone(), 
	      (Temp[])src.use().clone());
    }
    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	return new InstrMOVEproxy
	    (new Instr(inf, this, getAssem(),
		       map(defMap,def()), map(useMap,use())));
    }
    
}
