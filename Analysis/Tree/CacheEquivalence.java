// CacheEquivalence.java, created Wed Jun  6 15:06:02 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.DomTree;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.Maps.Derivation.DList;
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
 * @version $Id: CacheEquivalence.java,v 1.1.2.4 2001-06-12 21:27:50 cananian Exp $
 */
public class CacheEquivalence {
    private static final boolean DEBUG=false;

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
	/*------------------*/
	DomTree dt = new DomTree(code, cfg, false);
	/* zip down through dominator tree, collecting info */
	Environment e = new HashEnvironment();
	HCodeElement[] roots = dt.roots();
	for (int i=0; i<roots.length; i++)
	    traverseDT((Stm) roots[i], dt, e, td);
	/* okay, done with analysis! */
    }
    /* analyze stms travelling down the dominator tree */
    void traverseDT(Stm stm, DomTree dt, Environment e, TreeDerivation td) {
	/* save environment */
	Environment.Mark mark = e.getMark();
	/* do analysis */
	analyze(stm, e, td);
	/* recurse */
	HCodeElement[] child = dt.children(stm);
	for (int i=0; i<child.length; i++)
	    traverseDT((Stm)child[i], dt, e, td);
	/* restore environment */
	e.undoToMark(mark);
	/* done! */
	return;
    }
    /* analyze one statement in the environment defined by map m */
    void analyze(Stm stm, Map pre, TreeDerivation td) {
	Map post = new HashMap();
	/* first look for all *reads* */
	/* There is NO ORDER defined for any of these. */
	for (ExpList el=stm.kids(); el!=null; el=el.tail) {
	    add(el.head, pre, post, true, td);
	}
	/* now all post mappings get added to pre */
	pre.putAll(post); post.clear();
	/* now look for writes (which must happen after reads) */
	if (stm.kind()==TreeKind.MOVE)
	    add(((MOVE)stm).getDst(), pre, post, false, td);
	pre.putAll(post); post.clear();
    }
    void add(Exp e, Map pre, Map post, boolean recurse, TreeDerivation td) {
	if (e.kind()==TreeKind.MEM) {
	    MEM mem = (MEM) e;  Exp memexp = mem.getExp();
	    /* three cases: 1) a temp, 2) derivation from a temp,
	     * 3) a random temporary value. */
	    Temp t = null;
	    if (memexp.kind()==TreeKind.TEMP) { /* case 1 */
		t = ((TEMP)memexp).temp;
	    } else if (td.typeMap(memexp)==null) { /* case 2 */
		DList dl = td.derivation(memexp);
		if (dl.next==null && dl.sign)
		    t = dl.base;
	    }
	    if (t!=null) {
		CacheEquivSet ces = (CacheEquivSet) pre.get(t);
		if (ces==null) ces = new CacheEquivSet(mem);
		else ces.others.add(mem);
		cache_equiv.put(mem, ces);
		post.put(t, ces);
	    } else { /* case 3 */
		cache_equiv.put(mem, new CacheEquivSet(mem));
	    }
	}
	if (recurse)
	    for (Tree tp=e.getFirstChild(); tp!=null; tp=tp.getSibling())
		add((Exp)tp, pre, post, recurse, td);
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
		    ws.add(s);
		}
		/* for each element on the worklist... */
		while (!ws.isEmpty()) {
		    Stm s = (Stm) ws.pop();
		    s.accept(this);
		}
	    }
	    public void update(Temp t, Stm def, Value v) {
		List pair = Default.pair(t, def);
		Value old = (Value) valueMap.put(pair, v);
		if (old==null || !old.equals(v))
		    //put uses of t on the workset.
		    for (Iterator it = uses.getValues(pair).iterator();
			 it.hasNext(); )
			ws.add((Stm)it.next());
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
		    if (v.isBaseKnown() &&
			((UnknownOffset)v).def.isWellTyped())
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
	 * with known and unknown offsets. */
	static abstract class Value {  // bottom/noinfo.
	    boolean isBaseKnown() { return false; }
	    boolean isOffsetKnown() { return false; }
	    abstract Value unify(Value v);
	    abstract Value add(Value v);
	    abstract Value negate();
	    static final Value BOTTOM = new Value() {
		Value unify(Value v) { return BOTTOM; }
		Value add(Value v) { return BOTTOM; }
		Value negate() { return BOTTOM; }
		public String toString() { return "BOTTOM"; }
	    };
	    static final Value NOINFO = new Value() {
		Value unify(Value v) { return v; }
		Value add(Value v) { return NOINFO; }
		Value negate() { return NOINFO; }
		public String toString() { return "NO INFO"; }
	    };
	}
	static class Constant extends Value {
	    final long offset;
	    Constant(long offset) { this.offset = offset; }
	    boolean isOffsetKnown() { return true; }
	    Value unify(Value v) {
		if (this.equals(v)) return this;
		else return Value.BOTTOM;
	    }
	    Value add(Value v) {
		if (v instanceof Constant)
		    return new Constant(this.offset + ((Constant)v).offset);
		else return v.add(this);
	    }
	    Value negate() { return new Constant(-this.offset); }
	    public boolean equals(Object o) {
		try {
		    Constant c = (Constant) o;
		    return this.offset == c.offset;
		} catch (ClassCastException cce) { return false; }
	    }
	    public int hashCode() { return (int) offset; }
	    public String toString() { return ""+offset; }
	}	
	static class UnknownOffset extends Value {
	    final DefPoint def;
	    UnknownOffset(DefPoint def) { this.def=def; }
	    boolean isBaseKnown() { return true; }
	    Value unify(Value v) {
		if (this.equals(v)) return this;
		else return Value.BOTTOM;
	    }
	    Value add(Value v) {
		if (v instanceof Constant) return this;
		if (v instanceof KnownOffset) return Value.BOTTOM;
		else return v.add(this);
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
	    final long offset;   // constant offset from base pointer, if known
	    KnownOffset(DefPoint def, long offset) {
		super(def); this.offset = offset;
	    }
	    boolean isOffsetKnown() { return true; }
	    Value unify(Value v) {
		if (this.equals(v)) return this;
		if (super.equals(v)) return new UnknownOffset(def);
		return Value.BOTTOM;
	    }
	    Value add(Value v) {
		if (v instanceof Constant)
		    return new KnownOffset(def, offset+((Constant)v).offset);
		if (v instanceof KnownOffset) return Value.BOTTOM;
		else return v.add(this);
	    }
	    public boolean equals(Object o) {
		try {
		    KnownOffset ko = (KnownOffset) o;
		    return super.equals(ko) && this.offset == ko.offset;
		} catch (ClassCastException cce) { return false; }
	    }
	    public int hashCode() { return super.hashCode() + (int) offset; }
	    public String toString() { return def+"+"+offset; }
	}
	/*------------------------------------------------------------- */
	/* both pointers in temps and pointer constants can be def points */
	abstract class DefPoint {
	    abstract boolean isWellTyped();
	}
	class TempDefPoint extends DefPoint {
	    final TEMP base; final Stm def;
	    TempDefPoint(TEMP base, Stm def) { this.base=base; this.def=def; }
	    boolean isWellTyped() {
		HClass hc = td.typeMap(base);
		return (hc!=null) && (!hc.isPrimitive());
	    }
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
	    boolean isWellTyped() {
		HClass hc = td.typeMap(name);
		return (hc!=null) && (!hc.isPrimitive());
	    }
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
