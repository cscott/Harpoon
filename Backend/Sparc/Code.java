// Code.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
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
 * @version $Id: Code.java,v 1.1.2.2 1999-11-02 22:09:01 andyb Exp $
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

    /* AAA - to implement */
    public HCodeElement getRootElement() { return null; }

    /* AAA - to implement */
    public HCodeElement[] getLeafElements() { return null; }

    /* AAA - to implement, maybe? */
    public HCode clone(HMethod m) throws CloneNotSupportedException { 
	throw new CloneNotSupportedException(this.toString());
    }

    /* AAA - to implement */
    public static HCodeFactory codeFactory(final HCodeFactory hcf,
					   final Frame frame) {

        return null;
    }

    /* AAA - to implement */
    protected String getRegisterName(Instr i, Temp val, String suffix) { 
        if (((TempBuilder)frame.getTempBuilder()).isTwoWord(val)) {
        } else {
        }
        return null;
    }

    /* AAA - to implement */
    public void assignRegister(Instr i, Temp pseudoReg, List regs) { 
	if (((TempBuilder)frame.getTempBuilder()).isTwoWord(pseudoReg)) {
        } else {
        }
    }
}
