// MaxMunchCG.java, created Fri Feb 11 01:26:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.TEMP;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.TempFactory;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;

import harpoon.Util.Util;
import harpoon.Util.Default;

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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: MaxMunchCG.java,v 1.3 2002-02-26 22:43:45 cananian Exp $ */
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
    protected final Instr cgg_backendEmit(Instr i) {
	debug( "Emitting "+i.toString() );
	if (first == null) {
	    first = i;
	}
	// its correct that last==null the first time this is called
	i.layout(last, null);
	last = i;

	java.util.Iterator defs = i.defC().iterator();
	while(defs.hasNext()) {
	    Temp t = (Temp) defs.next();
	    TypeAndDerivation td = 
		(TypeAndDerivation) tempToType.get(t);
	    Util.ASSERT(td != null, 
			"Uh oh forgot to declare "+t+" before "+i);
	    ti2td.put(Default.pair(i, t), td);
	}

	return i;
    }

    /** tempmap from tree temps to instr temps */
    protected Temp makeTemp(TEMP t, TempFactory tf) {
	Temp treeTemp  = t.temp;
	Util.ASSERT(!frame.getRegFileInfo().isRegister(treeTemp));
	Temp instrTemp = (Temp) tempmap.get(treeTemp);
	if (instrTemp==null) {
	    instrTemp = frame.getTempBuilder().makeTemp(t, tf);
	    tempmap.put(treeTemp, instrTemp);
	} 
	Util.ASSERT(instrTemp.tempFactory()==tf);
	return instrTemp;
    }
    private Map tempmap = new HashMap();


    protected Derivation getDerivation() {
	final Map ti2td = this.ti2td; // keep own copy of this map.
	return new Derivation() {
	    public Derivation.DList derivation(HCodeElement hce, Temp t) 
		throws TypeNotKnownException {
		TypeAndDerivation tad = 
		    (TypeAndDerivation) ti2td.get( Default.pair(hce, t) );
		if (tad==null) throw new TypeNotKnownException(hce, t);

		return Derivation.DList.rename(tad.dlist, new TempMap() {
		    public Temp tempMap(Temp t1) {
			Temp tr = (Temp) tempmap.get(t1);
			Util.ASSERT(tr != null);
			return tr;
		    }
		});
	    }
	    
	    public HClass typeMap(HCodeElement hce, Temp t) 
		throws TypeNotKnownException {
		TypeAndDerivation tad = 
		    (TypeAndDerivation) ti2td.get( Default.pair(hce, t) );
		if (tad==null) throw new TypeNotKnownException(hce, t);
		return tad.type;
	    }
	};
    }

    // tXi -> TypeAndDerivation
    private Map ti2td = new HashMap();

    private Map tempToType = new HashMap();
	
    public void declare(Temp t, HClass clz) {
	// System.out.println(t + " " + clz);
	tempToType.put(t, new TypeAndDerivation(clz));
    }
    
    public void declare(Temp t, Derivation.DList dl) {
	// System.out.println(t + " " + dl);
	tempToType.put(t, new TypeAndDerivation(dl));
    }

    protected void clearDecl() { tempToType.clear(); }

    protected void _methodPrologue_(harpoon.IR.Assem.InstrFactory inf) {
	// initialize state variables each time gen() is called
	first = null; last = null;
	ti2td = new HashMap(); // reset derivation information.
	tempmap.clear();
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
