// MemHoisting.java, created Wed Jun  6 16:52:24 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>MemHoisting</code> ensures that the ordering of MEM operations
 * is well-defined in the tree, by creating a temporary for and hoisting
 * all but one MEM in any given subexpression.  Canonicalization completes
 * the transformation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MemHoisting.java,v 1.1.2.3 2001-06-11 17:48:54 cananian Exp $
 */
public abstract class MemHoisting extends Simplification {
    // hide constructor
    public MemHoisting() { }
    /** Code factory for applying MemHoisting to a
     *  canonical tree.  Clones the tree before doing
     *  transformation in-place. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	Util.assert(parent.getCodeName().equals(CanonicalTreeCode.codename));
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
		    simplify((Stm)code.getRootElement(), dg, HCE_RULES(code));
		    hc = code;
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	});
    }

    private static int count(Tree tr, Map m, int memcnt) {
	memcnt += (tr.kind()==TreeKind.MEM) ? 1 : 0;
	m.put(tr, new Integer(memcnt));
	for (Tree tp=tr.getFirstChild(); tp!=null; tp=tp.getSibling())
	    memcnt=count(tp, m, memcnt);
	return memcnt;
    }
    private static void recurse(Stm stm, CFGrapher cfgr, Set done, Map m) {
	// do this one.
	done.add(stm);
	count(stm, m, 0);
	// do successors.
	for (Iterator it=cfgr.succElemC(stm).iterator(); it.hasNext(); ) {
	    Stm next = (Stm) it.next();
	    if (!done.contains(next))
		recurse(next, cfgr, done, m);
	}
    }

    public static List HCE_RULES(final harpoon.IR.Tree.Code code) {
	final CFGrapher cfgr = code.getGrapher();
	// collect the number of MEMs in every subtree.
	final Map memmap = new HashMap();

	Set done = new HashSet();
	HCodeElement[] roots = cfgr.getFirstElements(code);
	for (int i=0; i<roots.length; i++)
	    recurse((Stm)roots[i], cfgr, done, memmap);
	roots=null; done=null; // free memory.

	// now make rules.
	return Arrays.asList(new Rule[] {
	    // MEM(x) -> ESEQ(MOVE(t, MEM(x)), t)
	    new Rule("hoistMem") {
		public boolean match(Exp e) {
		    if (e.kind() != TreeKind.MEM) return false;
		    // no infinite application: rule out MOVE(t, MEM(x))
		    // instances.
		    // (this also prevents inadvertent application to
		    //  MOVE(MEM(x), ...) trees)
		    if (e.getParent()!=null &&
			e.getParent().kind() == TreeKind.MOVE) return false;
		    // optimization: leave the first mem in a stm alone.
		    if (((Integer)memmap.get(e)).intValue() < 2) return false;
		    return true;
		}
		public Exp apply(TreeFactory tf,Exp e,DerivationGenerator dg) {
		    MEM mem = (MEM) e;
		    Temp t = new Temp(tf.tempFactory(), "memhoist");
		    TEMP T1 = new TEMP(tf, e, mem.type(), t);
		    TEMP T2 = new TEMP(tf, e, mem.type(), t);
		    MEM nmem = (MEM) mem.build(tf, mem.kids());
		    if (dg!=null) { // make types for the new TEMPs/Temp
			HClass hc = dg.typeMap(mem);
			if (hc!=null) {
			    dg.putTypeAndTemp(T1, hc, t);
			    dg.putTypeAndTemp(T2, hc, t);
			    dg.putType(nmem, hc);
			} else {
			    dg.putDerivation(T1, dg.derivation(mem));
			    dg.putDerivation(T2, dg.derivation(mem));
			    dg.putDerivation(nmem, dg.derivation(mem));
			}
			dg.remove(mem);
		    }
		    memmap.put(nmem, memmap.get(mem)); memmap.remove(mem);
		    return new ESEQ(tf, e,
				    new MOVE(tf, e, T1, nmem),
				    T2);
		}
	    },
        });
    }
}
