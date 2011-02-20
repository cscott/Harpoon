// ConstantPropagation.java, created Fri Aug 24 22:57:03 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.UNOP;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Set;

/**
 * <code>ConstantPropagation</code> performs constant
 * propagation on canonical tree form.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: ConstantPropagation.java,v 1.4 2002-04-10 03:02:06 cananian Exp $
 */
public class ConstantPropagation extends 
    harpoon.Analysis.Transformation.MethodMutator {
    
    /** Creates a <code>ConstantPropagation</code>. */
    public ConstantPropagation(HCodeFactory parent) { 
	super(parent);
	assert parent.getCodeName().equals(CanonicalTreeCode.codename);
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	String old = Print.print((Tree)hc.getRootElement());
	CFGrapher cfger = hc.getGrapher();
	TreeVisitor tv = new ConstPropVisitor(hc);
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the tree in-place.
	Object[] elements = cfger.getElements(hc).toArray();
	for (int i=0; i < elements.length; i++) {
	    ((Stm) elements[i]).accept(tv);
	}
	return hc;
    }

    private static class ConstPropVisitor extends TreeVisitor {
	private ReachingDefs rd;
	boolean changed = false;

	ConstPropVisitor(Code hc) {
	    this.rd = new ReachingDefsAltImpl(hc, 
					      hc.getGrapher(), 
					      hc.getUseDefer());
	}

	// should never get here
	public void visit (Tree t) { assert false; }

	// ALIGNs are okay
	public void visit(ALIGN a) { }

	// for BINOPs, tricky
	public void visit(BINOP b) {
	    // go up until we find a Stm so we can use a UseDefer
	    Tree parent = parentStm(b);
	    assert parent != null;
	    Exp left = b.getLeft();
	    if (left.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)left, (Stm)parent, rd);
		if (val != null) b.setLeft(val);
	    } else {
		left.accept(this);
	    }
	    Exp right = b.getRight();
	    if (right.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)right, (Stm)parent, rd);
		if (val != null) b.setRight(val);
	    } else {
		right.accept(this);
	    }
	}

	// for CJUMPs, check test condition
	public void visit(CJUMP c) {
	    Exp test = c.getTest();
	    if (test.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)test, c, rd);
		if (val != null) c.setTest(val);
	    } else {
		test.accept(this);
	    }
	}
	
	// CONSTs are already okay
	public void visit(CONST c) { }

	// for DATUM, check data
	public void visit(DATUM d) {
	    Exp data = d.getData();
	    if (data.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)data, d, rd);
		if (val != null) d.setData(val);
	    } else {
		data.accept(this);
	    }
	}

	// for EXPRs, check exp
	public void visit(EXPR e) {
	    Exp exp = e.getExp();
	    if (exp.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)exp, e, rd);
		if (val != null) e.setExp(val);
	    } else {
		exp.accept(this);
	    }
	}

	// for INVOCATIONs, check func and args
	public void visit (INVOCATION i) {
	    Exp func = i.getFunc();
	    if (func.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)func, i, rd);
		if (val != null) i.setFunc(val);
	    } else {
		func.accept(this);
	    }
	    ExpList args = i.getArgs();
	    while(args != null) {
		Exp arg = args.head;
		if (arg.kind() == TreeKind.TEMP) {
		    Exp val = constant((TEMP)arg, i, rd);
		    if (val != null)
			i.setArgs(ExpList.replace(i.getArgs(), arg, val));
		} else {
		    arg.accept(this);
		}
		args = args.tail;
	    }
	}

	// for JUMPs, check exp
	public void visit(JUMP j) {
	    Exp exp = j.getExp();
	    if (exp.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)exp, j, rd);
		if (val != null) j.setExp(val);
	    } else {
		exp.accept(this);
	    }
	}

	// LABELs are okay
	public void visit(LABEL l) { }

	// for MEMs, tricky
	public void visit(MEM m) {
	    // go up until we find a Stm so we can use a UseDefer
	    Tree parent = parentStm(m);
	    assert parent != null;
	    Exp exp = m.getExp();
	    if (exp.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)exp, (Stm)parent, rd);
		if (val != null) m.setExp(val);
	    } else {
		exp.accept(this);
	    }
	}

	// METHODs are okay
	public void visit(METHOD m) { }

	// for MOVEs, check the src
	public void visit(MOVE m) {
	    Exp src = m.getSrc();
	    if (src.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)src, m, rd);
		if (val != null) m.setSrc(val);
	    } else {
		src.accept(this);
	    }
	}

	// NAMEs are okay
	public void visit(NAME n) { }
	
	// for RETURNs, check retval
	public void visit(RETURN r) {
	    Exp retval = r.getRetval();
	    if (retval.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)retval, r, rd);
		if (val != null) r.setRetval(val);
	    } else {
		retval.accept(this);
	    }
	}

	// SEGMENTs are okay
	public void visit(SEGMENT s) { }

	// TEMPs are okay
	public void visit(TEMP t) { }

	// for THROWs, check retex and handler
	public void visit(THROW t) {
	    Exp retex = t.getRetex();
	    if (retex.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)retex, t, rd);
		if (val != null) t.setRetex(val);
	    } else {
		retex.accept(this);
	    }
	    Exp handler = t.getHandler();
	    if (handler.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)handler, t, rd);
		if (val != null) t.setHandler(val);
	    } else {
		handler.accept(this);
	    }
	}

	// for UNOPs, tricky
	public void visit(UNOP u) {
	    // go up until we find a Stm so we can use a UseDefer
	    Tree parent = parentStm(u);
	    assert parent != null;
	    Exp operand = u.getOperand();
	    if (operand.kind() == TreeKind.TEMP) {
		Exp val = constant((TEMP)operand, (Stm)parent, rd);
		if (val != null) u.setOperand(val);
	    } else {
		operand.accept(this);
	    }
	}

	// returns the parent Stm to whom this Exp belongs
	private static Stm parentStm(Exp exp) {
	    Tree parent = exp.getParent();
	    while (!(parent instanceof Stm))
		parent = parent.getParent();
	    return (Stm)parent;
	}

	// returns a CONST or NAME if the given TEMP can be
	// replaced by such. else returns null.
	private static Exp constant(TEMP T, Stm parent, ReachingDefs rd) {
	    Set s = rd.reachingDefs(parent, T.temp);
	    // if no definitions reach, then this
	    // is dead code, and we're done
	    if (s.size() == 0)
		return null;
	    // all the definitions must be MOVEs with
	    // equivalent CONSTs or NAMEs as sources
	    Iterator it = s.iterator();
	    // all definitions are Stms
	    Stm first = (Stm) it.next();
	    if (first.kind() != TreeKind.MOVE) return null;
	    Exp firstSrc = ((MOVE) first).getSrc();
	    // if the first MOVE we see fits
	    // our criterion, continue
	    // all subsequent MOVEs must have
	    // equivalent sources
	    if (firstSrc.kind() == TreeKind.CONST) {
		CONST c = (CONST) firstSrc;
		while(it.hasNext()) {
		    // check for match
		    Stm stm = (Stm) it.next();
		    Number n = c.value();
		    if (stm.kind() != TreeKind.MOVE) return null;
		    Exp src = ((MOVE) stm).getSrc();
		    if (src.kind() != TreeKind.CONST ||
			src.type() != c.type())
			return null;
		    if (n == null) {
			if (((CONST) src).value() != null) return null;
		    } else {
			if (!((CONST) src).value().equals(n)) return null;
		    }
		}
		// done!
		return (Exp)c.clone();
	    } else if (firstSrc.kind() == TreeKind.NAME) {
		NAME n = (NAME) firstSrc;
		while(it.hasNext()) {
		    // check for match
		    Stm stm = (Stm) it.next();
		    if (stm.kind() != TreeKind.MOVE) return null;
		    Exp src = ((MOVE) stm).getSrc();
		    if (src.kind() != TreeKind.NAME ||
			!((NAME) src).label.equals(n.label))
			return null;
		    // all NAMEs have type Type.POINTER
		    assert src.type() == n.type();
		}
		// done!
		return (Exp)n.clone();
	    }
	    return null;
	}
    }
}
