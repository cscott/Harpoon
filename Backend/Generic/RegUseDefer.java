// RegUseDefer.java, created Fri Sep 22 17:10:34 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

/**
 * <code>RegUseDefer</code> performs a Temp -> Register mapping on
 * Backend Codes.
 *
 * @see harpoon.IR.Assem.Instr
 * @see harpoon.Backend.Generic.Code;
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: RegUseDefer.java,v 1.1.2.3 2001-06-17 22:32:11 cananian Exp $
 */
public class RegUseDefer extends UseDefer {
    
    private Code assemCode;

    /** Creates a <code>RegUseDefer</code>. 

     */
    public RegUseDefer(Code assemblyCode) {
        assemCode = assemblyCode;
    }

    public Collection useC(HCodeElement hce) {
	Instr i = (Instr) hce;
	ArrayList list = new ArrayList();
	if (! (i instanceof harpoon.Analysis.Instr.InstrMOVEproxy)) // FSK: HACK
	    for(Iterator uses=i.useC().iterator(); uses.hasNext();) {
		list.addAll(assemCode.getRegisters(i,(Temp)uses.next()));
	    }
	return Collections.unmodifiableCollection(list);
    }

    public Collection defC(HCodeElement hce) {
	Instr i = (Instr) hce;
	ArrayList list = new ArrayList();
	if (! (i instanceof harpoon.Analysis.Instr.InstrMOVEproxy)) // FSK: HACK
	    for(Iterator defs=i.defC().iterator(); defs.hasNext();) {
		list.addAll(assemCode.getRegisters(i,(Temp)defs.next()));
	    }
	return Collections.unmodifiableCollection(list);
    }
   
}
