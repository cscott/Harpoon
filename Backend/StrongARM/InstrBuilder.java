// InstrBuilder.java, created Fri Sep 10 23:37:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Util.ArrayIterator;

import java.util.List;
import java.util.Iterator;
import java.util.Arrays;

/** <code>StrongARM.InstrBuilder</code> is an <code>Generic.InstrBuilder</code> for the
    StrongARM architecture.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InstrBuilder.java,v 1.1.2.2 1999-12-01 17:12:40 pnkfelix Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {

    private static final int OFFSET_LIMIT = 1024;

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

    public int getSize(Temp t) {
	if (t instanceof TwoWordTemp) {
	    return 2; 
	} else {
	    return 1;
	}
    }

    // note to self; add code to generate multi-instruction code for
    // loading Temps that cross the OFFSET_LIMIT; instead of using a
    // Constant Offset, just manually increment and decrement SP (for
    // Loads, can actually increment the target-register so that 

    public List makeLoad(Temp r, int offset, Instr template) {
	// Util.assert(offset < OFFSET_LIMIT, 
	// 	       "offset " + offset + " is too large");

	if (offset < OFFSET_LIMIT) { // common case
	    String[] strs = getLoadAssemStrs(r, offset);
	    Util.assert(strs.length == 1 ||
			strs.length == 2 );

	    if (strs.length == 2) {
		InstrMEM load1 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ r },
				 new Temp[]{ SP()  });
		InstrMEM load2 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[1],
				 new Temp[]{ r },
				 new Temp[]{ SP()  });
		load2.layout(load1, null);
		return Arrays.asList(new InstrMEM[] { load1, load2 });
	    } else {
		InstrMEM load = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ r },
				 new Temp[]{ SP()  });
		return Arrays.asList(new InstrMEM[] { load });
	    }
	} else {
	    // need to wrap load with instructions to shift SP down
	    // and up again, and need to make it *ONE* Instr so that
	    // they do not get seperated.  Also, note that this is
	    // safe since StrongARM does not seem to have normal
	    // interrupts 
	    
	    String assem = "";
	    int numSPdec = 0;
	    while (offset >= OFFSET_LIMIT) {
		numSPdec++;
		assem += "sub `s0, `s0, #"+(OFFSET_LIMIT*4) +"\n";
		offset -= OFFSET_LIMIT;
	    }

	    Iterator strs = new ArrayIterator(getLoadAssemStrs(r, offset));
	    while(strs.hasNext()) {
		assem += (String)strs.next() +"\n";
	    }
	    
	    while(numSPdec > 0) {
		numSPdec--;
		assem += "add `s0, `s0, #"+(OFFSET_LIMIT*4);
		if (numSPdec > 0) assem += "\n";
	    }
	    
	    return Arrays.asList
		(new InstrMEM[] 
		 { new InstrMEM(template.getFactory(), template,
				assem, 
				new Temp[]{ r }, 
				new Temp[]{ SP() }) });
	}
    }

    private String[] getLoadAssemStrs(Temp r, int offset) {
	if (r instanceof TwoWordTemp) {
	    return new String[] {
		"ldr `d0l, [`s0, #" +(-4*offset) + "] " ,
		    "ldr `d0h, [`s0, #" +(-4*(offset+1)) + "] " };
	} else {
	    return new String[] { "ldr `d0, [`s0, #" +(-4*offset) + "] " };
	}
    }

    public List makeStore(Temp r, int offset, Instr template) {
	Util.assert(offset < OFFSET_LIMIT, "offset " + offset + " is too large");
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
