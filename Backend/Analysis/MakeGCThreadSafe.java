// MakeGCThreadSafe.java, created Tue Jan 23 14:24:35 2001 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>MakeGCThreadSafe</code> adds checks to see whether another thread 
 * has caused a GC, and if so, halts the current thread by calling out to 
 * the runtime. The check is added to the beginning of each method (after 
 * the <code>METHOD</code> node), and to backedges (before 
 * <code>JUMP</code>s and <code>CJUMP</code>s that can branch to an 
 * earlier node). The purpose of the former is to ensure that all threads 
 * halt in a timely manner.
 * <p>
 * This pass is invoked in <code>harpoon.Main.SAMain</code>.
 *
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: MakeGCThreadSafe.java,v 1.3 2002-02-26 22:43:06 cananian Exp $
 */
public class MakeGCThreadSafe extends harpoon.Analysis.Tree.Simplification {
    // hide constructor
    private MakeGCThreadSafe() { }
    /** Code factory for adding GC polling calls to a
     *  canonical tree.  Clones the tree before doing
     *  optimization in-place. (Stolen from 
     *  <code>Analysis.Tree.JumpOptimization</code>;
     *  it wouldn't have made any sense to subclass.)
     */ 
    public static HCodeFactory codeFactory(final HCodeFactory parent,
					   final Frame f) {
	Util.ASSERT(parent.getCodeName().equals(CanonicalTreeCode.codename));
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
	// collect information about reachable JUMPs and CJUMPs
	CFGrapher cfgr = code.getGrapher();
	final HCodeElement[] roots = cfgr.getFirstElements(code);
	final Set reachable = new HashSet();
	Worklist worklist = new WorkSet();
	for(int i = 0; i < roots.length; i++)
	    worklist.push(roots[i]);
	while(!worklist.isEmpty()) {
	    HCodeElement curr = (HCodeElement)worklist.pull();
	    reachable.add(curr);
	    for(Iterator it = cfgr.succC(curr).iterator(); it.hasNext(); ) {
		HCodeEdge succ = (HCodeEdge)it.next();
		if (!reachable.contains(succ.to()))
		    worklist.push(succ.to());
	    }
	}
	cfgr = null;
	worklist = null;
	// collect information to identify back edges
	final Map m = new HashMap();
	int count = 0;
	for (Iterator it=code.getElementsI(); it.hasNext(); ) {
	    Tree tr = (Tree) it.next();
	    if (tr.kind() == TreeKind.LABEL)
		m.put(((LABEL)tr).label, new Integer(count++));
	    else if (tr.kind() == TreeKind.CJUMP ||
		     tr.kind() == TreeKind.JUMP)
		m.put(tr, new Integer(count++));
	}
	final Label LGCflag = new Label("halt_for_GC_flag");
	final Label LGCfunc = new Label(f.getRuntime().getNameMap().c_function_name
					("halt_for_GC"));
	final Set cjumps = new HashSet();
	return Arrays.asList(new Rule[] {
	    new Rule("GCpollatMethod") {
		private final Set clones = new HashSet();
		public boolean match(Stm stm) {
		    // poll at the beginning of each method
		    return (contains(_KIND(stm), _METHOD) && 
			    !clones.contains(stm));
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    Util.ASSERT(reachable.contains(stm));
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
		    final CJUMP cjump = new CJUMP
			(tf, stm, 
			 new MEM
			 (tf, stm, Type.INT, flag), Ltrue, Lfalse);
		    cjumps.add(cjump);
		    return new SEQ
			(tf, stm, clone,
			 new SEQ
			 (tf, stm, cjump,
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
	    },
            new Rule("GCpollatCjump") {
		private final Set clones = new HashSet();
		public boolean match(Stm stm) {
		    // not a cjump, already processed, or our cjump
		    if (!contains(_KIND(stm), _CJUMP) || 
			clones.contains(stm) ||
			cjumps.contains(stm))
			return false;
		    CJUMP cjump = (CJUMP) stm;
		    int cid = ((Integer)m.get(cjump)).intValue();
		    // either target may be a back branch
		    return (cid > ((Integer)m.get(cjump.iftrue)).intValue() || 
			    cid > ((Integer)m.get(cjump.iffalse)).intValue());
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    Util.ASSERT(reachable.contains(stm));
		    final Label Ltrue  = new Label();
		    final Label Lfalse = new Label();
		    final CJUMP orig = (CJUMP)stm;
		    final Stm clone = new CJUMP(tf, stm, 
						orig.getTest(),
						orig.iftrue,
						orig.iffalse);
		    clones.add(clone);
		    final NAME flag = new NAME(tf, stm, LGCflag);
		    final NAME func = new NAME(tf, stm, LGCfunc);
		    final CJUMP cjump = new CJUMP
			(tf, stm, 
			 new MEM
			 (tf, stm, Type.INT, flag), Ltrue, Lfalse);
		    cjumps.add(cjump);
		    return new SEQ
			(tf, stm,
			 new SEQ
			 (tf, stm, cjump,
			  new SEQ
			  (tf, stm,
			   new LABEL
			   (tf, stm, Ltrue, false),
			   new SEQ
			   (tf, stm,
			    new NATIVECALL
			    (tf, stm, null, func, null),
			    new LABEL
			    (tf, stm, Lfalse, false)))),
			 clone);
		}
	    },
	    new Rule("GCpollatJump") {
		private final Set clones = new HashSet();
		public boolean match(Stm stm) {
		    // not a jump or already processed or not reachable
		    if (!contains(_KIND(stm), _JUMP) || 
			clones.contains(stm) ||
			!reachable.contains(stm))
			return false;
		    Exp target = ((JUMP) stm).getExp();
		    // any calculated branch may be a back edge
		    if (!contains(_KIND(target), _NAME))
			return true;
		    // a non-computed branch
		    Label Ltarget = ((NAME)target).label;
		    return (((Integer)m.get(stm)).intValue() > 
			    ((Integer)m.get(Ltarget)).intValue());
		}
		public Stm apply(TreeFactory tf, Stm stm, 
				 DerivationGenerator dg) {
		    final Label Ltrue  = new Label();
		    final Label Lfalse = new Label();
		    final JUMP orig = (JUMP)stm;
		    final Stm clone = new JUMP(tf, stm, 
					       orig.getExp(),
					       orig.targets);
		    clones.add(clone);
		    final NAME flag = new NAME(tf, stm, LGCflag);
		    final NAME func = new NAME(tf, stm, LGCfunc);
		    final CJUMP cjump = new CJUMP
			(tf, stm, 
			 new MEM
			 (tf, stm, Type.INT, flag), Ltrue, Lfalse);
		    cjumps.add(cjump);
		    return new SEQ
		       (tf, stm,
			new SEQ
			(tf, stm, cjump,
			 new SEQ
			 (tf, stm,
			  new LABEL
			  (tf, stm, Ltrue, false),
			  new SEQ
			  (tf, stm,
			   new NATIVECALL
			   (tf, stm, null, func, null),
			   new LABEL
			   (tf, stm, Lfalse, false)))),
			clone);
		}
	    }
	});
    }
}
