// WriteBarrierConstElim.java, created Thu Aug 23 20:20:34 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Analysis.Tree.ConstantPropagation;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.List;

/**
 * <code>WriteBarrierConstElim</code> eliminates write barriers
 * on MOVEs that are assigned from constants. Constant 
 * propagation should probably be run beforehand for this pass
 * to make a difference.
 * <p>
 * This pass must be run before <code>WriteBarrierTreePass<code>
 * to have any effect.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierConstElim.java,v 1.3 2002-02-26 22:41:32 cananian Exp $
 */
public abstract class WriteBarrierConstElim extends 
    harpoon.Analysis.Tree.Simplification {
    
    // hide constructor
    private WriteBarrierConstElim() { }

    /** Code factory for applying <code>WriteBarrierConstElim/code>
     *  to a canonical tree.  Clones the tree before doing
     *  transformation in-place. */
    static HCodeFactory codeFactory(final Frame f,
				    final HCodeFactory parent,
				    final HMethod arrayHM,
				    final HMethod fieldHM) {
	Util.ASSERT(parent.getCodeName().equals(CanonicalTreeCode.codename));
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
			     HCE_RULES(code, f, arrayHM, fieldHM));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	});
    }

    public static List HCE_RULES(final harpoon.IR.Tree.Code code,
				 final Frame f, 
				 final HMethod arrayHM, 
				 final HMethod fieldHM) {
	final NameMap nm = f.getRuntime().getNameMap();
        final Label LarrayHM = nm.label(arrayHM);
	final Label LfieldHM = nm.label(fieldHM);
	final CFGrapher cfgr = code.getGrapher();
	// now make rules
	return Arrays.asList(new Rule[] {
	    new Rule("removeBarriers") {
		public boolean match(Stm stm) {
		    if (!contains(_KIND(stm), _CALL)) return false;
		    CALL call = (CALL) stm;
		    if (!contains(_KIND(call.getFunc()), _NAME)) return false;
		    NAME n = (NAME) call.getFunc();
		    if (!n.label.equals(LarrayHM) && !n.label.equals(LfieldHM))
			return false;
		    // now check if we need the write barrier
		    ExpList explist = call.getArgs();
		    // get to the third argument
		    explist = explist.tail;
		    explist = explist.tail;
		    Exp val = explist.head;
		    return (contains(_KIND(val), _CONST|_NAME));
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    // we can get rid of the barrier
		    return new EXPR(tf, stm, new CONST(tf, null));
		}
	    }
	});
    }
}
