// DerivationChecker.java, created Mon Oct 16 18:01:29 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.*;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;

import java.util.*;
/**
 * A <code>DerivationChecker</code> checks that all temps
 * defined in a <code>LowQuad.Code</code> have proper
 * derivations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DerivationChecker.java,v 1.1.2.1 2000-10-16 22:19:01 cananian Exp $
 */
public class DerivationChecker
    extends harpoon.Analysis.Transformation.MethodMutator {
    
    /** Creates a <code>DerivationChecker</code>. */
    public DerivationChecker(HCodeFactory parent) { super(parent); }

    /** No mutation is done (we only check the <code>HCode</code>). */
    protected HCode mutateHCode(HCodeAndMaps input) {
	harpoon.IR.Quads.Code c = (harpoon.IR.Quads.Code) input.hcode();
	// look at the derivation for every lowquad.
	Derivation d = c.getDerivation();
	ASSERT(d!=null, "Checker found no derivation!");
	for (Iterator it=c.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    Temp[] defs = q.def();
	    for (int i=0; i<defs.length; i++) {
		try {
		    HClass hc = d.typeMap(q, defs[i]);
		    Derivation.DList dl = d.derivation(q, defs[i]);
		    ASSERT(hc!=null ^ dl!=null, "BAD TYPE: "+hc+" / "+dl);
		} catch (Derivation.TypeNotKnownException tnke) {
		    ASSERT(false, tnke);
		}
	    }
	}
	return c;
    }
    private void ASSERT(boolean cond, Object errmsg) {
	if (!cond) throw new Error(errmsg.toString());
    }
}
