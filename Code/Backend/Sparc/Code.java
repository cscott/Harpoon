// Code.java, created Tue Nov  2  2:07:03 1999 by andyb
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

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>Code</code> is a code-view for SPARC assembly.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.4 2002-04-10 03:03:51 cananian Exp $
 */
class Code extends harpoon.Backend.Generic.Code {
    public static final String codename = "sparc";

    private Map tempInstrPairToRegisterMap;

    public Code(harpoon.IR.Tree.Code treeCode) {
	super(treeCode);

	tempInstrPairToRegisterMap = new HashMap();
    }

    public String getName() { return codename; }

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

    public String getRegisterName(Instr i, Temp val, String suffix) { 
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	Temp reg = null;
	String s = null;

	if (tb.isTwoWord(val)) {
	    if (suffix.startsWith("l")) {
		reg = get(i, tb.getLow(val));
	    } else if (suffix.startsWith("h")) {
		reg = get(i, tb.getHigh(val));
	    } else if (suffix.trim().equals("")) {
		assert false : ("BREAK!  empty suffix \n " +
			    "suffix: " + suffix + "\n" +
			    "instr: " + i + "\n" + 
			    "instr str: " + i.getAssem() + "\n"+
			    "temp: " + val);
	    } else {
		assert false : ("BREAK! Unknown suffix \n" +
                            "suffix: " + suffix + "\n" +
                            "instr: " + i + "\n" + 
                            "instr str: " + i.getAssem() + "\n"+
                            "temp: " + val);
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

    public List getRegisters(Instr i, Temp val) {
	assert i != null : "Instr was null in Code.getRegisters";
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	if (tb.isTwoWord(val)) {
	    Temp low = get(i, tb.getLow(val));
	    Temp high = get(i, tb.getHigh(val));
	    assert low != null : "low reg for "+val+" in "+i+" was null";
	    assert high != null : "high reg for "+val+" in "+i+" was null";
	    return Arrays.asList(new Temp[] { low, high });
	} else {
	    Temp t = get(i, val);
	    assert t != null : "register for "+val+" in "+i+" was null";
	    return Collections.nCopies(1, t);
	}
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

    public boolean registerAssigned(Instr i, Temp pr) {
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	Set keys = tempInstrPairToRegisterMap.keySet();
	if (tb.isTwoWord(pr)) {
	    TempInstrPair pair1 = new TempInstrPair(i, tb.getLow(pr));
	    TempInstrPair pair2 = new TempInstrPair(i, tb.getHigh(pr));
	    return (keys.contains(pair1) && keys.contains(pair2));
	} else {
	    return (keys.contains(new TempInstrPair(i, pr)));
	}
    }

    public void removeAssignment(Instr i, Temp pseudoReg) {
	TempBuilder tb = (TempBuilder) frame.getTempBuilder();
	if (tb.isTwoWord(pseudoReg)) {
	    tempInstrPairToRegisterMap.remove(
		new TempInstrPair(i, tb.getLow(pseudoReg)));
	    tempInstrPairToRegisterMap.remove(
		new TempInstrPair(i, tb.getHigh(pseudoReg)));
	} else {
	    tempInstrPairToRegisterMap.remove(
		new TempInstrPair(i, pseudoReg));
	}
    }
}
