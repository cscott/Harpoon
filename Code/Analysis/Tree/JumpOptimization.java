// JumpOptimization.java, created Wed Feb 16 18:19:49 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Label;
import harpoon.Util.Collections.DisjointSet;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * <code>JumpOptimization</code> removes branches-to-branches
 * and redundant labels.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JumpOptimization.java,v 1.6 2003-03-11 18:46:47 cananian Exp $
 */
public abstract class JumpOptimization extends Simplification {
    // hide constructor
    private JumpOptimization() { }
    
    /** Code factory for applying JumpOptimization to a
     *  canonical tree.  Clones the tree before doing
     *  optimization in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
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
		    simplify((Stm)code.getRootElement(), dg, HCE_RULES(code));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    public static List<Rule> HCE_RULES(final harpoon.IR.Tree.Code code) {
	final CFGrapher<Tree> cfgr = code.getGrapher();
	// collect info about useless branches
	final DisjointSet<Label> labelmap = new DisjointSet<Label>();
	for (Iterator<Tree> it=code.getElementsI(); it.hasNext(); ) {
	    Tree tr = it.next();
	    if (tr.kind() == TreeKind.LABEL) {
		LABEL label = (LABEL) tr;
		HCodeEdge<Tree>[] succ = cfgr.succ(label);
		if (succ.length==1 &&
		    succ[0].to().kind() == TreeKind.JUMP) {
		    JUMP jump = (JUMP) succ[0].to();
		    assert jump.targets!=null;
		    // note that self-loops are not considered useless.
		    if (jump.targets.tail==null && // only one target.
			!label.label.equals(jump.targets.head)) {
			labelmap.union(label.label, jump.targets.head);
			assert labelmap.find(label.label)
			    .equals(labelmap.find(jump.targets.head));
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
	    // this rule will flip CJUMPs to achieve better lay-out.
	    // CJUMP(BINOP(op, ..), iftrue, iffalse) ; LABEL(iftrue)  -->
	    //   CJUMP(BINOP(inv(op),..), iffalse, iftrue)
	    new Rule("flipCjump") {
		public boolean match(Stm s) {
		    if (s.kind() != TreeKind.CJUMP) return false;
		    CJUMP cjump = (CJUMP) s;
		    if (cjump.getTest().kind() != TreeKind.BINOP) return false;
		    BINOP binop = (BINOP) cjump.getTest();
		    if (!contains(_OP(binop.op),
				  _CMPEQ|_CMPNE|_CMPGT|_CMPGE|_CMPLT|_CMPLE))
			return false;
		    // now check if the next label corresponds to the iffalse.
		    Stm next = next(s);
		    if (next.kind() != TreeKind.LABEL) return false;
		    LABEL l = (LABEL) next;
		    return l.label == cjump.iftrue;
		}
		public Stm apply(TreeFactory tf,Stm s,DerivationGenerator dg) {
		    CJUMP cjump = (CJUMP) s;
		    BINOP binop = (BINOP) cjump.getTest();
		    return new CJUMP
			(tf, cjump, new BINOP
			 (tf, binop, binop.optype,
			  Bop.invert(binop.op),
			  binop.getLeft(), binop.getRight()),
			 cjump.iffalse, cjump.iftrue);
		}
		// find the next Stm in layout order, skipping SEQs.
		Stm next(Stm s) {
		    // go up until we find a SEQ that we're not the
		    // right-most child of.
		    while (s.getSibling()==null)
			s = (Stm) s.getParent();
		    // assert that our parent is a SEQ here.
		    assert s.getParent().kind()==TreeKind.SEQ;
		    // step over to the right...
		    s = (Stm) s.getSibling();
		    // ...and zip down the left side of the subtree until
		    // we find something other than a SEQ.
		    while (s.kind()==TreeKind.SEQ)
			s = (Stm) s.getFirstChild();
		    // done.  this is it.
		    return s;
		}
	    },
        });
    }
}
