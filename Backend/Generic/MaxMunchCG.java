// MaxMunchCG.java, created Fri Feb 11 01:26:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HClass;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Analysis.Maps.Derivation;

/**
 * <code>MaxMunchCG</code> is a <code>MaximalMunchCGG</code> specific 
 * extension of <code>CodeGen</code>.  Its purpose is to incorporate
 * functionality common to all target architectures but specific to
 * the particular code generation strategy employed by the CGG.  Other
 * <code>CodeGeneratorGenerator</code> implementations should add
 * their own extensions of <code>CodeGen</code>.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: MaxMunchCG.java,v 1.1.2.2 2000-02-15 23:35:20 pnkfelix Exp $ */
public abstract class MaxMunchCG extends CodeGen {
    
    /** Creates a <code>MaxMunchCG</code>. */
    public MaxMunchCG(Frame frame) {
        super(frame);
    }

    // first = null OR first instr passed to emit(Instr)
    protected Instr first;

    // last = null OR last instr passed to emit(Instr)
    protected Instr last; 
    
    /** Emits <code>i</code> as the next instruction in the
        instruction stream.
    */	
    protected Instr emit(Instr i) {
	debug( "Emitting "+i.toString() );
	if (first == null) {
	    first = i;
	}
	// its correct that last==null the first time this is called
	i.layout(last, null);
	last = i;
	return i;
    }

    // stores type information for Temps
    protected TypeState TYPE_STATE = new TypeState();
    
    static public class TypeState {
	public void declare(Temp t, HClass clz) {

	}

	public void declare(Temp t, Derivation.DList dl) {

	}
    }

    
}
