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
import java.util.Iterator;
import java.util.Arrays;

import net.cscott.jutil.Util;
/** <code>StrongARM.InstrBuilder</code> is an <code>Generic.InstrBuilder</code> for the
    StrongARM architecture.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InstrBuilder.java,v 1.5 2004-02-08 01:57:59 cananian Exp $
 */
public class InstrBuilder extends harpoon.Backend.Generic.InstrBuilder {

    // making offset limit a bit smaller than 1023 to allow for
    // .fpoffset margin
    private static final int OFFSET_LIMIT = 1000;

    RegFileInfo rfInfo;

    /* helper macro. */
    private final Temp FP() { 
	return rfInfo.FP;
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
    // Constant Offset, just manually increment and decrement FP (for
    // Loads, can actually increment the target-register so that 

    public List makeLoad(Temp r, int offset, Instr template) {
	// assert offset < OFFSET_LIMIT : // 	       "offset " + offset + " is too large";

	if (offset < OFFSET_LIMIT) { // common case
	    String[] strs = getLdrAssemStrs(r, offset);
	    assert strs.length == 1 ||
			strs.length == 2;

	    if (strs.length == 2) {
		InstrMEM load1 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ r },
				 new Temp[]{ FP()  });
		InstrMEM load2 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[1],
				 new Temp[]{ r },
				 new Temp[]{ FP()  });
		load2.layout(load1, null);
		return Arrays.asList(new InstrMEM[] { load1, load2 });
	    } else {
		InstrMEM load = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ r },
				 new Temp[]{ FP()  });
		return Arrays.asList(new InstrMEM[] { load });
	    }
	} else {
	    // System.out.println("Offset exceeded!");

	    // need to wrap load with instructions to shift FP down
	    // and up again, and need to make it *ONE* Instr so that
	    // they do not get seperated.  Also, note that this is
	    // safe since StrongARM does not seem to have normal
	    // interrupts 

	    int newOffset = offset;
	    while (newOffset >= OFFSET_LIMIT) {
		newOffset -= OFFSET_LIMIT;
	    }
	    String assem = 
		getWrappedAssem(getLdrAssemStrs(r, newOffset), 
				offset, "`s0"); 

	    return Arrays.asList
		(new InstrMEM[] 
		 { new InstrMEM(template.getFactory(), template,
				assem, 
				new Temp[]{ r }, 
				new Temp[]{ FP() }) });
	}
    }

    private String[] getLdrAssemStrs(Temp r, int offset) {
	if (r instanceof TwoWordTemp) {
	    return new String[] {
		"ldr `d0l, [`s0, #.fpoffset-" +(4*offset) + "] " ,
		    "ldr `d0h, [`s0, #.fpoffset-" +(4*(offset+1)) + "] @ restore2" };
	} else {
	    return new String[] { "ldr `d0, [`s0, #.fpoffset-" 
				      +(4*offset) + "] @ restore" };
	}
    }

    private String[] getStrAssemStrs(Temp r, int offset) {
	if (r instanceof TwoWordTemp) {
	    return new String[] {
		"str `s0l, [`s1, #.fpoffset-" +(4*offset) + "] " ,
		    "str `s0h, [`s1, #.fpoffset-" +(4*(offset+1)) + "] @ spill2" };
	} else {
	    return new String[] { "str `s0, [`s1, #.fpoffset-" 
				      +(4*offset) + "] @ spill" };
	}
    }

    public List makeStore(Temp r, int offset, Instr template) {
	// assert offset < OFFSET_LIMIT : //             "offset " + offset + " is too large";

	if (offset < OFFSET_LIMIT) { // common case
	    // assert harpoon.Backend.StrongARM.
	    //             Code.isValidConst( 4*offset ) : //		   "invalid offset: "+(4*offset);
	    String[] strs = getStrAssemStrs(r, offset);
	    assert strs.length == 1 || 
			strs.length == 2;
	    
	    if (strs.length == 2) {
		System.out.println("In makeStore, twoWord case");

		InstrMEM store1 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ },
				 new Temp[]{ r , FP() });
		InstrMEM store2 = 
		    new InstrMEM(template.getFactory(), template,
				 strs[1],
				 new Temp[]{ },
				 new Temp[]{ r , FP() });
		store2.layout(store1, null);
		assert store1.getNext() == store2 : "store1.next == store2";
		assert store2.getPrev() == store1 : "store2.prev == store1";
		return Arrays.asList(new InstrMEM[]{ store1, store2 });
	    } else {

		InstrMEM store = 
		    new InstrMEM(template.getFactory(), template,
				 strs[0],
				 new Temp[]{ },
				 new Temp[]{ r , FP() });
		return Arrays.asList(new InstrMEM[] { store });
	    }
	} else {
	    // need to wrap store with instructions to shift FP down
	    // and up again, and need to make it *ONE* Instr
	    
	    int newOffset = offset;
	    while (newOffset >= OFFSET_LIMIT) {
		newOffset -= OFFSET_LIMIT;
	    }
	    String assem = 
		getWrappedAssem(getStrAssemStrs(r, newOffset), 
				offset, "`s1");

	    return Arrays.asList
		(new InstrMEM[]
		 { new InstrMEM(template.getFactory(), template,
				assem,
				new Temp[] {},
				new Temp[] { r, FP() }) });
	}
    }

    private String getWrappedAssem(String[] strs, int offset, String spStr) {
	String assem = "";
	int numSPdec = 0;
	while (offset >= OFFSET_LIMIT) {
	    numSPdec++;
	    // can only do eight-bit chunks in 2nd Operand
	    int op2 = OFFSET_LIMIT*4;
	    while(op2 != 0) {
		// FSK: trusting CSA's code from CodeGen here...
		int eight = op2 & (0xFF << ((Util.ffs(op2)-1) & ~1));
		assem += "sub "+spStr+", "+spStr+", #"+eight+"\n";		
		op2 ^= eight;
	    }
	    offset -= OFFSET_LIMIT;
	}
	
	Iterator strIter = Arrays.asList(strs).iterator();
	while(strIter.hasNext()) { 
	    assem += (String)strIter.next() +"\n"; 
	}
	
	while(numSPdec > 0) {
	    numSPdec--;
	    int op2 = OFFSET_LIMIT*4;
	    while(op2 != 0) {
		// FSK: symmetric with above code (sort of)
		int eight = op2 & (0xFF << ((Util.ffs(op2)-1) & ~1));
		assem += "add "+spStr+", "+spStr+", #"+eight;
		op2 ^= eight;
		if (numSPdec > 0 || op2 != 0) assem += "\n";
	    }
	}
	
	return assem;
    }

    public InstrLABEL makeLabel(Instr template) {
	Label l = new Label();
	InstrLABEL il = new InstrLABEL(template.getFactory(), 
				       template,
				       l.toString() + ":", l);
	return il;
    }

    /** Returns a new <code>InstrLABEL</code> for generating new
	arbitrary code blocks to branch to.
	@param template An <code>Instr</code> to base the generated
	                <code>InstrLABEL</code>.
			<code>template</code> should be part of the
			instruction stream that the returned
			<code>InstrLABEL</code> is intended for. 
    */
    public InstrLABEL makeLabel(Label l, Instr template) {
	InstrLABEL il = new InstrLABEL(template.getFactory(), 
				       template,
				       l.toString() + ":", l);
	return il;
    }
}
