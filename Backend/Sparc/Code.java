// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.String;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>Code</code> is a code-view for SPARC assembly.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.3 1999-11-04 01:27:02 andyb Exp $
 */
public class Code extends harpoon.Backend.Generic.Code {
    public static final String codename = "sparc";

    private Map tempInstrPairToRegisterMap;

    public Code(harpoon.IR.Tree.Code treeCode) {
	super(treeCode.getMethod(), null, treeCode.getFrame());

	instrs = treeCode.getFrame().getCodeGen()
	    .gen(treeCode, newINF(treeCode.getMethod()));

	Util.assert(instrs != null);
	tempInstrPairToRegisterMap = new HashMap();
    }

    public String getName() { return codename; }

    public HCode clone(HMethod m) throws CloneNotSupportedException { 
	throw new CloneNotSupportedException(this.toString());
    }

    public static HCodeFactory codeFactory(final HCodeFactory prevhcf,
					   final Frame frame) {
	if (prevhcf.getCodeName().equals(CanonicalTreeCode.codename)) {
	    return new HCodeFactory() {
		public HCode convert(HMethod m) {
		    harpoon.IR.Tree.Code tc =
			(harpoon.IR.Tree.Code) prevhcf.convert(m);
		    return (tc == null) ? null : new Code(tc);
		}
		public void clear(HMethod m) { prevhcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    return codeFactory(CanonicalTreeCode.codeFactory(prevhcf, frame),
			       frame);
	}
    }

    private Temp get(Instr i, Temp val) {
	return (Temp) tempInstrPairToRegisterMap.get(
	    new TempInstrPair(i, val));
    }

    protected String getRegisterName(Instr i, Temp val, String suffix) { 
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	Temp reg = null;
	String s = null;

	if (tb.isTwoWord(val)) {
	    if (suffix.startsWith("l")) {
		reg = get(i, tb.getLow(val));
	    } else if (suffix.startsWith("h")) {
		reg = get(i, tb.getHigh(val));
	    } else if (suffix.trim().equals("")) {
		Util.assert(false, "BREAK! empty suffix: " + suffix);
	    } else {
		Util.assert(false, "BREAK! AAA - what to do here");
	    }
	    if (reg != null) {
		s = reg.name() + suffix.substring(1);
	    } else {
		s = val.name() + suffix;
	    }
	} else {
	    reg = get(i, val);
 
	    if (reg != null) {
		s = reg.name() + suffix;
	    } else {
		s = val.name() + suffix;
	    }
	}
	return s;
    }

    public void assignRegister(Instr i, Temp pseudoReg, List regs) { 
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	if (tb.isTwoWord(pseudoReg)) {
	    tempInstrPairToRegisterMap.put(
		new TempInstrPair(i, tb.getLow(pseudoReg)),
		regs.get(0));
	    tempInstrPairToRegisterMap.put(
		new TempInstrPair(i, tb.getHigh(pseudoReg)),
		regs.get(1));
	} else {
	    tempInstrPairToRegisterMap.put(
		new TempInstrPair(i, pseudoReg), 
		regs.get(0));
	}
    }
}
