// Code.java, created Fri Jul 30 13:41:35 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Assem.Instr;
import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Temp.Temp;


import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>Code</code> is a code-view for StrongARM assembly.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Code.java,v 1.1.2.10 1999-11-16 22:08:50 pnkfelix Exp $
 */
public class Code extends harpoon.Backend.Generic.Code {
    public static final String codename = "strongarm";

    Map tempInstrPairToRegisterMap;

    /** Creates a <code>Code</code>. */
    public Code(harpoon.IR.Tree.Code treeCode) {
        super(treeCode.getMethod(), 
	      null,
	      treeCode.getFrame());
	// treeCode.print(new java.io.PrintWriter(System.out));
	instrs = treeCode.getFrame().getCodeGen()
	    .gen(treeCode, newINF(treeCode.getMethod()));
	Util.assert(instrs != null);
	tempInstrPairToRegisterMap = new HashMap();
    }

    public String getName() { return codename; }

    /** @exception CloneNotSupportedException <code>clone()</code> is not
     *             implemented. */
    public HCode clone(HMethod m) throws CloneNotSupportedException {
	throw new CloneNotSupportedException(this.toString());
    }
    
    /**
     * Returns a code factory for <code>Code</code>, given a 
     * code factory for <code>CanonicalTreeCode</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>CanonicalTreeCode</code>, then creates and returns a code
     *      factory for <code>Code</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>CanonicalTreeCode.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>Code</code> from the
     *      code factory returned by <code>CanonicalTreeCode</code>.
     * @see CanonicalTreeCode#codeFactory(HCodeFactory, Frame)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) {
	if(hcf.getCodeName().equals(CanonicalTreeCode.codename)){
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    harpoon.IR.Tree.Code tc = 
			(harpoon.IR.Tree.Code) hcf.convert(m);
		    return (tc == null) ? null : new Code(tc);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    // recursion can be ugly some times.
	    return codeFactory(CanonicalTreeCode.
			       codeFactory(hcf, frame), frame);
	}
    }
    
    /* THIS NEEDS RETHINKING
    public static HCodeFactory codeFactory(final HCodeFactory hcf,
					   final OffsetMap offmap) {
	return codeFactory(hcf, new SAFrame(offmap));
    }

    public static HCodeFactory codeFactory(OffsetMap offmap) {
	return codeFactory(CanonicalTreeCode.
			   codeFactory( new SAFrame(offmap) ), offmap);
    }
    */
    
    private Temp get(Instr instr, Temp val) {
	return (Temp) tempInstrPairToRegisterMap.get(new TempInstrPair(instr, val)); 
    }

    
    protected String getRegisterName(final Instr instr,
				     final Temp val, 
				     final String suffix) {
	String s=null;
	if (val instanceof TwoWordTemp) {
	    // parse suffix
	    TwoWordTemp t = (TwoWordTemp) val;
	    Temp reg = null;
	    if (suffix.startsWith("l")) {
		// low
		reg = get(instr, t.getLow());
	    } else if (suffix.startsWith("h")) {
		// high
		reg = get(instr, t.getHigh());
	    } else if (suffix.trim().equals("")) {
		Util.assert(false, "BREAK!  empty suffix " +
			    "suffix: " + suffix + "\n" +
			    "instr: " + instr + "\n" + 
			    "instr str: " + instr.assem + "\n"+
			    "temp: " + val);
	    } else {
		Util.assert(false, "BREAK!  This parsing needs to be "+
			    "fixed, strongarm has a lot more cases than this."+
			    "\n suffix: "+ suffix + "\n" +
			    "Alternatively, the pattern could be trying to "+
			    "use a TwoWordTemp without the appropriate "+
			    "double word modifier (l, h) in " + instr);
	    }
	    if(reg != null) {
		s = reg.name() + suffix.substring(1);
	    } else {
		s = val.name() + suffix;
	    }
	} else { // single word; nothing special
	    Temp reg = get(instr, val);
	    
	    Util.assert(!suffix.startsWith("l") &&
			!suffix.startsWith("h"), "Shouldn't " +
			"have 'l' or 'h' suffix with Temp: " + 
			val + " Instrs: " + 
			instr.getPrev() + ", " + 
			instr + ", " + 
			instr.getNext());

	    if(reg != null) {
		s = reg.name() + suffix;
	    } else {
		s = val.name() + suffix;
	    }
	}
	// Util.assert(s.indexOf("r0l") == -1 && s.indexOf("r0h") == -1 &&
	// s.indexOf("r1l") == -1 && s.indexOf("r1h") == -1, 
	// "Improper parsing of " + suffix + " in " + instr + " " + val.getClass().getName());

	return s;
    }

    /** Assigns register(s) to the <code>Temp</code> pseudoReg. 
	<BR><B>requires:</B> <code>regs</code> is one of the
	    <code>List</code>s returned by
	    <code>SAFrame.suggestRegAssignment()</code> for
	    <code>pseudoReg</code>.
	<BR><B>effects:</B> Associates the register <code>Temp</code>s
	    in <code>regs</code> with (<code>instr</code> x
	    <code>pseudoReg</code>) in such a manner that
	    <code>getRegisterName(instr, psuedoReg, suffix)</code>
	    will return the name associated with the appropriate
	    register in <code>regs</code>.  
    */
    public void assignRegister(final Instr instr, 
			       final Temp pseudoReg,
			       final List regs) {
	if (pseudoReg instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) pseudoReg;
	    tempInstrPairToRegisterMap.put
		(new TempInstrPair(instr, t.getLow()), regs.get(0));
	    tempInstrPairToRegisterMap.put
		(new TempInstrPair(instr, t.getHigh()), regs.get(1));
	} else {
	    tempInstrPairToRegisterMap.put
		(new TempInstrPair(instr, pseudoReg), regs.get(0));
	}
    }

    public boolean registerAssigned(Instr instr, Temp pr) {
	if (pr instanceof TwoWordTemp) {
	    TwoWordTemp t = (TwoWordTemp) pr;
	    return 
		(tempInstrPairToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, t.getLow()))
		 &&
		 tempInstrPairToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, t.getHigh())));
	} else {
	    return 
		(tempInstrPairToRegisterMap.
		 keySet().contains
		 (new TempInstrPair(instr, pr)));
	}
    }
}
