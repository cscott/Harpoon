// DerivationGenerator.java, created Mon Feb 28 16:01:34 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>DerivationGenerator</code> helps maintain the accuracy of
 * the <code>Derivation</code> while the register allocator creates
 * spills.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DerivationGenerator.java,v 1.3 2002-02-26 22:43:31 cananian Exp $
 */
public class DerivationGenerator implements Derivation {
    private Map dtM = new HashMap();
    
    /** Creates a <code>DerivationGenerator</code>. */
    public DerivationGenerator(Instr instrs, Derivation deriv) {
	Util.ASSERT(deriv!=null);
	for (Instr in=instrs; in!=null; in=in.getNext()) {
	    for (Iterator it=in.defC().iterator(); it.hasNext(); ) {
		Temp d = (Temp) it.next();
		HClass hc = deriv.typeMap(in, d);
		DList dl  = deriv.derivation(in, d);
		if (hc!=null)
		    dtM.put(Default.pair(in, d), new TypeAndDerivation(hc));
		else
		    dtM.put(Default.pair(in, d), new TypeAndDerivation(dl));
	    }
	}
    }
    
    // public interface
    public HClass typeMap(HCodeElement hce, Temp t)
	throws TypeNotKnownException {
	TypeAndDerivation tad =
	    (TypeAndDerivation) dtM.get(Default.pair(hce, t));
	if (tad==null) throw new TypeNotKnownException(hce, t);
	return tad.type;
    }
    public DList  derivation(HCodeElement hce, Temp t)
	throws TypeNotKnownException {
	TypeAndDerivation tad =
	    (TypeAndDerivation) dtM.get(Default.pair(hce, t));
	if (tad==null) throw new TypeNotKnownException(hce, t);
	return tad.derivation;
    }
    // private interface
    /** replace old instr with new instr, using specified temp map for defs */
    void update(Instr oldi, Instr newi, TempMap defmap) {
	for (Iterator it=oldi.defC().iterator(); it.hasNext(); ) {
	    Temp d = (Temp) it.next();
	    TypeAndDerivation tad = 
		(TypeAndDerivation) dtM.remove(Default.pair(oldi, d));
	    dtM.put(Default.pair(newi, defmap.tempMap(d)), tad);
	}
    }
    void copy(Instr oldi, Temp olduse, Instr newi, Temp newuse) {
	// XXX HACK
	dtM.put(Default.pair(newi, newuse),
		new TypeAndDerivation(HClass.Void));
    }

    /** internal structure of type/derivation information */
    private static class TypeAndDerivation {
	/** non-null for base pointers */
	public final HClass type;
	/** non-null for derived pointers */ 
	public final DList derivation;
	// public constructors
	TypeAndDerivation(HClass type) { this(type, null); }
	TypeAndDerivation(DList deriv) { this(null, deriv); }
	/** private constructor */
	private TypeAndDerivation(HClass type, DList derivation) {
	    Util.ASSERT(type!=null ^ derivation!=null);
	    this.type = type;
	    this.derivation = derivation;
	}
    }
}
