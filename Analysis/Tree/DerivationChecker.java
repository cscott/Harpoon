// DerivationChecker.java, created Fri Jun 29 13:53:10 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeDerivation;

import java.util.Iterator;
/**
 * A <code>DerivationChecker</code> checks that all subtrees in
 * a <code>Tree.Code</code> have proper derivations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DerivationChecker.java,v 1.2 2002-02-25 21:00:32 cananian Exp $
 */
public class DerivationChecker {
    
    /** No one can create a <code>DerivationChecker</code> object. */
    private DerivationChecker() { }

    /** Create an <code>HCodeFactory</code> that will check the
     *  derivations of every "converted" <code>HCode</code>. */
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
	harpoon.IR.Tree.Code c = (harpoon.IR.Tree.Code) hcode;
	// look at the derivation for every lowquad.
	TreeDerivation d = c.getTreeDerivation();
	ASSERT(d!=null, "Checker found no derivation! "+c);
	for (Iterator it=c.getElementsI(); it.hasNext(); ) {
	    Tree t = (Tree) it.next();
	    if (!(t instanceof Exp)) continue;
	    try {
		HClass hc = d.typeMap((Exp)t);
		Derivation.DList dl = d.derivation((Exp)t);
		ASSERT(hc!=null ^ dl!=null, t, "BAD TYPE: "+hc+" / "+dl);
	    } catch (Derivation.TypeNotKnownException tnke) {
		ASSERT(false, t, tnke);
	    } catch (RuntimeException re) {
		ASSERT(false, t, re);
	    }
	}
    }
    /** Simple assertion facility -- this is not intended to be
     *  "compiled away" or turned off for speed like the standard
     *  harpoon.Util.Util assertion facility. */
    private static final void ASSERT(boolean cond, Object errmsg) {
	if (!cond) throw new Error(errmsg.toString());
    }
    private static final void ASSERT(boolean cond, Tree t, Object errmsg) {
	if (!cond) {
	    while (!(t instanceof Stm)) t=t.getParent();
	    throw new Error(errmsg + harpoon.IR.Tree.Print.print(t));
	}
    }
}
