// InstrMEM.java, created Mon Apr  5 17:27:57 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

import java.util.List;

/**
 * <code>InstrMEM</code> is used to represent memory operations in
 * assembly-level instruction representations.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrMEM.java,v 1.2 2002-02-25 21:04:13 cananian Exp $ 
 */
public class InstrMEM extends Instr {
    
    /** Creates a <code>InstrMEM</code>. */
    public InstrMEM(InstrFactory inf, HCodeElement codeSrc, String assem, 
		    Temp[] dst, Temp[] tempSrc, boolean canFallThrough,
		    List targets) {
        super(inf, codeSrc, assem, dst, tempSrc, canFallThrough, targets);
    }

    /** Creates a <code>InstrMEM</code> with default values for
	<code>canFallThrough</code> and <code>targets</code>. */
    public InstrMEM(InstrFactory inf, HCodeElement codeSrc, String assem, 
		    Temp[] dst, Temp[] tempSrc) {
        super(inf, codeSrc, assem, dst, tempSrc);
    }

    /** Accept a visitor */
    public void accept(InstrVisitor v) { v.visit(this); }    

    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	return new InstrMEM(inf, this, getAssem(),
			    map(defMap,def()), map(useMap,use()),
			    this.canFallThrough, getTargets());
    }

    public Instr cloneMutateAssem(InstrFactory inf, String newAssem) {
	return new InstrMEM(inf, this, newAssem, def(), use());
    }
}
