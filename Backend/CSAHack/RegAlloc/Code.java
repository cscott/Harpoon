// Code.java, created Wed Oct 20 12:58:47 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.StrongARM.TwoWordTemp;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * <code>Code</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.2 1999-11-06 00:36:19 cananian Exp $
 */
public class Code extends harpoon.Backend.Generic.Code {
    TempMap tm;
    
    /** Creates a <code>Code</code>. */
    public Code(final HMethod parent, final Instr instrs, final Frame frame)
    {
        super(parent, null, frame);
	// XXX: should clone instrs here.
	RegAlloc ra = new RegAlloc(frame, this, instrs);
	this.tm = ra;
	this.instrs = frame.getCodeGen().procFixup(parent, instrs, locals,
						   computeUsedRegs(instrs));
    }
    private Set computeUsedRegs(Instr instrs) {
	Set s = new HashSet();
	for (Instr il = instrs; il!=null; il=il.getNext()) {
	    Temp[] d = il.def();
	    for (int i=0; i<d.length; i++)
		if (d[i] instanceof TwoWordTemp) {
		    s.add(tm.tempMap(((TwoWordTemp)d[i]).getLow()));
		    s.add(tm.tempMap(((TwoWordTemp)d[i]).getHigh()));
		} else s.add(tm.tempMap(d[i]));
	}
	return Collections.unmodifiableSet(s);
    }

    public HCode clone(HMethod newMethod) {throw new Error("not supported.");}
    public String getName() { return "hacked-regalloc"; }
    protected String getRegisterName(Instr i, Temp val, String suffix) {
	if (val instanceof TwoWordTemp) {
	    if (suffix.equals("l"))
		return tm.tempMap(((TwoWordTemp)val).getLow()).toString();
	    if (suffix.equals("h"))
		return tm.tempMap(((TwoWordTemp)val).getHigh()).toString();
	    throw new Error("Unknown suffix: \""+suffix+"\" in "+i);
	}
	return tm.tempMap(val).toString();
    }
    public void assignRegister(Instr i, Temp pr, List regs) {
	throw new Error("unimplemented");
    }

    Access allocLocal() {
	final Temp SP = frame.getRegFileInfo().getRegister(13);
	final int n = locals++;
	return new Access() {
	    public Instr makeLoad(InstrFactory inf, HCodeElement source,
				  Temp dst) {
		return new InstrMEM(inf, source,
				    "ldr `d0, [`s0, #"+(n*4)+"] @ spill",
				    new Temp[] { dst }, new Temp[]{ SP });
	    }
	    public Instr makeStore(InstrFactory inf, HCodeElement source,
				   Temp src) {
		return new InstrMEM(inf, source,
				    "str `s0, [`s1, #"+(n*4)+"] @ spill",
				    null, new Temp[]{ src, SP });
	    }
	};
    }
    private int locals=0;

    abstract class Access {
	public abstract Instr makeLoad(InstrFactory inf, HCodeElement source,
				       Temp dst);
	public abstract Instr makeStore(InstrFactory inf, HCodeElement source,
					Temp src);
    }
}
