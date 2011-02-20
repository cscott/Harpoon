// BasePointerPreservingUseDefer.java, created Thu Aug 2 10:14:06 2001 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Analysis.Maps.Derivation;

import harpoon.Temp.Temp;
import harpoon.IR.Properties.UseDefer;
import harpoon.ClassFile.HCodeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * <code>BasePointerPreservingUseDefer</code> defines a view of the
 * code where uses of derived pointer values are also considered uses
 * of the base pointers that were used to compute the derived value.
 * This forces the register allocator to keep base pointers alive
 * (perhaps in stack locations, perhaps in registers).
 *
 * <p> A better implementation of this class would work to ensure that 
 * the usage counts of base pointers were not inflated too much by
 * these semantics because the base values are not REALLY being used
 * and so it would be a mistake to allocate them to registers most of
 * the time.  Perhaps the solution here is not in this class but in
 * the uses OF this class.
 * 
 */
public class BasePointerPreservingUseDefer extends UseDefer {
    private UseDefer backUD;
    private Derivation deriv;

    // helper sugar.
    private Derivation.DList deriv(HCodeElement hce, Temp t) {
	return deriv.derivation(hce,t);
    }
    
    public BasePointerPreservingUseDefer(UseDefer backing, Derivation deriv) {
	this.backUD = backing;
	this.deriv = deriv;
    }

    public Collection useC(HCodeElement hce) {
	ArrayList c = new ArrayList(backUD.useC(hce));
	ArrayList newElems = new ArrayList();
	for(Iterator ci = c.iterator(); ci.hasNext();){
	    Temp t = (Temp) ci.next();
	    Derivation.DList dlist = deriv(hce,t);
	    while (dlist != null) {
		newElems.add( dlist.base );
		dlist = dlist.next;
	    }
	}
	c.addAll( newElems );
	return c;
    }

    public Collection defC(HCodeElement hce) {
	return backUD.defC(hce);
    }

}
