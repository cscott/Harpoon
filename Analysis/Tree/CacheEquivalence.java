// CacheEquivalence.java, created Wed Jun  6 15:06:02 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.DomTree;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode.PrintCallback;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.Print;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Default;
import harpoon.Util.Environment;
import harpoon.Util.HashEnvironment;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>CacheEquivalence</code> creates tag-check equivalence classes
 * for MEM operations in a Tree.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CacheEquivalence.java,v 1.1.2.11 2001-06-14 20:57:50 cananian Exp $
 */
public class CacheEquivalence {
    private static final boolean DEBUG=false;
    private static final int CACHE_LINE_SIZE = 32; /* bytes */

    /** Creates a <code>CacheEquivalence</code>. */
    public CacheEquivalence(harpoon.IR.Tree.Code code) {
	CFGrapher cfg = code.getGrapher();
	UseDefer udr = code.getUseDefer();
	TreeDerivation td = code.getTreeDerivation();
	/* new analysis */
	final Dataflow df = new Dataflow(code, cfg, udr, td);
	if (DEBUG) {
	    harpoon.IR.Tree.Print.print(new java.io.PrintWriter(System.out),
					code, new PrintCallback() {
		public void printAfter(java.io.PrintWriter pw,
				       HCodeElement hce) {
		    if (hce instanceof Exp) {
			Exp e = (Exp) hce;
			Tree t = e;
			while (!(t instanceof Stm))
			    t = t.getParent();
			pw.print(" [VAL: "+df.valueOf(e, (Stm)t)+"]");
		    }
		}
	    });
	}
	/*- construct cache eq -*/
	new TagDominate(code, cfg, td, df);
    }
    /* -------- cache equivalence pass ------- */
    private class TagDominate {
	final TreeDerivation td;
	final Dataflow df;
	final DomTree dt;
	final TreeBuilder tb;
	TagDominate(Code c, CFGrapher cfg, TreeDerivation td, Dataflow df) {
	    this.td = td;
	    this.df = df;
	    this.tb = c.getFrame().getRuntime().treeBuilder;
	    this.dt = new DomTree(c, cfg, false);

	    final Environment e = new HashEnvironment();
	    HCodeElement[] roots = dt.roots();
	    for (int i=0; i<roots.length; i++)
		traverseDT((Stm) roots[i], e);
	}
	/* analyze stms travelling down the dominator tree */
	void traverseDT(Stm stm, Environment e) {
	    /* save environment */
	    Environment.Mark mark = e.getMark();
	    /* do analysis */
	    analyze(stm, e);
	    /* recurse */
	    HCodeElement[] child = dt.children(stm);
	    for (int i=0; i<child.length; i++)
		traverseDT((Stm)child[i], e);
	    /* restore environment */
	    e.undoToMark(mark);
	    /* done! */
	    return;
	}
	/* analyze one statement in the environment defined by map m */
	void analyze(Stm stm, Map pre) {
	    Map post = new HashMap();
	    /* first look for all *reads* */
	    /* There is NO ORDER defined for any of these. */
	    for (ExpList el=stm.kids(); el!=null; el=el.tail) {
		add(stm, el.head, pre, post, true);
	    }
	    /* now all post mappings get added to pre */
	    pre.putAll(post); post.clear();
	    /* now look for writes (which must happen after reads) */
	    if (stm.kind()==TreeKind.MOVE)
		add(stm, ((MOVE)stm).getDst(), pre, post, false);
	    pre.putAll(post); post.clear();
	}
	void add(Stm root, Exp e, Map pre, Map post, boolean recurse) {
	    if (e.kind()==TreeKind.MEM) {
		MEM mem = (MEM) e;  Exp memexp = mem.getExp();
		Dataflow.Value v = df.valueOf(memexp, root);
		// cases:
		//  1) known base & known offset.
		//  2) known base & unknown offset, but object is smaller
		//     than cache line size.
		//  3) all others.
		Dataflow.DefPoint dp = null, kgroup = null;
		long offset=0; long modulus=0;
		if (v.isBaseKnown()) {
		    if (v.isOffsetKnown()) {
			Dataflow.KnownOffset ko = (Dataflow.KnownOffset) v;
			if (ko.offset.modulus==0) {
			    dp = ko.def; offset = ko.offset.offset; // case 1
			}
		    } else {
			Dataflow.UnknownOffset uo = (Dataflow.UnknownOffset) v;
			if (objSize(uo.def.type()) <= CACHE_LINE_SIZE
			    // arrays can't count as small because
			    // length is not statically known.
			    && !uo.def.type().isArray()) {
			    dp = uo.def; offset = 0; // case 2;
			}
		    }
		}
		if (dp!=null) { // cases 1 and 2
		    int line = (int) (offset / CACHE_LINE_SIZE);
		    List pair = Default.pair(dp, new Integer(line));
		    CacheEquivSet ces = (CacheEquivSet) pre.get(pair);
		    if (ces==null) ces = new CacheEquivSet(mem);
		    else ces.others.add(mem);
		    cache_equiv.put(mem, ces);
		    post.put(pair, ces);
		} else { // case 3
		    cache_equiv.put(mem, new CacheEquivSet(mem));
		}
	    }
	    if (recurse)
		for (Tree tp=e.getFirstChild(); tp!=null; tp=tp.getSibling())
		    add(root, (Exp)tp, pre, post, recurse);
	}
	int objSize(HClass hc) { return tb.headerSize(hc)+tb.objectSize(hc); }
    }

