// DynamicWBTreePass.java, created Tue Jun 25 14:35:31 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Runtime1.TreeBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;

import java.util.Arrays;
import java.util.List;

/**
 * <code>DynamicWBTreePass</code> performs some low-level transformations to
 * the output of <code>DynamicWBQuadPass</code> which cannot be done in the
 * quad form on which <code>DynamicWBQuadPass</code> operates. 
 * <code>DynamicWBTreePass</code> works on tree form.
 * <p>
 * 
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: DynamicWBTreePass.java,v 1.2 2003-03-11 19:15:41 cananian Exp $
 */
public class DynamicWBTreePass extends harpoon.Analysis.Tree.Simplification {
    
    // hide constructor
    private DynamicWBTreePass() { }
        
    /** Code factory for applying <code>DynamicWBTreePass</code> to a
     *  canonical tree.  Clones the tree before doing transformation in-place.
     */
    static public HCodeFactory codeFactory(final HCodeFactory parent,
					   final Frame frame,
					   final Linker linker) {
	final HMethod clearHM = 
	    linker.forName("harpoon.Runtime.PreciseGC.WriteBarrier").getMethod
	    ("clearBit", new HClass[] { linker.forName("java.lang.Object") });
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
	return Canonicalize.codeFactory(new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m).hcode();
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { /* i guess not */ }
		    // ...do analysis and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, 
			     HCE_RULES(frame, clearHM));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	});
    }

    private static List<Rule> HCE_RULES(final Frame frame, final HMethod clearHM) {
	final Runtime runtime = frame.getRuntime();
        final Label LclearHM = runtime.getNameMap().label(clearHM);
	// now make rules
	return Arrays.asList(new Rule[] {
	    new Rule("replaceClearBit") {
		// match calls to clearBit method
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _CALL)) return false;
		    CALL call = (CALL) stm;
		    if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		    return ((NAME) call.getFunc()).label.equals(LclearHM);
		}
		public Stm apply(TreeFactory tf, Stm stm,
				 DerivationGenerator dg) {
		    TreeBuilder tb = (TreeBuilder) runtime.getTreeBuilder();
		    return tb.clearHashBit
			(tf, stm, dg, ((CALL)stm).getArgs().head);
		}
	    }
	});
    }
}
