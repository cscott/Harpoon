// WriteBarrierPostPass.java, created Fri Aug 17 19:15:38 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>WriteBarrierPostPass</code> performs some low-level 
 * transformations to the output of <code>WriteBarrierPrePass</code> 
 * which cannot be done in the quad form on which 
 * <code>WriteBarrierPrePass</code> operates. 
 * <code>>WriteBarrierPostPass</code> works on tree form.
 * <p>
 * This pass is invoked by 
 * <code>WriteBarrierPrePass.treeCodeFactory()</code>.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierPostPass.java,v 1.1.2.1 2001-08-21 01:08:15 kkz Exp $
 */
public class WriteBarrierPostPass extends 
    harpoon.Analysis.Tree.Simplification {
    private final List RULES = new ArrayList();
    
    /** Creates a <code>WriteBarrierPostPass</code>. */
    WriteBarrierPostPass(final Frame f, HMethod hm) {
        final Label Lhm = f.getRuntime().getNameMap().label(hm);

	// add rule
	RULES.add(new Rule("replaceCall") {
	    public boolean match(Stm stm) {
		if (!contains(_KIND(stm), _CALL)) return false;
		CALL call = (CALL) stm;
		if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		return ((NAME) call.getFunc()).label.equals(Lhm);
	    }
	    public Stm apply(TreeFactory tf, Stm stm, DerivationGenerator dg) {
		final NAME func =
		    new NAME(tf, stm, 
			     new Label(tf.getFrame().getRuntime().
				       getNameMap().c_function_name
				       ("generational_write_barrier")));
		if (dg != null) dg.putType(func, HClass.Void);
		ExpList explist = ((CALL) stm).getArgs();
		Util.assert(explist.tail == null);
		return new NATIVECALL(tf, stm, null, func, explist);
	    }
	});
    }

    /** Code factory for applying the post pass to the given tree
     *  form.  Clones the tree before processing it in-place. */
    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    }    
}