    /*---------------- dataflow pass -------------------*/
    private static class Dataflow {
	final ReachingDefs rd;
	final TreeDerivation td;
	Dataflow(Code c, CFGrapher cfg, UseDefer udr, TreeDerivation td) {
	    this.rd = new ReachingDefsAltImpl(c, cfg, udr);
	    this.td = td;
	    new StmVisitor(c, cfg, udr);
	}
	/* dataflow result accessor functions */
	Value valueOf(Exp e, Stm root) {
	    return new ValueVisitor(e, root).value;
	}
	/* temp x stm-to-value mapping */
	private Map valueMap = new HashMap();
	Value valueUseAt(Temp t, Stm s) {
	    Value v = Value.NOINFO;
	    for (Iterator it=rd.reachingDefs(s, t).iterator(); it.hasNext(); ){
		Stm def = (Stm) it.next();
		v = v.unify(valueDefAt(t, def));
	    }
	    return v;
	}
	Value valueDefAt(Temp t, Stm s) {
	    Value v = (Value) valueMap.get(Default.pair(t,s));
	    return (v==null) ? Value.NOINFO : v;
	}
	/** visitor class to do dataflow through statements */
	private class StmVisitor extends TreeVisitor {
	    MultiMap uses = new GenericMultiMap();
	    WorkSet ws = new WorkSet();
	    StmVisitor(Code c, CFGrapher cfg, UseDefer udr) {
		/* create uses map & add all elements to worklist. */
		for (Iterator it=cfg.getElements(c).iterator(); it.hasNext();){
		    Stm s = (Stm) it.next();
		    for (Iterator it2=udr.useC(s).iterator(); it2.hasNext();) {
			Temp u = (Temp) it2.next();
			for (Iterator it3=rd.reachingDefs(s, u).iterator();
			     it3.hasNext(); ) {
			    Stm d = (Stm) it3.next();
			    uses.add(Default.pair(u,d), s);
			}
		    }
		    // add all statements s to worklist.
		    ws.addLast(s);
		}
		/* for each element on the worklist... */
		while (!ws.isEmpty()) {
		    Stm s = (Stm) ws.removeFirst();
		    s.accept(this);
		}
	    }
	    public void update(Temp t, Stm def, Value v) {
		Util.assert(t!=null && def!=null && v!=null);
		List pair = Default.pair(t, def);
		Value old = (Value) valueMap.put(pair, v);
		if (old==null || !old.equals(v))
		    //put uses of t on the workset.
		    for (Iterator it = uses.getValues(pair).iterator();
			 it.hasNext(); )
			ws.addLast((Stm)it.next());
	    }
	    public void visit(Tree e) { Util.assert(false); }
	    public void visit(Stm s) { /* no defs */ }
	    public void visit(INVOCATION s) {
		TEMP t = s.getRetval();
		if (t!=null)
		    update(t.temp, s,
			   new KnownOffset(new TempDefPoint(t, s), 0));
	    }
	    public void visit(CALL s) {
		visit((INVOCATION)s);
		TEMP t = s.getRetex();
		update(t.temp, s,
		       new KnownOffset(new TempDefPoint(t, s), 0));
	    }
	    public void visit(METHOD s) {
		for (int i=0; i<s.getParamsLength(); i++) {
		    TEMP t = s.getParams(i);
		    update(t.temp, s,
			   new KnownOffset(new TempDefPoint(t, s), 0));
		}
	    }
	    public void visit(MOVE s) {
		if (s.getDst().kind()==TreeKind.TEMP) {
		    TEMP t = (TEMP) s.getDst();
		    Value v = valueOf(s.getSrc(), s);
		    if (s.getDst().type()!=Type.POINTER ||
			(v.isBaseKnown() &&
			 ((UnknownOffset)v).def.isWellTyped()))
			update(t.temp, s, v);
		    else
			update(t.temp, s,
			       new KnownOffset(new TempDefPoint(t, s),0));
		}
	    }
	}
	/** visitor class to compute Values of expression trees. */
	private class ValueVisitor extends TreeVisitor {
	    final Stm root;
	    Value value = Value.BOTTOM;
	    ValueVisitor(Exp e, Stm root) { this.root=root; e.accept(this); }
	    public void visit(Tree e) { Util.assert(false); }
	    public void visit(CONST c) {
		if (c.type()==Type.INT || c.type()==Type.LONG)
		    this.value = new Constant(c.value().longValue());
	    }
	    public void visit(ESEQ e) {
		this.value=valueOf(e.getExp(), root);
	    }
	    public void visit(MEM e) { /* bottom */ }
	    public void visit(NAME e) {
		this.value = new KnownOffset(new NameDefPoint(e), 0);
	    }
	    public void visit(BINOP e) {
		if (e.op==Bop.ADD) {
		    Value left = valueOf(e.getLeft(), root);
		    Value right= valueOf(e.getRight(), root);
		    this.value = left.add(right);
		} else if (e.op==Bop.MUL) {
		    Value left = valueOf(e.getLeft(), root);
		    Value right= valueOf(e.getRight(), root);
		    this.value = left.mul(right);
		} else if (e.op==Bop.SHL) {
		    Value left = valueOf(e.getLeft(), root);
		    Value right= valueOf(e.getRight(), root);
		    Value mult = (right instanceof Constant) ?
			new Constant(1<<((Constant)right).offset) :
			Value.UNKNOWN;
		    this.value = left.mul(mult);
		}
	    }
	    public void visit(UNOP e) {
		if (e.op==Uop.NEG) {
		    Value op = valueOf(e.getOperand(), root);
		    this.value = op.negate();
		}
	    }
	    public void visit(TEMP t) {
		this.value = valueUseAt(t.temp, root);
	    }
	}
	/* the value lattice: top/bottom, constants, and base pointers
	 * with known and unknown offsets, modulo a constant. */
	// op rules: most specific is 'this'.  base ptrs more spec than consts.
	static abstract class Value {  // bottom/noinfo.
	    abstract protected int specificity();
	    boolean isBaseKnown() { return false; }
	    boolean isOffsetKnown() { return false; }
	    Value unify(Value v) {
		Util.assert(this!=NOINFO);
		if (v==NOINFO) return this;
		return BOTTOM;
	    }
	    final Value add(Value v) {
		return (this.specificity()>v.specificity()) ?
		    this._add(v) : v._add(this);
	    }
	    protected abstract Value _add(Value v);
	    final Value mul(Value v) {
		return (this.specificity()>v.specificity()) ?
		    this._mul(v) : v._mul(this);
	    }
	    protected abstract Value _mul(Value v);
	    abstract Value negate();
	    static final Value BOTTOM = new Value() {
		protected int specificity() { return 0; }
		protected Value _add(Value v) { return BOTTOM; }
		protected Value _mul(Value v) { return BOTTOM; }
		Value negate() { return BOTTOM; }
		public String toString() { return "BOTTOM"; }
	    };
	    static final Value UNKNOWN = new UnknownConstant();
	    static final Value NOINFO = new Value() {
		protected int specificity() { return 6; }
		Value unify(Value v) { return v; }
		protected Value _add(Value v) { return NOINFO; }
		protected Value _mul(Value v) { return NOINFO; }
		Value negate() { return NOINFO; }
		public String toString() { return "NO INFO"; }
	    };
	}
	static class UnknownConstant extends Value {
	    protected int specificity() { return 1; }
	    UnknownConstant() {
		Util.assert(this instanceof ConstantModuloN ||
			    Value.UNKNOWN==null);
	    }
	    Value unify(Value v) {
		if (!(v instanceof UnknownConstant)) return super.unify(v);
		// UnknownConstant is common superclass of this and v.
		return Value.UNKNOWN;
	    }
	    protected Value _add(Value v) {
		// must handle v=BOTTOM, v=UNKNOWN
		if (v instanceof UnknownConstant) return Value.UNKNOWN;
		return Value.BOTTOM;
	    }
	    protected Value _mul(Value v) {
		// must handle v=BOTTOM, v=UNKNOWN
		if (v instanceof UnknownConstant) return Value.UNKNOWN;
		return Value.BOTTOM;
	    }
	    Value negate() { return this; }
	    public String toString() { return "X"; }
	}
	static class ConstantModuloN extends UnknownConstant {
	    protected int specificity() { return 2; }
	    final long offset; final long modulus;
	    ConstantModuloN(long offset, long modulus) {
		this.offset=offset; this.modulus=modulus;
		Util.assert(modulus>1 && offset>=0);
		Util.assert(offset < modulus);
	    }
	    protected ConstantModuloN(long offset) {
		Util.assert(this instanceof Constant);
		this.offset=offset; this.modulus=0;
	    }
	    boolean isOffsetKnown() { return true; }
	    Value unify(Value v) {
		if (!(v instanceof ConstantModuloN)) return super.unify(v);
		// ConstantModuloN is common superclass of this and v.
		ConstantModuloN cmn = (ConstantModuloN) v;
		long smallM, smallN, largeM, largeN;
		if (this.modulus > cmn.modulus) {
		    largeM=this.modulus; largeN=this.offset;
		    smallM=cmn.modulus;  smallN=cmn.offset;
		} else {
		    smallM=this.modulus; smallN=this.offset;
		    largeM=cmn.modulus;  largeN=cmn.offset;
		}
		Util.assert(largeM>0);
		long off=0, mod=1;
		if ((smallM==0 || smallM==largeM) &&
		    mymod(smallN, largeM)==largeN) {
		    off=largeN; mod=largeM;
		} else if (smallM > 0 &&
			   (largeM % smallM)==0 &&
			   (largeN % smallM)==smallN) {
		    off=smallN; mod=smallM;
		} // others?
		if (mod>1) return new ConstantModuloN(off, mod);
		// can't unify the constants.
		return Value.UNKNOWN;
	    }
	    protected Value _add(Value v) {
		// must handle v=BOTTOM, UNKNOWN, CONSTANTMODULON, CONSTANT
		if (v==Value.BOTTOM) return v;
		if (v==Value.UNKNOWN) return Value.UNKNOWN;
		ConstantModuloN cmn = (ConstantModuloN) v;
		long smallM, smallN, largeM, largeN;
		if (this.modulus > cmn.modulus) {
		    largeM=this.modulus; largeN=this.offset;
		    smallM=cmn.modulus;  smallN=cmn.offset;
		} else {
		    smallM=this.modulus; smallN=this.offset;
		    largeM=cmn.modulus;  largeN=cmn.offset;
		}
		if (smallM>0) {
		    if ((largeM % smallM)==0)
			return new ConstantModuloN((largeN+smallN) % smallM,
						   smallM);
		} else {
		    if (largeM>0)
			return new ConstantModuloN(mymod(largeN+smallN,largeM),
						   largeM);
		    else return new Constant(largeN+smallN);
		}
		return Value.UNKNOWN;
	    }
	    protected Value _mul(Value v) {
		// handles v=BOTTOM, v=UNKNOWN, v=CONSTANTMODULON
		if (v==Value.BOTTOM) return v;
		if (v==Value.UNKNOWN) {
		    // UNKNOWN*(0 mod b) = 0 mod b
		    // UNKNOWN*(a mod b) = 0 mod gcd(a,b)
		    if (this.offset==0)
			return new ConstantModuloN(0, this.modulus);
		    long mod = Util.gcd(this.offset, this.modulus);
		    if (mod>1)
			return new ConstantModuloN(0, mod);
		    return Value.UNKNOWN;
		}
		ConstantModuloN cmn = (ConstantModuloN) v;
		// (a mod b) * (c mod d) = (ac) mod gdb(b, d)
		Util.assert(cmn.modulus>1 && this.modulus>1);
		long mod = Util.gcd(this.modulus, cmn.modulus);
		if (mod>1)
		    return new ConstantModuloN
			((this.offset*cmn.offset) % mod, mod);
		else return Value.UNKNOWN;
	    }
	    Value negate() {
		return new ConstantModuloN(modulus-offset, modulus);
	    }
	    public boolean equals(Object o) {
		try {
		    ConstantModuloN cmn = (ConstantModuloN) o;
		    return this.offset == cmn.offset &&
			this.modulus == cmn.modulus;
		} catch (ClassCastException cce) { return false; }
	    }
	    public int hashCode() { return (int)offset + 7*(int)modulus; }
	    public String toString() {
		return offset+((modulus==0)?"":(" mod "+modulus));
	    }
	}
	static class Constant extends ConstantModuloN {
	    protected int specificity() { return 3; }
	    Constant(long offset) { super(offset); }
	    Value unify(Value v) {
		if (!(v instanceof Constant)) return super.unify(v);
		// Constant is common superclass of this and v.
		Constant c = (Constant) v;
		if (this.offset==c.offset)
		    return new Constant(this.offset);
		long small=Math.min(this.offset, c.offset);
		long large=Math.max(this.offset, c.offset);
		long mod=(large-small);
		Util.assert(mymod(small, mod)==mymod(large, mod));
		if (mod>1) return new ConstantModuloN(mymod(small, mod), mod);
		return Value.UNKNOWN;
	    }
	    protected Value _mul(Value v) {
		// must handle BOTTOM, UNKNOWN, CONSTANTMODULON, CONSTANT
		if (v==Value.BOTTOM) return v;
		if (v==Value.UNKNOWN) {
		    // UNKNOWN * c = 0 mod c
		    if (this.offset>0)
			return new ConstantModuloN(0, this.offset);
		    if (this.offset<0)
			return new ConstantModuloN(0, -this.offset).negate();
		    return Value.UNKNOWN;
		}
		ConstantModuloN cmn = (ConstantModuloN) v;
		if (cmn.modulus>1) {
		    long mult = Math.abs(this.offset);
		    Value r = new ConstantModuloN(cmn.offset*mult,
						  cmn.modulus*mult);
		    return (this.offset>=0) ? r : r.negate();
		}
		Util.assert(cmn.modulus==0 && this.modulus==0);
		return new Constant(cmn.offset * this.offset);
	    }
	    Value negate() { return new Constant(-this.offset); }
	}	
	static class UnknownOffset extends Value {
	    protected int specificity() { return 4; }
	    final DefPoint def;
	    UnknownOffset(DefPoint def) { this.def=def; }
	    boolean isBaseKnown() { return true; }
	    Value unify(Value v) {
		if (!(v instanceof UnknownOffset)) return super.unify(v);
		// UnknownOffset is common superclass of this and v
		UnknownOffset uo = (UnknownOffset) v;
		if (this.def.equals(uo.def))
		    return new UnknownOffset(def);
		else return Value.BOTTOM;
	    }
	    protected Value _add(Value v) {
		// must handle BOTTOM, UNKNOWN, CMN, C, UO.
		if (v instanceof UnknownConstant) return this;
		return Value.BOTTOM;
	    }
	    protected Value _mul(Value v) {
		// must handle BOTTOM, UNKNOWN, CMN, C, UO.
		return Value.BOTTOM;
	    }
	    Value negate() { return Value.BOTTOM; }

