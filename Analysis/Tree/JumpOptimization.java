// JumpOptimization.java, created Wed Feb 16 18:19:49 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.ClassFile.*;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.*;
import harpoon.Temp.Label;
import harpoon.Util.*;

import java.util.*;
/**
 * <code>JumpOptimization</code> removes branches-to-branches
 * and redundant labels.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JumpOptimization.java,v 1.1.2.1 2000-02-23 19:22:37 cananian Exp $
 */
public abstract class JumpOptimization extends Simplification {
    // hide constructor
    private JumpOptimization() { }
    
    /** Code factory for applying JumpOptimization to a
     *  canonical tree.  Clones the tree before doing
     *  optimization in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    harpoon.IR.Tree.Code code = (harpoon.IR.Tree.Code) hc;
		    // clone code...
		    code = (harpoon.IR.Tree.Code) code.clone(m);
		    DerivationGenerator dg = null;
		    try {
			dg = (DerivationGenerator) code.getTreeDerivation();
		    } catch (ClassCastException ex) { /* i guess not */ }
		    // ...do analysis and modify cloned code in-place.
		    simplify((Stm)code.getRootElement(), dg, HCE_RULES(code));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    public static List HCE_RULES(final harpoon.IR.Tree.Code code) {
	final CFGrapher cfgr = code.getGrapher();
	// collect info about useless branches
	final DisjointSet labelmap = new DisjointSet();
	for (Iterator it=code.getElementsI(); it.hasNext(); ) {
	    Tree tr = (Tree) it.next();
	    if (tr.kind() == TreeKind.LABEL) {
		LABEL label = (LABEL) tr;
		HCodeEdge[] succ = cfgr.succ(label);
		if (succ.length==1 &&
		    ((Tree)succ[0].to()).kind() == TreeKind.JUMP) {
		    JUMP jump = (JUMP) succ[0].to();
		    Util.assert(jump.targets!=null);
		    if (jump.targets.tail==null) { // only one target.
			labelmap.union(label.label, jump.targets.head);
			Util.assert(labelmap.find(jump.targets.head)!=
				    label.label);
		    }
		}
	    }
	}
	// now make rules.
	return Arrays.asList(new Rule[] {
	    // JUMP(NAME(l)) --> JUMP(NAME(map(l)))
	    new Rule("redirJump") {
		public boolean match(Stm s) {
		    if (s.kind() != TreeKind.JUMP) return false;
		    JUMP jump = (JUMP) s;
		    return jump.targets.tail==null &&
			labelmap.find(jump.targets.head)!=jump.targets.head;
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    JUMP jump = (JUMP) s;
		    return new JUMP(tf, s,
				    (Label) labelmap.find(jump.targets.head));
		}
	    },
	    // CJUMP(x, iftrue, iffalse) -> CJUMP(x, map(iftrue), map(iffalse))
	    new Rule("redirCjump") {
		public boolean match(Stm s) {
		    if (s.kind() != TreeKind.CJUMP) return false;
		    CJUMP cjump = (CJUMP) s;
		    return labelmap.find(cjump.iftrue)!=cjump.iftrue ||
			labelmap.find(cjump.iffalse)!=cjump.iffalse;
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    CJUMP cjump = (CJUMP) s;
		    return new CJUMP(tf, s, cjump.getTest(),
				     (Label) labelmap.find(cjump.iftrue),
				     (Label) labelmap.find(cjump.iffalse));
		}
	    },
	    // CALL(.., NAME(handler), ..) -> CALL(.., NAME(map(handler)), ..)
	    new Rule("redirHandler") {
		public boolean match(Stm s) {
		    if (s.kind() != TreeKind.CALL) return false;
		    CALL call = (CALL) s;
		    Label l = call.getHandler().label;
		    return labelmap.find(l) != l;
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    CALL call = (CALL) s;
		    NAME oldhandler = call.getHandler();
		    NAME newhandler =
			new NAME(tf, oldhandler,
				 (Label) labelmap.find(oldhandler.label) );
		    return new CALL(tf, s, call.getRetval(), call.getRetex(),
				    call.getFunc(), call.getArgs(),
				    newhandler, call.isTailCall);
		}
	    },
        });
    }
}
