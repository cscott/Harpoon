// AlignmentAnalysis.java, created Thu Mar 14 01:19:55 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
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
import harpoon.Util.Collections.Environment;
import harpoon.Util.Collections.HashEnvironment;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>AlignmentAnalysis</code> computes the alignment
 * (some offset modulo some number from some base) of every
 * typed pointer in a Tree.  It is a dataflow analysis.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AlignmentAnalysis.java,v 1.1.2.2 2002-03-15 16:32:48 cananian Exp $
 */
public class AlignmentAnalysis {
    
    final ReachingDefs<Tree> rd;
    final TreeDerivation td;
    /** Creates a <code>AlignmentAnalysis</code>. */
    public AlignmentAnalysis(Code c, CFGrapher<Tree> cfg, UseDefer<Tree> udr,
			     TreeDerivation td) {
	this.rd = new ReachingDefsAltImpl<Tree>(c, cfg, udr);
	this.td = td;
	new StmVisitor(c, cfg, udr);
    }
    /* dataflow result accessor functions */
    public Value valueOf(Exp e, Stm root) {
	return new ValueVisitor(e, root).value;
    }
    /* temp x stm-to-value mapping */
    private Map<Map.Entry<Temp,Stm>,Value> valueMap =
	new HashMap<Map.Entry<Temp,Stm>,Value>();
    public Value valueUseAt(Temp t, Stm s) {
	Value v = Value.NOINFO;
	Set<Tree> rdset=rd.reachingDefs(s, t);
	for (Iterator<Tree> it=rdset.iterator(); it.hasNext(); )
	    v = v.unify(valueDefAt(t, (Stm)it.next()));
	return v.fillKGroup(rdset, t);
    }
    public Value valueDefAt(Temp t, Stm s) {
	Value v = valueMap.get(Default.entry(t,s));
	return (v==null) ? Value.NOINFO : v;
    }
    /** visitor class to do dataflow through statements */
    private class StmVisitor extends TreeVisitor {
	MultiMap<Map.Entry<Temp,Stm>,Stm> uses =
	    new GenericMultiMap<Map.Entry<Temp,Stm>,Stm>();
	WorkSet<Stm> ws = new WorkSet<Stm>();
	StmVisitor(Code c, CFGrapher<Tree> cfg, UseDefer<Tree> udr) {
	    /* create uses map & add all elements to worklist. */
	    for (Iterator<Tree> it=cfg.getElements(c).iterator();
		 it.hasNext(); ) {
		Stm s = (Stm) it.next();
		for (Iterator<Temp> it2=udr.useC(s).iterator();
		     it2.hasNext(); ) {
		    Temp u = it2.next();
		    for (Iterator<Tree> it3=rd.reachingDefs(s, u).iterator();
			 it3.hasNext(); ) {
			Stm d = (Stm) it3.next();
			uses.add(Default.entry(u,d), s);
		    }
		}
		// add all statements s to worklist.
		ws.addLast(s);
	    }
	    /* for each element on the worklist... */
	    while (!ws.isEmpty()) {
		Stm s = ws.removeFirst();
		s.accept(this);
	    }
	}
	public void update(Temp t, Stm def, Value v) {
	    assert t!=null && def!=null && v!=null;
	    Map.Entry<Temp,Stm> pair = Default.entry(t, def);
	    Value old = valueMap.put(pair, v);
	    if (old==null || !old.equals(v))
		//put uses of t on the workset.
		for (Iterator<Stm> it = uses.getValues(pair).iterator();
		     it.hasNext(); )
		    ws.addLast(it.next());
	}
	public void visit(Tree e) { assert false; }
	public void visit(Stm s) { /* no defs */ }
	public void visit(INVOCATION s) {
	    TEMP t = s.getRetval();
	    if (t!=null)
		update(t.temp, s,
		       new BaseAndOffset(new TempDefPoint(t, s), 0));
	}
	public void visit(CALL s) {
	    visit((INVOCATION)s);
	    TEMP t = s.getRetex();
	    update(t.temp, s,
		   new BaseAndOffset(new TempDefPoint(t, s), 0));
	}
	public void visit(METHOD s) {
	    for (int i=0; i<s.getParamsLength(); i++) {
		TEMP t = s.getParams(i);
		update(t.temp, s,
		       new BaseAndOffset(new TempDefPoint(t, s), 0));
	    }
	}
	public void visit(MOVE s) {
	    if (s.getDst().kind()==TreeKind.TEMP) {
		TEMP t = (TEMP) s.getDst();
		Value v = valueOf(s.getSrc(), s);
		if (s.getDst().type()==Type.POINTER &&
		    v.isBaseKnown() &&
		    !((BaseAndOffset)v).def.isWellTyped())
		    update(t.temp, s,
			   new BaseAndOffset(new TempDefPoint(t, s),0));
		else {
		    // if kgroup is unset, fill it now.
		    update(t.temp, s, v.fillKGroup(s, t.temp));
		}
	    }
	}
    }
    /** visitor class to compute Values of expression trees. */
    private class ValueVisitor extends TreeVisitor {
	final Stm root;
	Value value;
	ValueVisitor(Exp e, Stm root) {
	    this.root=root;
	    this.value= (e.type()==Type.INT || e.type()==Type.LONG)
		? Value.SOMEINT : Value.BOTTOM;
	    e.accept(this);
	}
	public void visit(Tree e) { assert false; }
	public void visit(CONST c) {
	    if (c.type()==Type.INT || c.type()==Type.LONG)
		this.value = new Constant(c.value().longValue());
	}
	public void visit(ESEQ e) {
	    this.value=valueOf(e.getExp(), root);
	}
	public void visit(MEM e) { /* bottom */ }
	public void visit(NAME e) {
	    this.value = new BaseAndOffset(new NameDefPoint(e), 0);
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
		    new Constant(1<<((Constant)right).number) :
		    Value.SOMEINT;
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
    public static abstract class Value {  // bottom/noinfo.
	abstract protected int specificity();
	public boolean isBaseKnown() { return false; }
	public boolean isOffsetKnown() { return false; }
	Value fillKGroup(Set defs, Temp t) { return this; }
	final Value fillKGroup(Stm def, Temp t) {
	    return fillKGroup(Collections.singleton(def), t);
	}
	Value unify(Value v) {
	    assert this!=NOINFO;
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
	static final Value SOMEINT = new IntegerValue();
	static final Value NOINFO = new Value() {
		protected int specificity() { return 5; }
		Value unify(Value v) { return v; }
		protected Value _add(Value v) { return NOINFO; }
		protected Value _mul(Value v) { return NOINFO; }
		Value negate() { return NOINFO; }
		public String toString() { return "NO INFO"; }
	    };
    }
    public static class IntegerValue extends Value {
	protected int specificity() { return 1; }
	IntegerValue() {
	    assert this instanceof ConstantModuloN ||
		Value.SOMEINT==null;
	}
	Value unify(Value v) {
	    if (!(v instanceof IntegerValue)) return super.unify(v);
	    // IntegerValue is common superclass of this and v.
	    return Value.SOMEINT;
	}
	protected Value _add(Value v) {
	    // must handle v=BOTTOM, v=SOMEINT
	    if (v instanceof IntegerValue) return Value.SOMEINT;
	    return Value.BOTTOM;
	}
	protected Value _mul(Value v) {
	    // must handle v=BOTTOM, v=SOMEINT
	    if (v instanceof IntegerValue) return Value.SOMEINT;
	    return Value.BOTTOM;
	}
	Value negate() { return this; }
	public String toString() { return "X"; }
    }
    public static class ConstantModuloN extends IntegerValue {
	protected int specificity() { return 2; }
	public final long number; public final long modulus;
	public final KGroup kgroup;
	ConstantModuloN(long number, long modulus, KGroup kgroup) {
	    this.number=number; this.modulus=modulus; this.kgroup=kgroup;
	    assert modulus>1;
	    assert kgroup!=null || (number>=0 && number<modulus);
	}
	protected ConstantModuloN(long number) {
	    assert this instanceof Constant;
	    this.number=number; this.modulus=0; this.kgroup=null;
	}
	public boolean isOffsetKnown() { return true; }
	Value fillKGroup(Set defs, Temp t) {
	    if (kgroup!=null || this instanceof Constant)
		return this;
	    return new ConstantModuloN(mymod(number, modulus),modulus,
				       new KGroup(defs, t));
	}
	Value unify(Value v) {
	    if (!(v instanceof ConstantModuloN)) return super.unify(v);
	    // preserve k-group equality if this==v
	    if (this.equals(v)) return this;
	    // ConstantModuloN is common superclass of this and v.
	    ConstantModuloN large, small;
	    if (this.modulus > ((ConstantModuloN)v).modulus) {
		large=this; small=(ConstantModuloN)v;
	    } else {
		small=this; large=(ConstantModuloN)v;
	    }
	    assert large.modulus>0;
	    long off=0, mod=1;
	    if ((small.modulus==0 || small.modulus==large.modulus) &&
		mymod(small.number, large.modulus) ==
		mymod(large.number, large.modulus)) {//(a mod b) join c
		off=large.number; mod=large.modulus;
	    } else if (small.modulus > 0) { // (a mod b) join (c mod d)
		mod=Util.gcd(large.modulus, small.modulus);
		if (large.number!=small.number)
		    mod=Util.gcd(mod, Math.abs(large.number-small.number));
		off=small.number;
	    } // others?
	    if (mod>1)
		return new ConstantModuloN(mymod(off,mod), mod, null);
	    // can't unify the constants.
	    return Value.SOMEINT;
	}
	protected Value _add(Value v) {
	    // must handle v=BOTTOM, SOMEINT, CONSTANTMODULON, CONSTANT
	    //      and this=CONSTANTMODULON, CONSTANT
	    if (v==Value.BOTTOM) return v;
	    if (v==Value.SOMEINT) return Value.SOMEINT;
	    ConstantModuloN large, small;
	    if (this.modulus > ((ConstantModuloN)v).modulus) {
		large=this; small=(ConstantModuloN)v;
	    } else {
		small=this; large=(ConstantModuloN)v;
	    }
	    if (small.modulus>0) {// (a mod b)+(c mod d)
		if (large.kgroup==small.kgroup && large.kgroup!=null)
		    // (a+k*b)+(c+k*d) = (a+c)+k*(b+d)
		    return new ConstantModuloN
			(large.number+small.number,
			 large.modulus+small.modulus, large.kgroup);
		// (a mod b)+(c mod d)=(a+c) mod gcd(b, d)
		long mod = Util.gcd(small.modulus, large.modulus);
		if (mod>1)
		    return new ConstantModuloN
			(mymod(large.number+small.number,mod), mod, null);
	    } else if (large.modulus>0) { // (a mod b)+c
		long off=large.number+small.number;
		if (large.kgroup==null) off=mymod(off, large.modulus);
		return new ConstantModuloN(off, large.modulus,
					   large.kgroup);
	    } else return new Constant(large.number+small.number);
	    return Value.SOMEINT;
	}
	protected Value _mul(Value v) {
	    // handles v=BOTTOM, v=SOMEINT, v=CONSTANTMODULON
	    if (v==Value.BOTTOM) return v;
	    if (v==Value.SOMEINT) {
		// SOMEINT*(0 mod b) = 0 mod b
		// SOMEINT*(a mod b) = 0 mod gcd(a,b)
		if (this.number==0)
		    return new ConstantModuloN(0, this.modulus, null);
		long mod = Util.gcd(this.number, this.modulus);
		if (mod>1)
		    return new ConstantModuloN(0, mod, null);
		return Value.SOMEINT;
	    }
	    ConstantModuloN cmn = (ConstantModuloN) v;
	    // (a mod b) * (c mod d) = (ac) mod gdb(b, d)
	    assert cmn.modulus>1 && this.modulus>1;
	    long mod = Util.gcd(this.modulus, cmn.modulus);
	    if (mod>1)
		return new ConstantModuloN
		    (mymod(this.number*cmn.number,mod), mod, null);
	    else return Value.SOMEINT;
	}
	Value negate() {
	    return new ConstantModuloN(mymod(modulus-number,modulus),
				       modulus, null/*k'=-k*/);
	}
	public boolean equals(Object o) {
	    if (!(o instanceof ConstantModuloN)) return false;
	    ConstantModuloN cmn = (ConstantModuloN) o;
	    return (this.kgroup==null ? cmn.kgroup==null :
		    (cmn.kgroup!=null &&
		     this.kgroup.equals(cmn.kgroup))) &&
		this.number == cmn.number &&
		this.modulus == cmn.modulus;
	}
	public int hashCode() { return (int)number + 7*(int)modulus; }
	public String toString() {
	    return number+((modulus==0)?"":(" mod "+modulus+" k:"+kgroup));
	}
    }
    public static class Constant extends ConstantModuloN {
	protected int specificity() { return 3; }
	Constant(long number) { super(number); }
	Value unify(Value v) {
	    if (!(v instanceof Constant)) return super.unify(v);
	    // Constant is common superclass of this and v.
	    Constant c = (Constant) v;
	    if (this.number==c.number)
		return new Constant(this.number);
	    long small=Math.min(this.number, c.number);
	    long large=Math.max(this.number, c.number);
	    long mod=(large-small);
	    assert mymod(small, mod)==mymod(large, mod);
	    if (mod>1)
		return new ConstantModuloN(mymod(small, mod), mod, null);
	    return Value.SOMEINT;
	}
	protected Value _mul(Value v) {
	    // must handle BOTTOM, SOMEINT, CONSTANTMODULON, CONSTANT
	    if (v==Value.BOTTOM) return v;
	    if (v==Value.SOMEINT) {
		// SOMEINT * c = 0 mod c
		if (this.number>1)
		    return new ConstantModuloN(0, this.number, null);
		if (this.number<-1)
		    return new ConstantModuloN(0, -this.number, null)
			.negate();
		return Value.SOMEINT;
	    }
	    ConstantModuloN cmn = (ConstantModuloN) v;
	    if (cmn.modulus>1) {
		long mult = Math.abs(this.number);
		Value r = new ConstantModuloN(cmn.number*mult,
					      cmn.modulus*mult,
					      cmn.kgroup);
		return (this.number>=0) ? r : r.negate();
	    }
	    assert cmn.modulus==0 && this.modulus==0;
	    return new Constant(cmn.number * this.number);
	}
	Value negate() { return new Constant(-this.number); }
    }
    public static class BaseAndOffset extends Value {
	protected int specificity() { return 4; }
	public final DefPoint def;
	public final IntegerValue offset;
	BaseAndOffset(DefPoint def, IntegerValue offset) {
	    this.def = def; this.offset = offset;
	}
	// special case:
	BaseAndOffset(DefPoint def, long offset) {
	    this(def, new Constant(offset));
	}
	public boolean isBaseKnown() { return true; }
	public boolean isOffsetKnown() { return offset.isOffsetKnown(); }
	Value fillKGroup(Set defs, Temp t) {
	    return new BaseAndOffset(def, (IntegerValue)
				     offset.fillKGroup(defs, t));
	}
	Value unify(Value v) {
	    if (!(v instanceof BaseAndOffset)) return super.unify(v);
	    // BaseAndOffset is common superclass of this and v
	    BaseAndOffset ko = (BaseAndOffset) v;
	    if (this.def.equals(ko.def)) {
		Value cc = this.offset.unify(ko.offset);
		if (cc instanceof IntegerValue)
		    return new BaseAndOffset(def, (IntegerValue) cc);
	    }
	    return Value.BOTTOM;
	}
	protected Value _add(Value v) {
	    // must handle BOTTOM, SOMEINT, CMN, C, BAO
	    if (!(v instanceof IntegerValue)) return Value.BOTTOM;
	    Value cc = this.offset.add(v);
	    assert cc instanceof IntegerValue;
	    return new BaseAndOffset(def, (IntegerValue) cc);
	}
	protected Value _mul(Value v) {
	    // must handle BOTTOM, SOMEINT, CMN, C, BAO.
	    return Value.BOTTOM;
	}
	Value negate() { return Value.BOTTOM; }
	public boolean equals(Object o) {
	    if (!(o instanceof BaseAndOffset)) return false;
	    BaseAndOffset bao = (BaseAndOffset) o;
	    return this.def.equals(bao.def) &&
		this.offset.equals(bao.offset);
	}
	public int hashCode() {
	    return def.hashCode()+7*offset.hashCode();
	}
	public String toString() { return def+"+"+offset; }
    }
    // utility function
    /** returns the positive value of a mod b, even when a is negative. */
    private static long mymod(long a, long b) {
	assert b>0;
	long r = a % b;
	return (r<0) ? (b+r) : r;
    }
    /*------------------------------------------------------------- */
    /* both pointers in temps and pointer constants can be def points */
    public abstract class DefPoint {
	public abstract HClass type();
	public final boolean isWellTyped() {
	    HClass hc=type();
	    return hc!=null && !hc.isPrimitive();
	}
    }
    public class TempDefPoint extends DefPoint {
	public final TEMP base; public final Stm def;
	TempDefPoint(TEMP base, Stm def) { this.base=base; this.def=def; }
	public HClass type() { return td.typeMap(base); }
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
    public class NameDefPoint extends DefPoint {
	public final NAME name;
	NameDefPoint(NAME name) { this.name = name; }
	public boolean equals(Object o) {
	    return o instanceof NameDefPoint &&
		name.label.equals(((NameDefPoint)o).name.label);
	}
	public HClass type() { return td.typeMap(name); }
	public int hashCode() { return name.label.hashCode(); }
	public String toString() { return name.label.toString(); }
    }

    /** a k-group is a "phi-function" definition point. */
    public static class KGroup {
	final Set defs; final Temp t;
	KGroup(Set defs, Temp t) { this.defs=defs; this.t=t; }
	public String toString() { return "<"+t+","+defs+">"; }
	public boolean equals(Object o) {
	    if (!(o instanceof KGroup)) return false;
	    KGroup kg = (KGroup) o;
	    return this.t.equals(kg.t) && this.defs.equals(kg.defs);
	}
	public int hashCode() { return t.hashCode() + 11*defs.hashCode(); }
    }
}