	    public boolean equals(Object o) {
		try {
		    UnknownOffset uo = (UnknownOffset) o;
		    return this.def.equals(uo.def);
		} catch (ClassCastException cce) { return false; }
	    }
	    public int hashCode() { return def.hashCode(); }
	    public String toString() { return def+"+X"; }
	}
	static class KnownOffset extends UnknownOffset {
	    protected int specificity() { return 5; }
	    final ConstantModuloN offset;
	    KnownOffset(DefPoint def, long offset) {
		// special case
		this(def, new Constant(offset));
	    }
	    KnownOffset(DefPoint def, ConstantModuloN offset) {
		super(def); this.offset = offset;
	    }
	    boolean isOffsetKnown() { return true; }
	    Value unify(Value v) {
		if (!(v instanceof KnownOffset)) return super.unify(v);
		// KnownOffset is common superclass of this and v
		KnownOffset ko = (KnownOffset) v;
		if (this.def.equals(ko.def)) {
		    Value cc = this.offset.unify(ko.offset);
		    if (cc instanceof ConstantModuloN)
			return new KnownOffset(def, (ConstantModuloN) cc);
		    else return new UnknownOffset(def);
		}
		return Value.BOTTOM;
	    }
	    protected Value _add(Value v) {
		// must handle BOTTOM, UNKNOWN, CMN, C, UO, KO
		if (!(v instanceof UnknownConstant)) return Value.BOTTOM;
		Value cc = this.offset.add(v);
		if (cc instanceof ConstantModuloN)
		    return new KnownOffset(def, (ConstantModuloN) cc);
		else return new UnknownOffset(def);
	    }
	    public boolean equals(Object o) {
		try {
		    KnownOffset ko = (KnownOffset) o;
		    return super.equals(ko) && this.offset.equals(ko.offset);
		} catch (ClassCastException cce) { return false; }
	    }
	    public int hashCode() {
		return super.hashCode()+7*offset.hashCode();
	    }
	    public String toString() { return def+"+"+offset; }
	}
	// utility function
	/** returns the positive value of a mod b, even when a is negative. */
	static long mymod(long a, long b) {
	    Util.assert(b>0);
	    long r = a % b;
	    return (a>=0 || r==0) ? r : (b+r);
	}
	/*------------------------------------------------------------- */
	/* both pointers in temps and pointer constants can be def points */
	abstract class DefPoint {
	    abstract HClass type();
	    final boolean isWellTyped() {
		HClass hc=type();
		return hc!=null && !hc.isPrimitive();
	    }
	}
	class TempDefPoint extends DefPoint {
	    final TEMP base; final Stm def;
	    TempDefPoint(TEMP base, Stm def) { this.base=base; this.def=def; }
	    HClass type() { return td.typeMap(base); }
	    public boolean equals(Object o) {
		return o instanceof TempDefPoint &&
		    base.temp.equals(((TempDefPoint)o).base.temp) &&
		    def.equals(((TempDefPoint)o).def);
	    }
	    public int hashCode() {
		return base.temp.hashCode() ^ def.hashCode();
	    }
	    public String toString() { return base.temp.toString(); }
	}
	class NameDefPoint extends DefPoint {
	    final NAME name;
	    NameDefPoint(NAME name) { this.name = name; }
	    public boolean equals(Object o) {
		return o instanceof NameDefPoint &&
		    name.label.equals(((NameDefPoint)o).name.label);
	    }
	    HClass type() { return td.typeMap(name); }
	    public int hashCode() { return name.label.hashCode(); }
	    public String toString() { return name.label.toString(); }
	}
    }
    
