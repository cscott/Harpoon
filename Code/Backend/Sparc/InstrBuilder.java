// InstrBuilder.java, created Tue Nov  2  2:07:04 1999 by andyb
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
 * @version $Id: InstrBuilder.java,v 1.2 2002-02-25 21:02:37 cananian Exp $
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
            InstrMEM load = 
                new InstrMEM(template.getFactory(), template,
                             "ldd [`s0 + " +(-4*(offset+1)) + "], `d0h",
                             new Temp[] { r },
                             new Temp[] { regFileInfo.SP() });
	    return Arrays.asList(new InstrMEM[] { load, load });
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
	    InstrMEM store = 
		new InstrMEM(template.getFactory(), template,
			     "std `s0h, [`s1 + " + (-4*(offset+1)) + "]",
			     null, new Temp[] { r, regFileInfo.SP() });
	    return Arrays.asList(new InstrMEM[] { store });
	} else {
	    InstrMEM store =
		new InstrMEM(template.getFactory(), template,
			     "st `s0, [`s1 + " + (-4*offset) + "]",
			     null, new Temp[] { r, regFileInfo.SP() });
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
