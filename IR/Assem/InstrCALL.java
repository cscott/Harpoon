// InstrCALL.java, created Wed Jan 26 10:56:44 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import java.util.List;

/**
 * <code>InstrCALL</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrCALL.java,v 1.3 2003-06-10 15:07:13 cananian Exp $
 */
public class InstrCALL extends Instr {
    
    /** Creates a <code>InstrCALL</code>. */
    public InstrCALL( InstrFactory inf, HCodeElement source,
		      String assem, Temp[] dst, Temp[] src,
		      boolean canFallThrough, List<Label> targets) {
        super(inf, source, assem, dst, src, canFallThrough, targets);
    }

    /** Accept a visitor */
    public void accept(InstrVisitor v) { v.visit(this); }

    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	// should clone label or something.
	return new InstrCALL(getFactory(), this, getAssem(), 
			     map(defMap,def()), map(useMap, use()),
			     this.canFallThrough, getTargets());
    }
    public Instr cloneMutateAssem(InstrFactory inf, String newAssem) {
	return new InstrCALL(inf, this, newAssem, def(), use(),
			     canFallThrough, getTargets());
    }
    
}
