// DerivationChecker.java, created Mon Oct 16 18:01:29 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;

import java.util.Iterator;
/**
 * A <code>DerivationChecker</code> checks that all temps
 * defined in a <code>LowQuad.Code</code> have proper
 * derivations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DerivationChecker.java,v 1.1.2.2 2001-06-29 18:16:59 cananian Exp $
 */
public class DerivationChecker {
    
    /** No one can create a <code>DerivationChecker</code> object. */
    private DerivationChecker() { }

    /** Create an <code>HCodeFactory</code> that will check the
     *  derivation of every "converted" <code>HCode</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	return new HCodeFactory() {
	    public String getCodeName() { return hcf.getCodeName(); }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public HCode convert(HMethod m) {
		HCode hc = hcf.convert(m);
		if (hc!=null) check(hc);
		return hc;
	    }
	};
    }
    /** Check the given <code>HCode</code> for <code>Derivation</code>
     *  errors. */
    public static void check(HCode hcode) {
	harpoon.IR.Quads.Code c = (harpoon.IR.Quads.Code) hcode;
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
    }
    /** Simple assertion facility -- this is not intended to be
     *  "compiled away" or turned off for speed like the standard
     *  harpoon.Util.Util assertion facility. */
    private static void ASSERT(boolean cond, Object errmsg) {
	if (!cond) throw new Error(errmsg.toString());
    }
}
