// MakeGCThreadSafe.java, created Tue Jan 23 14:24:35 2001 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>MakeGCThreadSafe</code> adds a polling call to the beginning of each
 * method to check if another thread has caused a GC. In the output code, the
 * call should get wrapped appropriately with a dump and restore of the roots
 * for the thread. We may want to add the same code to backedges to make sure
 * all threads halt in a timely manner.
 * <p>
 * This pass is invoked in <code>harpoon.Main.SAMain</code>.
 *
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: MakeGCThreadSafe.java,v 1.1.2.1 2001-02-13 21:55:35 kkz Exp $
 */
public class MakeGCThreadSafe extends harpoon.Analysis.Tree.Simplification {
    // hide constructor
    private MakeGCThreadSafe() { }
    /** Code factory for adding GC polling calls to a
     *  canonical tree.  Clones the tree before doing
     *  optimization in-place. 
     */ 
    public static HCodeFactory codeFactory(final HCodeFactory parent,
					   final Frame f) {
	Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
	final Frame frame = f;
	return new HCodeFactory() {
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
			     HCE_RULES(code, f));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }
    public static List HCE_RULES(final harpoon.IR.Tree.Code code, 
				 final Frame f) {
	final Label LGCflag = new Label("halt_for_GC_flag");
	final Label LGCfunc = new Label(f.getRuntime().nameMap.c_function_name
					("halt_for_GC"));
	return Arrays.asList(new Rule[] {
	    new Rule("addGCpoll") {
		private final Set clones = new HashSet();
		public boolean match(Stm stm) {
		    return (contains(_KIND(stm), _METHOD) && 
			    !clones.contains(stm));
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    final Label Ltrue  = new Label();
		    final Label Lfalse = new Label();
		    final METHOD orig = (METHOD)stm;
		    final Stm clone = new METHOD(tf, stm, 
						 orig.getMethod(),
						 orig.getReturnType(), 
						 orig.getParams());
		    clones.add(clone);
		    final NAME flag = new NAME(tf, stm, LGCflag);
		    final NAME func = new NAME(tf, stm, LGCfunc);
		    return new SEQ
			(tf, stm, clone,
			 new SEQ
			 (tf, stm,
			  new CJUMP // if (halt_for_GC_flag)
			  (tf, stm, 
			   new MEM
			   (tf, stm, Type.INT, flag), Ltrue, Lfalse),
			  new SEQ
			  (tf, stm,
			   new LABEL
			   (tf, stm, Ltrue, false),
			   new SEQ
			   (tf, stm,
			    new NATIVECALL
			    (tf, stm, null, func, null),
			    new LABEL
			    (tf, stm, Lfalse, false)))));
		}
	    }
	});
    }
}
    /**
    private final List RULES = new ArrayList(); 
    public MakeGCThreadSafe(final Frame f) {
	final Frame frame = f;
	final Label LGCflag = new Label("halt_for_GC_flag");
	final Label LGCfunc = new Label(f.getRuntime().nameMap.c_function_name
	    ("halt_for_GC"));
	RULES.add(new Rule("addGCpoll") {
	    private final Set clones = new HashSet();
	    public boolean match(Stm stm) {
		return (contains(_KIND(stm), _METHOD) && 
			!clones.contains(stm));
	    }
	    public Stm apply(TreeFactory tf, Stm stm, DerivationGenerator dg) {
		final Label Ltrue  = new Label();
		final Label Lfalse = new Label();
		//final Stm clone = (Stm)stm.clone();
		final METHOD orig = (METHOD)stm;
		final Stm clone = new METHOD(tf, stm, 
					     orig.getMethod(),
					     orig.getReturnType(), 
					     orig.getParams());
		clones.add(clone);
		final NAME flag = new NAME(tf, stm, LGCflag);
		final NAME func = new NAME(tf, stm, LGCfunc);
		//dg.putType(flag, HClass.Void);
		//dg.putType(func, HClass.Void);
		return new SEQ
		    (tf, stm, clone,
		     new SEQ
		     (tf, stm,
		      new CJUMP // if (halt_for_GC_flag)
		      (tf, stm, 
		       new MEM
		       (tf, stm, Type.INT, flag), Ltrue, Lfalse),
		      new SEQ
		      (tf, stm,
		       new LABEL
		       (tf, stm, Ltrue, false),
		       new SEQ
		       (tf, stm,
			new NATIVECALL
			(tf, stm, null, func, null),
			new LABEL
			(tf, stm, Lfalse, false)))));
	    }
	});
    }
    public HCodeFactory codeFactory(final HCodeFactory parent) {
	return codeFactory(parent, RULES);
    } */   
