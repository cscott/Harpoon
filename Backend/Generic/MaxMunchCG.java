// MaxMunchCG.java, created Fri Feb 11 01:26:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HClass;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Analysis.Maps.Derivation;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>MaxMunchCG</code> is a <code>MaximalMunchCGG</code> specific 
 * extension of <code>CodeGen</code>.  Its purpose is to incorporate
 * functionality common to all target architectures but specific to
 * the particular code generation strategy employed by the CGG.  Other
 * <code>CodeGeneratorGenerator</code> implementations should add
 * their own extensions of <code>CodeGen</code>.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: MaxMunchCG.java,v 1.1.2.3 2000-02-17 02:01:25 pnkfelix Exp $ */
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

    // tXi -> TypeAndDerivation
    private Map ti2td = new HashMap();

    // stores type information for Temps
    protected TypeState TYPE_STATE = new TypeState();
    
    static public class TypeState {
	Map tempToType = new HashMap();
	
	public void declare(Temp t, HClass clz) {
	    // System.out.println(t + " " + clz);
	    tempToType.put(t, new TypeAndDerivation(clz));
	}

	public void declare(Temp t, Derivation.DList dl) {
	    // System.out.println(t + " " + dl);
	    tempToType.put(t, new TypeAndDerivation(dl));
	}
    }

    // union type for Derivation.DList and HClass
    static class TypeAndDerivation {
	Derivation.DList dlist;
	HClass type;
	TypeAndDerivation(HClass hc) {
	    type = hc;
	}
	TypeAndDerivation(Derivation.DList dl) {
	    dlist = dl;
	}
    }

    
}
