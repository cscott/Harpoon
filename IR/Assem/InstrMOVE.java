// InstrMOVE.java, created Mon Aug  2 23:19:01 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import harpoon.Util.Util;

/**
 * <code>InstrMOVE</code> represents a copying of a set of source
 * <code>Temp</code>s to a set of destination <code>Temp</code>s.
 * This instruction is being specialized to allow for easier detection
 * of MOVEs which could guide optimizations (either in eliminating the
 * MOVE in question or in choosing which register a given
 * <code>Temp</code> would be best assigned to.
 * 
 * Note that <code>InstrMOVE</code>s at the lowest level represents
 * the movement of data from register to register, <B>not</B> to
 * memory (use <code>InstrMEM</code> for that).  However, prior to
 * register allocation it is legal for <code>InstrMOVE</code>s to have
 * non-register <code>Temp</code>s as their source or destination; the
 * instruction will simply be replaced later in the compilation with
 * either a new backend legal <code>InstrMOVE</code> or
 * <code>InstrMEM</code>.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrMOVE.java,v 1.3.2.1 2002-02-27 08:35:56 cananian Exp $ 
 */
public class InstrMOVE extends Instr {
    
    /** Creates a <code>InstrMOVE</code>. */
    public InstrMOVE(InstrFactory inf, HCodeElement codeSrc, 
		     String assem, Temp[] dst, Temp[] tempSrc) {
        super(inf, codeSrc, assem, dst, tempSrc);
	assert dst.length == 1 : "can only have one dest";
	assert tempSrc.length == 1 : "can only have one src";
    }
    
    /** Accept a visitor */
    public void accept(InstrVisitor v) { v.visit(this); }

    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	return new InstrMOVE(inf, this, getAssem(),
			     map(defMap,def()), map(useMap,use()));
    }

    public Instr cloneMutateAssem(InstrFactory inf, String newAssem) {
	return new InstrMOVE(inf, this, newAssem, def(), use());
    }

    public boolean isMove() { return true; }
}
