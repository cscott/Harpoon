// TreePostPass.java, created Thu Jan 11 16:04:23 2001 by cananian
// Copyright (C) 2000  <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;
/**
 * <code>TreePostPass</code> performs some low-level transformations to
 * the output of <code>SyncTransformer</code> which cannot be done in
 * the quad form which <code>SyncTransformer</code> operates on.
 * <code>TreePostPass</code> works on tree form.
 * <p>
 * This pass is invoked by <code>SyncTransformer.treeCodeFactory()</code>.
 * 
 * @author   <cananian@alumni.princeton.edu>
 * @version $Id: TreePostPass.java,v 1.1.2.1 2001-01-11 23:14:12 cananian Exp $
 */
class TreePostPass extends harpoon.Analysis.Tree.Simplification {
    private final List RULES = new ArrayList(); 
    TreePostPass(final Frame f, final long FLAG_VALUE, HField HFflagvalue) {
	final Label Lflagvalue = f.getRuntime().nameMap.label(HFflagvalue);
	// add all rules to rule set
	//          ...remove mentions of Object.flagValue field...
	// MEM(NAME(Label(flagValue))) -> CONST(0x44f702f7L)
	RULES.add(new Rule("constFlagValue") {
	    public boolean match(Exp e) {
		if (!contains(_KIND(e), _MEM)) return false;
		MEM mem = (MEM) e;
		if (!contains(_KIND(mem.getExp()), _NAME)) return false;
		NAME name = (NAME) mem.getExp();
		// check label.
		return name.label.equals(Lflagvalue);
	    }
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		MEM e1 = (MEM) e;
		NAME e2 = (NAME) e1.getExp();
		if (dg!=null) { dg.remove(e1); dg.remove(e2); }
		CONST c = f.pointersAreLong() ?
		    new CONST(tf, e, FLAG_VALUE) :
		    new CONST(tf, e, (int) FLAG_VALUE);
		if (dg!=null) { dg.putType(c, HClass.Void); }
		return c;
	    }
	});
	// other uses of flagValue are errors.
	// NAME(Label(flagValue)) -> CONST(0) (and assert false)
	RULES.add(new Rule("assertFlagValue") {
	    public boolean match(Exp e) {
		if (contains(_KIND(e.getParent()), _MEM)) return false;
		if (!contains(_KIND(e), _NAME)) return false;
		NAME name = (NAME) e;
		// check label.
		boolean b = name.label.equals(Lflagvalue);
		Util.assert(!b, "flag value slipped past us!");
		return b;
	    }
	    // otherwise a very visible (we hope) runtime error.
	    public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg) {
		return new CONST(tf, e); // null pointer
	    }
	});
    }
    /** Code factory for applying the post pass to the given tree
     *  form.  Clones the tree before processing it in-place. */
    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    }
}
