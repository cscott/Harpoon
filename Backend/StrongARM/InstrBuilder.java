// InstrBuilder.java, created Fri Sep 10 23:37:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;

import java.util.List;
import java.util.Arrays;

/** <code>StrongARM.InstrBuilder</code> is an <code>Generic.InstrBuilder</code> for the
    StrongARM architecture.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InstrBuilder.java,v 1.1.2.1 1999-09-20 15:29:40 pnkfelix Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {
    RegFileInfo rfInfo;

    /* helper macro. */
    private final Temp SP() { 
	return rfInfo.SP;
    }
    
    InstrBuilder(RegFileInfo rfInfo) {
	super();
	this.rfInfo = rfInfo;
    }

    // TODO: override makeStore/Load(List, int, Instr) to take
    // advantage of StrongARM's multi-register memory operations.   

    public List makeLoad(Temp r, int offset, Instr template) {
	if (r instanceof TwoWordTemp) {
	    InstrMEM load1 = 
		new InstrMEM(template.getFactory(), template,
			     "ldr `d0l, [`s0, #" +(-4*offset) + "] " ,
			     new Temp[]{ r },
			     new Temp[]{ SP()  });
	    InstrMEM load2 = 
		new InstrMEM(template.getFactory(), template,
			     "ldr `d0h, [`s0, #" +(-4*(offset+1)) + "] ",
			     new Temp[]{ r },
			     new Temp[]{ SP()  });
	    load2.layout(load1, null);
	    return Arrays.asList(new InstrMEM[] { load1, load2 });
	} else {
	    InstrMEM load = 
		new InstrMEM(template.getFactory(), template,
			     "ldr `d0, [`s0, #" +(-4*offset) + "] ",
			     new Temp[]{ r },
			     new Temp[]{ SP()  });
	    return Arrays.asList(new InstrMEM[] { load });
	}
				     
    }

    public List makeStore(Temp r, int offset, Instr template) {
	if (r instanceof TwoWordTemp ) {
	    InstrMEM store1 = 
		new InstrMEM(template.getFactory(), template,
			     "str `s0l, [`s1, #" +(-4*offset) + "] ",
			     new Temp[]{ },
			     new Temp[]{ r , SP() });
	    InstrMEM store2 = 
		new InstrMEM(template.getFactory(), template,
			     "str `s0h, [`s1, #" +(-4*(offset+1)) + "] ",
			     new Temp[]{ },
			     new Temp[]{ r , SP() });
	    store2.layout(store1, null);
	    return Arrays.asList(new InstrMEM[]{ store1, store2 });
	} else {
	    InstrMEM store = 
		new InstrMEM(template.getFactory(), template,
			     "str `s0, [`s1, #" +(-4*offset) + "] ",
			     new Temp[]{ },
			     new Temp[]{ r , SP() });
	    return Arrays.asList(new InstrMEM[] { store });
	}
    }

    public InstrLABEL makeLabel(Instr template) {
	Label l = new Label();
	InstrLABEL il = new InstrLABEL(template.getFactory(), 
				       template,
				       l.toString() + ":", l);
	return il;
    }
}
