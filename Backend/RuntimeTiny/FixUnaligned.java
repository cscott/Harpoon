// FixUnaligned.java, created Thu Mar 14 15:17:07 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Analysis.Tree.AlignmentAnalysis;
import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Analysis.Tree.Simplification;
import harpoon.Backend.Maps.FieldMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
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
 * <code>FixUnaligned</code> is a pass on <code>Tree</code> form
 * which used alignment analysis to discover unaligned references,
 * and then "corrects" them (using only aligned memory accesses)
 * in the proper way for your architecture (specified via parameters
 * to the code factory).
 *
 * <p><b>XXX BROKEN AND INCOMPLETE XXX</b></p>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FixUnaligned.java,v 1.1.2.1 2002-03-15 23:03:46 cananian Exp $
 */
class FixUnaligned extends Simplification {
    // hide constructor
    private FixUnaligned() { }

    /** Code factory for applying FixUnaligned to a
     *  canonical tree.  Clones the tree before doing
     *  transformation in-place.  Result may not be canonicalized. */
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
	    public String getCodeName() { return TreeCode.codename; }
	    public void clear(HMethod m) { parent.clear(m); }
        };
    }

    private static Stm findRoot(Exp e) {
	Tree t = e.getParent();
	if (t instanceof Exp)
	    return findRoot((Exp)t);
	else return (Stm) t;
    }
    private static int sizeOf(MEM mem) {
	if (mem.isSmall()) return (mem.bitwidth()+7)/8;
	return Type.isDoubleWord(mem.getFactory(), mem.type()) ? 8 : 4;
    }
    // XXX DERIVATIONS ARE NOT CORRECT.
    private static Exp assemble(TreeFactory tf, HCodeElement source, Temp base,
				int offset, int size,
				//Derivation deriv, DerivationGenerator dg,
				int totalsize, boolean bigEndian,
				boolean signed) {
	int ty = (totalsize > 4) ? Type.LONG : Type.INT;
	// big-endian or little-endian?
	// first, pull off the largest chunk you can from the front.
	int lowsize=1;
	boolean signThis = bigEndian ? (offset==0):(offset+lowsize==totalsize);
	Exp low = new MEM(tf, source, 8, signThis ? signed : false,
			  new BINOP
			  (tf, source, Type.POINTER, Bop.ADD,
			   new TEMP(tf, source, Type.POINTER, base),
			   new CONST(tf, source, offset)));
	int bitshift=bigEndian ? ((totalsize-(offset+lowsize))*8) : (offset*8);


	if (ty == Type.LONG)
	    low = new UNOP(tf, source, Type.INT, Uop._2L, low);
	if (bitshift>0)
	    low = new BINOP(tf, source, ty, Bop.SHL, low,
			    new CONST(tf, source, bitshift));
	if (size-lowsize > 0) {
	    // recursively fetch the remainder.
	    Exp high = assemble(tf, source, base, offset+lowsize, size-lowsize,
				totalsize, bigEndian, signed);
	    // assemble the pieces.
	    low = new BINOP(tf, source, ty, Bop.OR, low, high);
	}
	// done!
	return low;
    }
    private static Exp disassemble(MEM mem, TreeFactory tf,
				   DerivationGenerator dg) {
	HCodeElement source = mem;
	Temp t = new Temp(tf.tempFactory(), "fixun");
	assert mem.getExp().type() == Type.POINTER;
	return new ESEQ(tf, source,
			new MOVE
			(tf, source,
			 new TEMP(tf, source, Type.POINTER, t),
			 mem.getExp()),
			assemble(tf, source, t, 0, sizeOf(mem), sizeOf(mem),
				 Boolean.getBoolean
				 ("harpoon.runtimetiny.big-endian"),
				 mem.isSmall() ? mem.signed() : true));
    }

    public static List<Rule> HCE_RULES(final harpoon.IR.Tree.Code code) {
	// make an AlignmentAnalysis
	final AlignmentAnalysis aa =
	    new AlignmentAnalysis(code, code.getGrapher(), code.getUseDefer(),
				  code.getTreeDerivation());
	// now make rules.
	return Arrays.asList(new Rule[] {
	    // MEM(x) -> byte-wise MEM *reads only*
	    new Rule("breakupRead") {
		public boolean match(Exp e) {
		    // this must be a MEM.
		    if (e.kind() != TreeKind.MEM) return false;
		    // make sure this is a read.
		    if (e.getParent().kind() == TreeKind.MOVE &&
			((MOVE)e.getParent()).getDst() == e) return false;
		    // some other constraints.
		    if (Type.isFloatingPoint(e.type())) return false;
		    AlignmentAnalysis.Value v = aa.valueOf(((MEM)e).getExp(),
							   findRoot(e));
		    if (!v.isBaseKnown()) return false; //not well-typed.
		    // XXX here we would theoretically look at the offset
		    // and figure out if disassembly is necessary.
		    // but we're just going to assume that it is.
		    return true;
		}
		public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg)
		{
		    return disassemble((MEM)e, tf, dg);
		}
	    },
	    new Rule("breakupWrite") {
		public boolean match(Exp e) {
		    // XXX
		    return false;
		}
		public Exp apply(TreeFactory tf, Exp e, DerivationGenerator dg)
		{
		    assert false;
		    return e;
		}
	    },
	});
    }
}
