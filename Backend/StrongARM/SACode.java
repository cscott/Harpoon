// SACode.java, created Fri Jul 30 13:41:35 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Assem.Instr;
import harpoon.Analysis.Instr.TempInstrPair;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Temp.Temp;


import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>SACode</code> is a code-view for StrongARM assembly.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SACode.java,v 1.1.2.14 1999-08-04 21:46:11 pnkfelix Exp $
 */
public class SACode extends harpoon.Backend.Generic.Code {
    public static final String codename = "strongarm";

    Map tempInstrPairToRegisterMap;

    /** Creates a <code>SACode</code>. */
    public SACode(harpoon.IR.Tree.Code treeCode) {
        super(treeCode.getMethod(), 
	      null,
	      treeCode.getFrame());
	// treeCode.print(new java.io.PrintWriter(System.out));
	instrs = treeCode.getFrame().codegen()
	    .gen(treeCode, newINF(treeCode.getMethod()));
	Util.assert(instrs != null);
	instrs=treeCode.getFrame()
	    .procAssemDirectives(treeCode.getFrame()
				 .procLiveOnExit(instrs));
	tempInstrPairToRegisterMap = new HashMap();
    }

    public String getName() { return codename; }

    public HCode clone(HMethod m) throws CloneNotSupportedException {
	throw new CloneNotSupportedException(this.toString());
    }
    
    /**
     * Returns a code factory for <code>SACode</code>, given a 
     * code factory for <code>CanonicalTreeCode</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>CanonicalTreeCode</code>, then creates and returns a code
     *      factory for <code>SACode</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>CanonicalTreeCode.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>SACode</code> from the
     *      code factory returned by <code>CanonicalTreeCode</code>.
     * @see CanonicalTreeCode#codeFactory(HCodeFactory, Frame)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if(hcf.getCodeName().equals(CanonicalTreeCode.codename)){
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    harpoon.IR.Tree.Code tc = 
			(harpoon.IR.Tree.Code) hcf.convert(m);
		    return (tc == null) ? null : new SACode(tc);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    return codeFactory(CanonicalTreeCode.codeFactory(hcf, new SAFrame()));
	}
    }
    
    public static HCodeFactory codeFactory() {
	return codeFactory(CanonicalTreeCode.codeFactory( new SAFrame() ));
    }
    
    private Temp get(Instr instr, Temp val) {
	return (Temp) tempInstrPairToRegisterMap.get(new TempInstrPair(instr, val)); 
    }

    protected String getRegisterName(final Instr instr,
				     final Temp val, 
				     final String suffix) {
	class RegFinder extends SATempVisitor {
	    String s;
	    public void visit(Temp t) {
		// single word...nothing special
		Temp reg = get(instr, t);
		Util.assert(reg != null, "Should be a mapping for " + instr +
			    " x " + val + " but no register was found." );
		s = reg.name() + suffix;
	    }
	    public void visit(TwoWordTemp t) {
		// parse suffix
		Temp reg = null;
		if (suffix.startsWith("l")) {
		    // low
		    reg = get(instr, t.getLow());
		} else if (suffix.startsWith("h")) {
		    // high
		    reg = get(instr, t.getHigh());
		} else {
		    Util.assert(false, "BREAK!  This parsing needs to be "+
				"fixed, strongarm has a lot more cases than this.");
		}
		Util.assert(reg != null, "Should be a mapping for " + instr +
			    " x " + val + " but no register was found." );
		s = reg.name() + suffix.substring(1);
	    }
	}
	RegFinder finder = new RegFinder();
	// fix Visitor mess
	return finder.s;
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
	
	SATempVisitor visitor = new SATempVisitor(){
	    public void visit(Temp t) {
		if (t.equals(pseudoReg)) {
		    tempInstrPairToRegisterMap.put
		       (new TempInstrPair(instr, t), regs.get(0));
		}
	    }
	    public void visit(TwoWordTemp t) {
		if (t.equals(pseudoReg)) {
		    tempInstrPairToRegisterMap.put
		       (new TempInstrPair(instr, t.getLow()), regs.get(0));
		    tempInstrPairToRegisterMap.put
		       (new TempInstrPair(instr, t.getHigh()), regs.get(1));
		}
	    }
	};
	// fix Visitor mess
    }
}
