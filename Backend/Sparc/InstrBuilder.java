// InstrBuilder.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.util.Arrays;
import java.util.List;

/**
 * <code>Sparc.InstrBuilder</code> is another implementation of
 * <code>Generic.InstrBuilder</code> - for the Sparc architecture.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrBuilder.java,v 1.1.2.5 1999-11-04 09:15:08 andyb Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {
    private final RegFileInfo regFileInfo;
    private final TempBuilder tempBuilder;

    InstrBuilder(RegFileInfo regFileInfo, TempBuilder tempBuilder) {
	super();
	this.regFileInfo = regFileInfo;
	this.tempBuilder = tempBuilder;
    }

    public List makeLoad(Temp r, int offset, Instr template) {
	if (tempBuilder.isTwoWord(r)) {
	    // Should do some stuff later to convert this to ldd?
	    InstrMEM load1 =
		new InstrMEM(template.getFactory(), template,
			     "ld [`s0 + " + (-4*offset) + "], `d0l",
			     new Temp[] { r },
			     new Temp[] { regFileInfo.SP() });
	    InstrMEM load2 = 
		new InstrMEM(template.getFactory(), template,
			     "ld [`s0 + " + (-4*(offset+1)) + "], `d0h",
			     new Temp[] { r },
			     new Temp[] { regFileInfo.SP() });
	    load2.layout(load1, null);
	    return Arrays.asList(new InstrMEM[] { load1, load2 });
	} else {
	    InstrMEM load = 
		new InstrMEM(template.getFactory(), template,
			     "ld [`s0 + " + (-4*offset) + "], `d0",
			     new Temp[] { r },
			     new Temp[] { regFileInfo.SP() });
	    return Arrays.asList(new InstrMEM[] { load });
	}
    }

    public List makeStore(Temp r, int offset, Instr template) {
	if (tempBuilder.isTwoWord(r)) {
	    // again, should consolidate this to std if possible
	    InstrMEM store1 = 
		new InstrMEM(template.getFactory(), template,
			     "st `s0l, [`s1 + " + (-4*offset) + "]",
			     new Temp[] { },
			     new Temp[] { r, regFileInfo.SP() });
	    InstrMEM store2 = 
		new InstrMEM(template.getFactory(), template,
			     "st `s0h, [`s1 + " + (-4*(offset+1)) + "]",
			     new Temp[] { },
			     new Temp[] { r, regFileInfo.SP() });  
	    store2.layout(store1, null);
	    return Arrays.asList(new InstrMEM[] { store1, store2 });
	} else {
	    InstrMEM store =
		new InstrMEM(template.getFactory(), template,
			     "st `s0, [`s1 + " + (-4*offset) + "]",
			     new Temp[] { },
			     new Temp[] { r, regFileInfo.SP() });
	    return Arrays.asList(new InstrMEM[] { store });
	}
    }	 

    public InstrLABEL makeLabel(Instr template) {
	Label l = new Label();
	InstrLABEL il = new InstrLABEL(template.getFactory(), template,
				       l.toString() + ":", l);
	return il;
    }
}