    /*----------------------------------------------------------------*/

    /** defines the properties of cache-equivalence sets */
    static class CacheEquivSet {
	public final MEM first;
	public final Set others = new HashSet();
	public CacheEquivSet(MEM mem) { this.first = mem; }
    }
    final Map cache_equiv = new HashMap();

    /** Returns the number of memory operations which share the same
     *  tag as this memory operation.  1 indicates no sharing possible. */
    public int num_using_this_tag(MEM mem) {
	return ((CacheEquivSet) cache_equiv.get(mem)).others.size() + 1;
    }
    /** Returns 'true' if this operation requires a tag check.  If
     *  ops_using_this_tag(mem) is also true, then you should store the
     *  result of the tag check some where for further use. */
    public boolean needs_tag_check(MEM mem) {
	return whose_tag_check(mem) == mem;
    }
    /** Returns the MEM operation which should have stored the
     *  necessary tag information for this MEM operation. */
    public MEM whose_tag_check(MEM mem) {
	return ((CacheEquivSet) cache_equiv.get(mem)).first;
    }
    /** Returns all the MEM operations which use the tag defined
     *  by whose_tag_check(mem) */
    public Set ops_using_this_tag(MEM mem) {
	return Collections.unmodifiableSet
	    (((CacheEquivSet) cache_equiv.get(mem)).others);
    }
}
