// ResilientUnHandler.java, created Fri Jan 24 15:29:39 2003 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.Unreachable;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>ResilientUnHandler</code> replaces implicit exception
 * handling with explicit resilient exception handling and removes
 * <code>HANDLER</code> quads from the graph.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: ResilientUnHandler.java,v 1.2 2003-03-15 23:24:58 kkz Exp $ */
final class ResilientUnHandler {
    private static final boolean ARRAY_BOUNDS_CHECKS
	= !Boolean.getBoolean("harpoon.unhandler.noarraychecks");
    // entry point.
    public static final Quad unhandler(final QuadFactory qf, final Code code,
				       final ClassHierarchy ch,
				       boolean coalesce_exceptions) {
	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)code.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final HandlerMap hm = new HandlerMap(qf, old_method);
	final CloningTempMap ctm = new CloningTempMap(code.qf.tempFactory(),
						      qf.tempFactory());
	final StaticState ss = new StaticState(qf, qm, hm, ctm, ch,
					       coalesce_exceptions);
	visitAll(new Visitor(new TempInfo(), ss), old_header);
	// now qm contains mappings from old to new, we just have to link them.
	for (Iterator e = code.getElementsI(); e.hasNext(); ) {
	    Quad old = (Quad) e.next();
	    // link next.
	    Edge[] el = old.nextEdge();
	    for (int i=0; i<el.length; i++)
		Quad.addEdge(qm.getFoot((Quad)el[i].from()),el[i].which_succ(),
			     qm.getHead((Quad)el[i].to()), el[i].which_pred());
	}
	// fixup optimized instanceof.
	ss.iofm.fixup(ss);
	// fixup try blocks.
	Temp[] qMp = ((METHOD)qm.getHead(old_method)).params();
	final METHOD qM = new METHOD(qf, old_method, qMp,
				     1 /* no HANDLERS any more */);
	final HEADER qH = (HEADER)qm.getHead(old_header);
	Quad.addEdge(qH, 1, qM, 0);
	Edge e = old_method.nextEdge(0);
	Quad.addEdge(qM, 0, qm.getHead((Quad)e.to()), e.which_pred());
	hm.fixup(qf, qH, qm, ss.extra(0), ctm);
	// return new header.
	return qH;
    }
    /** Recursively visit all quads starting at <code>start</code>. */
    private static final void visitAll(Visitor v, Quad start) {
	start.accept(v);
	final StaticState ss = v.ss;
	assert ss.qm.contains(start);
	Quad[] ql = start.next();
	for (int i=0; i<ql.length; i++) {
	    if (ss.qm.contains(ql[i])) continue; // skip if already done.
	    Visitor vv = (ql.length==1)? v : // don't clone if never reused
		new Visitor((TempInfo) v.ti.clone(), ss);
	    visitAll(vv, ql[i]);
	}
    }
	
    // type information //////////////////
    /** Base class for type information. */
    private static abstract class Type {
	// explicit methods to replace "bad" instanceofs.
	boolean isTop() { return false; }
	boolean isNonNull()    { return false; }
	boolean isIntConst()   { return false; }
	boolean isFixedArray() { return false; }
	// explicit accessors to avoid typecasting.
	int getArrayLength() { throw new Error("Not a FixedArray."); }
	int getConstValue() { throw new Error("Not an IntConst."); }
	// debugging
	public abstract String toString();
	// static types for convenience/efficiency.
	static final Type top=new Top();
	static final Type nonnull=new NonNull();
    }
    private static class Top extends Type {
	boolean isTop() { return true; }
	public String toString() { return "Top"; }
    }
    private static class NonNull extends Type {
	boolean isNonNull() { return true; }
	public String toString() { return "NonNull"; }
    }
    private static class IntConst extends Type {
	final int value;
	IntConst(int value) { this.value = value; }
	boolean isIntConst() { return true; }
	int getConstValue() { return value; }
	public String toString() { return "IntConst("+value+")"; }
    }
    private static class FixedArray extends NonNull {
	final int length;
	FixedArray(int length) { this.length = length; }
	boolean isFixedArray() { return true; }
	int getArrayLength() { return length; }
	public String toString() { return "FixedArray("+length+")"; }
    }

    /** Mapping of temps to information known about their values at a
     *  given program point. */
    private static class TempInfo implements Cloneable {
	private static class AliasList {
	    static class TypeBox {
		Type type;
		TypeBox(Type type) { this.type = type; }
	    }
	    Temp temp;
	    TypeBox box;
	    AliasList prevAlias, nextAlias; // circular list
	    AliasList(Temp temp, Type ty) {
		this.temp = temp; this.box=new TypeBox(ty);
		this.prevAlias=this.nextAlias=this; // its own alias.
	    }
	}
	final private Map h = new HashMap();
	public TempInfo() { /* do nothing */ }
	private TempInfo(Map h) {
	    // copy types
	    for (Iterator it=h.values().iterator(); it.hasNext(); ) {
		AliasList al = (AliasList) it.next();
		this.put(al.temp, al.box.type);
	    }
	    // and copy aliases
	    for (Iterator it=h.values().iterator(); it.hasNext(); ) {
		AliasList al = (AliasList) it.next();
		if (al.prevAlias!=null)
		    this.createAlias(al.temp, al.prevAlias.temp);
	    }
	}

	/** Get the type of a given temp. */
	public Type get(Temp t) { 
	    AliasList al = (AliasList) h.get(t);
	    return (al==null) ? Type.top : al.box.type;
	}
	/** Put a type for a temp, breaking any alias to the temp. */
	public void put(Temp t, Type ty) {
	    breakAlias(t); update(t, ty);
	}
	/** Update a type for a temp, updating all aliases as well. */
	public void update(Temp t, Type ty) {
	    // all aliases also get the type.
	    if (ty==null) ty=Type.top;
	    AliasList al = (AliasList) h.get(t);
	    // if top and no aliases...
	    if (ty.isTop() && (al==null || al.prevAlias==al))
		h.remove(t); // ...save memory.
	    else if (al==null) // no previous type.
		h.put(t, new AliasList(t, ty));
	    else al.box.type = ty; // update type for all aliases at once.
	}
	/** Process a move. Type of dst becomes that of src. */
	public void doMove(Temp dst, Temp src) {
	    put(dst, get(src)); createAlias(dst, src);
	}
	/** Break an alias.  Type of target becomes independent. */
	private void breakAlias(Temp t) {
	    AliasList al = (AliasList) h.get(t);
	    if (al==null || al.prevAlias==al) return; // no aliases.
	    // remove from circular list.
	    al.prevAlias.nextAlias = al.nextAlias;
	    al.nextAlias.prevAlias = al.prevAlias;
	    al.nextAlias=al.prevAlias=al;
	    // and give it a type box of its own.
	    al.box = new AliasList.TypeBox(al.box.type);
	}
	/** Create an alias between (all aliases of) dst and (all aliases of)
	 *  src.  The type of (all aliases of) dst becomes that of src. */
	public void createAlias(Temp dst, Temp src) {
	    AliasList d0 = (AliasList) h.get(dst);
	    AliasList s0 = (AliasList) h.get(src);
	    if (d0==null) h.put(dst, d0=new AliasList(dst, Type.top));
	    if (s0==null) h.put(src, s0=new AliasList(src, Type.top));
	    // if they're already aliases we're already done.
	    if (d0.box==s0.box) return;
	    // all boxes in dst must point to src.
	    AliasList al=d0;
	    do {
		al.box=s0.box;
		al=al.nextAlias;
	    } while (al!=d0);
	    // now join the circular lists.
	    AliasList d1 = d0.nextAlias;
	    AliasList s1 = s0.nextAlias;
	    d0.nextAlias=s1; s1.prevAlias=d0;
	    s0.nextAlias=d1; d1.prevAlias=s0;
	    assert s0.box==d0.box && s1.box==d1.box;
	}
	/** Clear all type information (ie at program merge points) */
	public void clear() { h.clear(); }
	/** Clone the temp info (ie at program splits) */
	public Object clone() { return new TempInfo(h); }
    }

    /** mapping from old quads to new quads. */
    private static class QuadMap {
	final private Map h = new HashMap();
	void put(Quad old, Quad new_header, Quad new_footer) {
	    h.put(old, new Quad[] { new_header, new_footer });
	}
	Quad getHead(Quad old) {
	    Quad[] ql=(Quad[])h.get(old); return (ql==null)?null:ql[0];
	}
	Quad getFoot(Quad old) {
	    Quad[] ql=(Quad[])h.get(old); return (ql==null)?null:ql[1];
	}
	boolean contains(Quad old) { return h.containsKey(old); }
    }
    /** HANDLER information. */
    private static final class HandlerMap {
	/** source of HANDLER info */
	final private METHOD oldQm;
	/** input exception temp for every handlerset */
	final Temp Tex;

	/** Constructor. */
	HandlerMap(QuadFactory qf, METHOD oldQm) {
	    this.oldQm = oldQm;
	    this.Tex = new Temp(qf.tempFactory(), "exc_");
	    // make entries for all HANDLERs.
	    Quad[] ql = oldQm.next();
	    for (int i=1; i<ql.length; i++)
		get(Hhandler, (HANDLER)ql[i]);
	}

	/** mapping <Exception, HandlerSet> -> fixup list for 
	 *  exception construction code */
	List registry(HClass HCex, HandlerSet hs) {
	    return get(Hehs, Arrays.asList(new Object[] { HCex, hs }));
	}
	private final Map Hehs = new HashMap();

	/** mapping HandlerSet -> test tree */
	void register(NOP from, HandlerSet to) {
	    get(Hhs, to).add(from);
	}
	private final Map Hhs = new HashMap();

	/** lonely throws with null handlersets. */
	void register(THROW q) { Vthrows.add(q); }
	private final List Vthrows = new ArrayList();

	/** HANDLER->Vector.  Later, link all NOPs in Vector to PHI. */
	private void register(NOP from, HANDLER to) {
	    get(Hhandler, to).add(from);
	}
	private final Map Hhandler = new HashMap();

	final HandlerSet handlers(Quad q) {
	    Quad[] ql = oldQm.next();
	    HandlerSet hs = null;
	    for (int i=ql.length-1; i > 0; i--) // element 0 not a HANDLER
		if (((HANDLER)ql[i]).isProtected(q))
		    hs = new HandlerSet((HANDLER)ql[i], hs);
	    return hs;
	}

	/** Link registered HANDLER and HandlerSet edges. */
	final void fixup(QuadFactory qf, HEADER newQh,
			 QuadMap qm, Temp Textra, CloningTempMap ctm) {
	    METHOD newQm = (METHOD) newQh.next(1);

	    // share exception-generation code
	    for (Iterator it=Hehs.values().iterator(); it.hasNext(); ) {
		List l = (List) it.next();
		if (l.size() < 2) continue; // no fixup necessary.
		PHI phi = new PHI(qf, (Quad)l.get(0), new Temp[0], l.size());
		Edge ed = ((NEW)l.get(0)).prevEdge(0);
		Quad.addEdge(phi, 0, (Quad)ed.to(), ed.which_pred());
		for (int i=0; i<l.size(); i++) {
		    if (i>0) ed = ((NOP)l.get(i)).prevEdge(0);
		    Quad.addEdge((Quad)ed.from(), ed.which_succ(), phi, i);
		}
	    }
	    
	    // next do HandlerSets
	    for (Iterator e=Hhs.keySet().iterator(); e.hasNext(); ) {
		HandlerSet hs = (HandlerSet)e.next();
		List v = get(Hhs, hs);
		assert v.size()>0; // should be!
		PHI phi = new PHI(qf, (Quad)v.get(0),
				  new Temp[0], v.size() /*arity*/);
		// link all to in of phi
		for (int i=0; i<v.size(); i++) {
		    Edge ed = ((NOP)v.get(i)).prevEdge(0);
		    Quad.addEdge((Quad)ed.from(), ed.which_succ(), phi, i);
		}
		// now build instanceof tree. exception in Tex.
		if (hs==null) { // this is common THROW for no-handler case
		    THROW q0 = new THROW(qf, phi, Tex);
		    Quad.addEdge(phi, 0, q0, 0);
		    register(q0);
		} else { // create TypeSwitch using each handler in handlerset
		    /* count exception types and create keys array */
		    ArrayList al = new ArrayList();
		    for (Iterator ee=hs.iterator(); ee.hasNext(); ) {
			HClass HCex = ((HANDLER)ee.next()).caughtException();
			if (HCex==null) break; // this is the 'catch any' case.
			al.add(HCex);
		    }
		    /* create TYPESWITCH */
		    TYPESWITCH ts = new TYPESWITCH
			(qf, phi, Tex,
			 (HClass[]) al.toArray(new HClass[al.size()]),
			 new Temp[0], true);
		    /* link up typeswitch */
		    Quad.addEdge(phi, 0, ts, 0);
		    int edge=0;
		    for (Iterator ee=hs.iterator(); edge<ts.arity(); edge++) {
			if (!ee.hasNext()) break; // no 'catch any' handler
			HANDLER h = (HANDLER)ee.next();
			NOP q0 = new NOP(qf, h);
			Quad.addEdge(ts, edge, q0, 0);
			register(q0, h);
		    }
		    if (edge<ts.arity()) { // add a 'rethrow' statement
			THROW q0 = new THROW(qf, phi, Tex);
			Quad.addEdge(ts, edge++, q0, 0);
			register(q0);
		    }
		    assert edge==ts.arity();
		}
	    } // end 'for each handler set'

	    // attach end-of-the-line throws to footer.
	    FOOTER oldQf = (FOOTER) newQh.next(0); int j=oldQf.arity();
	    FOOTER newQf = oldQf.resize(j + Vthrows.size());
	    for (Iterator e=Vthrows.iterator(); e.hasNext(); )
		Quad.addEdge((THROW)e.next(), 0, newQf, j++);

	    // FINALLY link to handlers
	    for (Iterator e=Hhandler.keySet().iterator(); e.hasNext(); ) {
		HANDLER h = (HANDLER) e.next();
		List v = get(Hhandler, h); // note that v can be size 0.
		Edge ed; Quad tail;
		// note that v.size() may be equal to zero.
		// if this is the case, then the unreachable code elimination
		// below will take care of it.
		PHI  q0 = new PHI(qf, h, new Temp[0], v.size() /*arity*/);
		for (int i=0; i<v.size(); i++) {
		    ed = ((NOP)v.get(i)).prevEdge(0);
		    Quad.addEdge((Quad)ed.from(), ed.which_succ(), q0, i);
		}
		MOVE q1 = new MOVE(qf, h, Quad.map(ctm, h.exceptionTemp()),
				   Tex);
		Quad.addEdge(q0, 0, q1, 0);
		// link into handler.
		ed = qm.getFoot(h).nextEdge(0); tail = q1;

		Quad.addEdge(tail, 0, (Quad)ed.to(), ed.which_pred());
	    }
	    // trim unreachables (those 0-input phis in the above)
	    Unreachable.prune(newQh);
	    // done.
	}

	// make hashtables appear to map directly to vectors.
	private static List get(Map h, Object o) {
	    List v = (List) h.get(o);
	    if (v==null) { v = new ArrayList(); h.put(o, v); }
	    return v;
	}
    }
    /** Fixup information for optimized INSTANCEOFs. */
    private static final class InstanceOfFixupMap extends HashSet {
	void put(CJMP cjmp, PHI phi) { this.add(Default.pair(cjmp, phi)); }
	void fixup(StaticState ss) {
	    for (Iterator it=this.iterator(); it.hasNext(); ) {
		List pair = (List) it.next();
		CJMP cjmp = (CJMP) pair.get(0);
		PHI phi = (PHI) pair.get(1);
		// get new version of the cjmp.
		cjmp = (CJMP) ss.qm.getFoot(cjmp);
		// add 0-PHI-0 to 0-edge of CJMP.
		Edge e = cjmp.nextEdge(0);
		Quad.addEdge((Quad)e.from(), e.which_succ(), phi, 0);
		Quad.addEdge(phi, 0, (Quad)e.to(), e.which_pred());
		// done!
	    }
	}
    }

    /** Static state for visitor. */
    private static final class StaticState {
	final QuadFactory qf;
	final QuadMap qm;
	final HandlerMap hm;
	final CloningTempMap ctm;
	final ClassHierarchy ch;
	final InstanceOfFixupMap iofm = new InstanceOfFixupMap();
	final boolean coalesce;
	final List extra = new ArrayList(4);
	StaticState(QuadFactory qf, QuadMap qm, HandlerMap hm,
		    CloningTempMap ctm, ClassHierarchy ch, boolean coalesce) {
	    this.qf = qf; this.qm = qm; this.hm = hm; this.ctm = ctm;
	    this.ch = ch;
	    this.coalesce = coalesce;
	}
	Temp extra(int i) {
	    while (extra.size() <= i)
		extra.add(new Temp(qf.tempFactory(),
				   "un"+extra.size()+"_"));
	    return (Temp) extra.get(i);
	}
    }

    /** Guts of the algorithm: map from old to new quads, putting the
     *  result in the QuadMap. */
    private static final class Visitor extends QuadVisitor {
	final QuadFactory qf;
	// which Temps are non-null/arrays of known length/integer constants
	final TempInfo ti;
	// various bits of static state.
	final StaticState ss;

	Visitor(TempInfo ti, StaticState ss) {
	    this.qf = ss.qf; this.ti = ti; this.ss = ss;
	    //////////////// exceptions.
	    // (try to cache these up here, since they are frequently used)
	    Linker linker=qf.getLinker();
	    this.HCarraystoreE = 
		linker.forName("java.lang.ArrayStoreException");
	    this.HCnullpointerE =
		linker.forName("java.lang.NullPointerException");
	    this.HCarrayindexE =
		linker.forName("java.lang.ArrayIndexOutOfBoundsException");
	    this.HCnegativearrayE =
		linker.forName("java.lang.NegativeArraySizeException");
	    this.HCarithmeticE =
		linker.forName("java.lang.ArithmeticException");
	    this.HCclasscastE =
		linker.forName("java.lang.ClassCastException");
	}

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    assert !ss.qm.contains(q);
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    Temp d[] = q.def();
	    for (int i=0; i<d.length; i++)
		ti.put(d[i], Type.top);
	}
	// watch order of type updates below: always update src before dst
	// to allow dst type to overwrite src if src==dst.
	public void visit(AGET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq, tail = nq;
	    Type Tobj = ti.get(q.objectref());
	    Type Tind = ti.get(q.index());
	    if (ARRAY_BOUNDS_CHECKS &&
		! (Tobj.isFixedArray() &&
		   Tind.isIntConst() &&
		   Tind.getConstValue() < Tobj.getArrayLength() &&
		   Tind.getConstValue() >= 0) ) {
		Quad[] qs = Tobj.isFixedArray() ? // don't make ALENGTH quad
		    boundsCheck(q, head, tail, Tobj.getArrayLength(), q.index(),
				defaultConst(head, q.dst(), q.type())) :
		    boundsCheck(q, head, tail, q.objectref(), q.index(),
				defaultConst(head, q.dst(), q.type()));
		head = qs[0];
		tail = qs[1];
	    }
	    if (!Tobj.isNonNull()) {
		Quad[] qs = nullCheck(q, head, tail, q.objectref(),
				      defaultConst(head, q.dst(), q.type()));
		head = qs[0];
		tail = qs[1];
	    } else {
		ti.update(q.objectref(), alsoNonNull(Tobj));
	    }
	    ss.qm.put(q, head, tail);
	    ti.put(q.dst(), Type.top);
	}
	public void visit(ALENGTH q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq, tail = nq;
	    Type Tobj = ti.get(q.objectref());
	    if (!Tobj.isNonNull()) {
		Quad[] qs = nullCheck(q, head, tail, q.objectref(),
				      defaultConst(head, q.dst(), HClass.Int));
		head = qs[0];
		tail = qs[1];
	    } else {
		ti.update(q.objectref(), alsoNonNull(Tobj));
	    }
	    ss.qm.put(q, head, tail);
	    if (Tobj.isFixedArray())
		ti.put(q.dst(), new IntConst(Tobj.getArrayLength()));
	    else
		ti.put(q.dst(), Type.top);
	}
	public void visit(ANEW q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq, tail = nq;
	    Type Tdim = null;
	    for (int i=q.dimsLength()-1; i>=0; i--) {
		Tdim = ti.get(q.dims(i));
		if ( ! (Tdim.isIntConst() && Tdim.getConstValue() >= 0)) {
		    Quad[] qs = minusCheck(q, head, tail, q.dims(i),
					   defaultConst(head, q.dst(), q.hclass()));
		    head = qs[0];
		    tail = qs[1];
		}
	    }
	    ss.qm.put(q, head, tail);
	    if (Tdim.isIntConst()) // type of first array dimension.
		ti.put(q.dst(), new FixedArray(Tdim.getConstValue()));
	    else ti.put(q.dst(), Type.top);
	}
	public void visit(ARRAYINIT q) {
	    // hmm.  have to break it up if we can't prove that it's safe.
	    Type Tobj = ti.get(q.objectref());
	    // XXX break the following into its three component cases?
	    if (Tobj.isFixedArray() && q.offset() >= 0 &&
		q.offset()+q.value().length <= Tobj.getArrayLength() ) {
		// safe.
		Quad nq = (Quad) q.clone(qf, ss.ctm);
		ss.qm.put(q, nq, nq);
	    } else { 
		// not safe.  Break into components.
		assert false; // FIXME
	    }
	    ti.update(q.objectref(), alsoNonNull(Tobj));
	}
	public void visit(ASET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq, tail = nq;
	    Type Tobj = ti.get(q.objectref());
	    Type Tind = ti.get(q.index());
	    // do COMPONENTOF test for non-primitive arrays.
	    if (!q.type().isPrimitive()) {
		Quad[] qs = componentCheck(q,head,tail,q.objectref(),q.src());
		head = qs[0];
		tail = qs[1];
	    }
	    if (ARRAY_BOUNDS_CHECKS &&
		! (Tobj.isFixedArray() &&
		   Tind.isIntConst() &&
		   Tind.getConstValue() < Tobj.getArrayLength() &&
		   Tind.getConstValue() >= 0) ) {
		Quad[] qs = Tobj.isFixedArray() ? // don't make ALENGTH quad.
		    boundsCheck(q,head,tail,Tobj.getArrayLength(),q.index(),
				null) : // have to query for ALENGTH
		    boundsCheck(q,head,tail,q.objectref(),q.index(),null);
		head = qs[0];
		tail = qs[1];
	    }
	    if (!Tobj.isNonNull()) {
		Quad[] qs = nullCheck(q, head, tail, q.objectref(), null);
		head = qs[0];
		tail = qs[1];
	    } else {
		ti.update(q.objectref(), alsoNonNull(Tobj));
	    }
	    ss.qm.put(q, head, tail);
	}
	public void visit(CALL q) {
	    Quad nq, head, tail;
	    assert q.retex()==null : "don't allow checked ex in qwt";
	    // if retex==null, add the proper checks.
	    if (q.retex()!=null) nq=head=tail=(Quad)q.clone(qf, ss.ctm);
	    else {
		Temp Tex = ss.extra(0);
		Temp Trv = q.retval()==null ? null : ss.extra(1);
		nq=head=new CALL(qf, q, q.method(), Quad.map(ss.ctm,q.params()),
				 Trv, Tex, q.isVirtual(), q.isTailCall(),
				 new Temp[0]);
		Quad q4 = new PHI(qf, q, new Temp[0], 2);
		// because of "define both" CALL semantics, we need to rewrite
		// so that we only redefine retval if non-exceptional edge
		// is taken.
		if (q.retval()!=null) {
		    Quad q5=new MOVE(qf, q, Quad.map(ss.ctm, q.retval()), Trv);
		    Quad.addEdge(head, 0, q5, 0);
		    Quad sub = 
			defaultConst(head, q.retval(), q.method().getReturnType());
		    Quad.addEdge(head, 1, sub, 0);
		    Quad.addEdge(q5, 0, q4, 0);
		    Quad.addEdge(sub, 0, q4, 1);
		} else {
		    Quad.addEdge(head, 0, q4, 0);
		    Quad.addEdge(head, 1, q4, 1);
		}
		tail = q4;
	    }
	    // if non-static, check that receiver is not null.
	    if (!q.isStatic() && !ti.get(q.params(0)).isNonNull()) {
		Quad[] qs=nullCheck(q, head, tail, q.params(0), q.retval()==null?null:
				    defaultConst(nq, q.retval(), 
						 q.method().getReturnType()));
		head = qs[0];
		tail = qs[1];
	    } else {
		if (!q.isStatic()) ti.update(q.params(0),
					     alsoNonNull(ti.get(q.params(0))));
	    }
	    ss.qm.put(q, head, tail);
	    // nothing known about return values or exceptions thrown.
	    if (q.retval()!=null) ti.put(q.retval(), Type.top);
	    if (q.retex()!=null) ti.put(q.retex(), Type.nonnull);
	}

	// no run-time checks necessary:
	/*public void visit(CJMP q);*/
	/*public void visit(COMPONENTOF q);*/

	public void visit(CONST q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    if (q.type() == HClass.Int)
		ti.put(q.dst(), new IntConst(((Integer)q.value()).intValue()));
	    else
		ti.put(q.dst(), Type.top);
	}
	    
	/*public void visit(DEBUG q);*/
	/*public void visit(FOOTER q);*/
	public void visit(GET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq, tail=nq;
	    if (!q.isStatic()) {
		Type Tobj = ti.get(q.objectref());
		if (!Tobj.isNonNull()) {
		    Quad[] qs = nullCheck(q, head, tail, q.objectref(),
					  defaultConst(head, q.dst(), 
						       q.field().getType()));
		    head = qs[0];
		    tail = qs[1];
		} else {
		    ti.update(q.objectref(), alsoNonNull(Tobj));
		}
	    }
	    ss.qm.put(q, head, tail);
	    ti.put(q.dst(), Type.top);
	}
	/*public void visit(HEADER q);*/
	public void visit(INSTANCEOF q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    Type Tobj = ti.get(q.src());
	    if (!Tobj.isNonNull()) {
		// insert explicit test against null.
		Temp Tr = ss.extra(0);
		Quad q0 = new CONST(qf, head, Tr, null, HClass.Void);
		Quad q1 = new OPER(qf, head, Qop.ACMPEQ, Tr,
				   new Temp[]{Quad.map(ss.ctm, q.src()), Tr});
		Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
		Quad q3 = new CONST(qf, head, Quad.map(ss.ctm, q.dst()),
				    new Integer(0), HClass.Int);
		Quad q4 = new PHI(qf, head, new Temp[0], 2);
		Quad.addEdges(new Quad[] { q0, q1, q2, head, q4 });
		Quad.addEdge(q2, 1, q3, 0);
		Quad.addEdge(q3, 0, q4, 1);
		head = q0; nq = q4;
		// optimize: if INSTANCEOF directly feeds a CJMP, then we
		// can change q2 to directly jump to proper dest, removing
		// need to second test. (also, analysis works better on
		// this version, due to the way the value merges are arranged)
		if (q.next(0) instanceof CJMP &&
		    ((CJMP)q.next(0)).test().equals(q.dst())) {
		    CJMP cjmp = (CJMP) q.next(0);
		    // translation of this quad will resume after instanceof
		    nq = q2.next(0);
		    // add to fixup map.
		    ss.iofm.put(cjmp, (PHI) q4);
		}
	    }
	    ss.qm.put(q, head, nq);
	    ti.put(q.dst(), Type.top);
	}
	/*public void visit(LABEL q);*/
	/*public void visit(HANDLER q);*/
	public void visit(METHOD q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    for (int i=0; i<q.paramsLength(); i++)
		ti.put(q.params(i), // 'this' is non-null for non-static.
		       (i==0 && !q.isStatic()) ? Type.nonnull : Type.top);
	    ss.qm.put(q, nq, nq);
	}
	public void visit(MONITORENTER q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq, tail=nq;
	    Type Tlck = ti.get(q.lock());
	    if (!Tlck.isNonNull()) {
		Quad[] qs = nullCheck(q, head, tail, q.lock(), null);
		head = qs[0];
		tail = qs[1];
	    } else {
		ti.update(q.lock(), alsoNonNull(Tlck));
	    }
	    ss.qm.put(q, head, tail);
	}
	public void visit(MONITOREXIT q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq, tail=nq;
	    Type Tlck = ti.get(q.lock());
	    if (!Tlck.isNonNull()) {
		Quad[] qs = nullCheck(q, head, tail, q.lock(), null);
		head = qs[0];
		tail = qs[1];
	    } else {
		ti.update(q.lock(), alsoNonNull(Tlck));
	    }
	    ss.qm.put(q, head, tail);
	}
	public void visit(MOVE q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    ti.doMove(q.dst(), q.src());
	}
	public void visit(NEW q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    ti.put(q.dst(), Type.nonnull);
	}
	/*public void visit(NOP q);*/
	public void visit(OPER q) {
	    // we're really ambitious; we do limited constant prop
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq, tail=nq;
	    Type Td = Type.top;
	    if (q.operandsLength()==1) {
		Type To = ti.get(q.operands(0));
		switch (q.opcode()) {
		case Qop.INEG:
		    if (To.isIntConst())
			Td = new IntConst(-To.getConstValue());
		    break;
		default: break;
		}
	    } else if (q.operandsLength()==2) {
		Type Tl = ti.get(q.operands(0));
		Type Tr = ti.get(q.operands(1));
		switch (q.opcode()) {
		case Qop.LDIV:
		case Qop.LREM:
		    {
			Quad[] qs = longZeroCheck(q, head, q.operands(1), defaultConst
						  (head, q.dst(), q.evalType()));
			head = qs[0];
			tail = qs[1];
		    }
		    break;
		case Qop.IDIV:
		    if (Tl.isIntConst() && Tr.isIntConst() &&
			Tr.getConstValue()!=0)
			Td = new IntConst(Tl.getConstValue() / 
					  Tr.getConstValue());
		    else {
			Quad[] qs = intZeroCheck(q, head, q.operands(1), defaultConst
						 (head, q.dst(), q.evalType()));
			head = qs[0];
			tail = qs[1];
		    }
		    break;
		case Qop.IREM:
		    if (Tl.isIntConst() && Tr.isIntConst() &&
			Tr.getConstValue()!=0)
			Td = new IntConst(Tl.getConstValue() %
					  Tr.getConstValue());
		    else {
			Quad[] qs = intZeroCheck(q, head, q.operands(1), defaultConst
						 (head, q.dst(), q.evalType()));
			head = qs[0];
			tail = qs[1];
		    }
		    break;
		case Qop.IADD:
		    if (Tl.isIntConst() && Tr.isIntConst())
			Td = new IntConst(Tl.getConstValue() +
					  Tr.getConstValue());
		    break;
		case Qop.IMUL:
		    if (Tl.isIntConst() && Tr.isIntConst())
			Td = new IntConst(Tl.getConstValue() *
					  Tr.getConstValue());
		    break;
		default: break;
		}
	    }
	    ti.put(q.dst(), Td);
	    ss.qm.put(q, head, tail);
	}
	public void visit(PHI q) {
	    ti.clear(); // clear all info at program merge points.
	    visit((Quad)q); // copy
	}
	/*public void visit(RETURN q);*/
	public void visit(SET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq, tail=nq;
	    if (!q.isStatic()) {
		Type Tobj = ti.get(q.objectref());
		if (!Tobj.isNonNull()) {
		    Quad[] qs = nullCheck(q, head, tail, q.objectref(), null);
		    head = qs[0];
		    tail = qs[1];
		} else {
		    ti.update(q.objectref(), alsoNonNull(Tobj));
		}
	    }
	    ss.qm.put(q, head, tail);
	}
	/*public void visit(SIGMA q);*/
	/*public void visit(SWITCH q);*/
	public void visit(THROW q) {
	    Temp Tex = Quad.map(ss.ctm, q.throwable());
	    Quad head = _throwException_(qf, q,  Tex); // new throw.
	    Type Tthr = ti.get(q.throwable());
	    if (!Tthr.isNonNull())
		head = nullCheck(q, head, q.throwable());
	    // make a unreachable chain.  The PHI would be enough, except
	    // that the Quad superclass enforces the 'only connect
	    // THROW, HEADER, or RETURN to FOOTER' rule.  So we have to
	    // go ahead and clone the THROW so we have something to
	    // connect.  This whole string will be wiped out by the
	    // unreachable code elimination pass.
	    Quad q0 = new PHI(qf, q, new Temp[0], 0); // unreachable head.
	    Quad nq = (Quad) q.clone(qf, ss.ctm); // unreachable foot.
	    Quad.addEdge(q0, 0, nq, 0);
	    ss.qm.put(q, head, nq);
	    ti.update(q.throwable(), alsoNonNull(Tthr)); // arguably useless.
	}
	public void visit(TYPECAST q) {
	    // translate as:
	    //  if (obj!=null && !(obj instanceof class))
	    //     throw new ClassCastException();
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head, tail;
	    Type Tobj = ti.get(q.objectref());
	    Temp Tr = ss.extra(0);
	    Quad q1 = new INSTANCEOF(qf, q, Tr,
				     Quad.map(ss.ctm, q.objectref()),
				     q.hclass());
	    Quad q2 = new CJMP(qf, q, Tr, new Temp[0]);
	    if (isHandled(q, HCclasscastE)) {
		Quad q3 = _throwException_(qf, q, HCclasscastE);
		Quad.addEdges(new Quad[] { q1, q2, q3 });
		Quad.addEdge(q2, 1, nq, 0);
		tail = nq;
	    } else {
		Quad sub = defaultConst(nq, q.objectref(), q.hclass());
		Quad q3 = new PHI(qf, q, new Temp[0], 2);
		Quad.addEdges(new Quad[] { q1, q2, sub, q3 });
		Quad.addEdge(q2, 1, nq, 0);
		Quad.addEdge(nq, 0, q3, 1);
		tail = q3;
	    }
	    head = q1;
	    if (!Tobj.isNonNull()) { // specially handle null.
		Quad q4 = new CONST(qf, q, Tr, null, HClass.Void);
		Quad q5 = new OPER(qf, q, Qop.ACMPEQ, Tr,
				   new Temp[] { Quad.map(ss.ctm,q.objectref()),
						Tr });
		Quad q6 = new CJMP(qf, q, Tr, new Temp[0]);
		Quad q7 = new PHI(qf, q, new Temp[0], 2); // a-ok merge
		Quad.addEdges(new Quad[] { q4, q5, q6, head });
		Quad.addEdge(q6, 1, q7, 0); // if null, branch to a-ok
		Quad.addEdge(q2, 1, q7, 1); // if instanceof class, goto a-ok
		Quad.addEdge(q7, 0, nq, 0); // (link PHI to a-ok)
		head = q4; // tail is unchanged
	    }
	    ss.qm.put(q, head, tail);
	    // no change to type info, since we don't keep track of class
	}

	/// Add 'nonnull' attribute
	Type alsoNonNull(Type in) {
	    return in.isNonNull()?in:Type.nonnull;
	}

	boolean isHandled(Quad old, HClass HCex) {
	    HandlerSet hs = ss.hm.handlers(old);
	    if (hs==null) return false;	    
	    for(Iterator ee=hs.iterator(); ee.hasNext(); ) {
		HClass Hcls = ((HANDLER)ee.next()).caughtException();
		// Hcls==null is the 'catch any' case
		if (Hcls==null || Hcls.equals(HCex) ||
		    ss.ch.children(Hcls).contains(HCex))
		    return true;
	    }
	    return false; // no match
	}
	
	//////////////// exceptions.
	private final HClass HCarraystoreE;
	private final HClass HCnullpointerE;
	private final HClass HCarrayindexE;
	private final HClass HCnegativearrayE;
	private final HClass HCarithmeticE;
	private final HClass HCclasscastE;

	//////////////// runtime checks.
	Quad intZeroCheck(Quad old, Quad head, Temp Tz) {
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, new Integer(0), HClass.Int);
	    Quad q1 = new OPER(qf, head, Qop.ICMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm,Tz), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = _throwException_(qf, old, HCarithmeticE);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    Quad.addEdge(q2, 1, q3, 0);
	    return q0;
	}
	Quad[] intZeroCheck(Quad old, Quad head, Temp Tz, Quad sub) {
	    if (isHandled(old, HCarithmeticE))
		return new Quad[] { intZeroCheck(old, head, Tz), head };
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, new Integer(0), HClass.Int);
	    Quad q1 = new OPER(qf, head, Qop.ICMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm,Tz), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = new PHI(qf, head, new Temp[0], 2);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head, q3 });
	    Quad.addEdge(q2, 1, sub, 0);
	    Quad.addEdge(sub, 0, q3, 1);
	    return new Quad[] { q0, q3 };
	}
	Quad longZeroCheck(Quad old, Quad head, Temp Tz) {
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, new Long(0), HClass.Long);
	    Quad q1 = new OPER(qf, head, Qop.LCMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm, Tz), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = _throwException_(qf, old, HCarithmeticE);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    Quad.addEdge(q2, 1, q3, 0);
	    return q0;
	}
	Quad[] longZeroCheck(Quad old, Quad head, Temp Tz, Quad sub) {
	    if (isHandled(old, HCarithmeticE))
		return new Quad[] { longZeroCheck(old, head, Tz), head };
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, new Long(0), HClass.Long);
	    Quad q1 = new OPER(qf, head, Qop.LCMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm, Tz), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = new PHI(qf, head, new Temp[0], 2);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head, q3 });
	    Quad.addEdge(q2, 1, sub, 0);
	    Quad.addEdge(sub, 0, q3, 1);
	    return new Quad[] { q0, q3 };
	}
	Quad componentCheck(Quad old, Quad head, Temp Tobj, Temp Tsrc) {
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    // test Tobj against null & branch around COMPONENTOF.
	    Quad q0 = new COMPONENTOF(qf, head, Tr,
				      Quad.map(ss.ctm, Tobj),
				      Quad.map(ss.ctm, Tsrc));
	    Quad q1 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q2 = _throwException_(qf, old, HCarraystoreE);
	    if (ti.get(Tsrc).isNonNull()) {
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		Quad.addEdge(q1, 1, head, 0);
		return q0;
	    } else { // insert null check if src is not known non-null
		Quad qa = new CONST(qf, head, Tr, null, HClass.Void);
		Quad qb = new OPER(qf, head, Qop.ACMPEQ, Tr,
				   new Temp[] { Quad.map(ss.ctm, Tsrc), Tr });
		Quad qc = new CJMP(qf, head, Tr, new Temp[0]);
		Quad qd = new PHI(qf, head, new Temp[0], 2);
		Quad.addEdges(new Quad[] { qa, qb, qc, q0, q1, q2 });
		Quad.addEdge(qc, 1, qd, 0);
		Quad.addEdge(q1, 1, qd, 1);
		Quad.addEdge(qd, 0, head, 0);
		return qa;
	    }
	}
	Quad[] componentCheck(Quad old, Quad head, Quad tail, Temp Tobj, Temp Tsrc) {
	    if (isHandled(old, HCarraystoreE))
		return new Quad[] { componentCheck(old, head, Tobj, Tsrc), tail };
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    // test Tobj against null & branch around COMPONENTOF.
	    Quad q0 = new COMPONENTOF(qf, head, Tr,
				      Quad.map(ss.ctm, Tobj),
				      Quad.map(ss.ctm, Tsrc));
	    Quad q1 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q2 = new PHI(qf, head, new Temp[0], 2);
	    if (ti.get(Tsrc).isNonNull()) {
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		Quad.addEdge(q1, 1, head, 0);
		Quad.addEdge(tail, 0, q2, 1);
		return new Quad[] { q0, q2 };
	    } else { // insert null check if src is not known non-null
		Quad qa = new CONST(qf, head, Tr, null, HClass.Void);
		Quad qb = new OPER(qf, head, Qop.ACMPEQ, Tr,
				   new Temp[] { Quad.map(ss.ctm, Tsrc), Tr });
		Quad qc = new CJMP(qf, head, Tr, new Temp[0]);
		Quad qd = new PHI(qf, head, new Temp[0], 2);
		Quad.addEdges(new Quad[] { qa, qb, qc, q0, q1, q2 });
		Quad.addEdge(qc, 1, qd, 0);
		Quad.addEdge(q1, 1, qd, 1);
		Quad.addEdge(qd, 0, head, 0);
		Quad.addEdge(tail, 0, q2, 1);
		return new Quad[] { qa, q2 };
	    }
	}
	Quad nullCheck(Quad old, Quad head, Temp Tobj) {
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, null, HClass.Void);
	    Quad q1 = new OPER(qf, head, Qop.ACMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm, Tobj), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = _throwException_(qf, old, HCnullpointerE);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    Quad.addEdge(q2, 1, q3, 0);
	    return q0;
	}
	Quad[] nullCheck(Quad old, Quad head, Quad tail, Temp Tobj, Quad sub) {
	    if (isHandled(old, HCnullpointerE))
		return new Quad[] { nullCheck(old, head, Tobj), tail };
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tr, null, HClass.Void);
	    Quad q1 = new OPER(qf, head, Qop.ACMPEQ, Tr,
			       new Temp[] { Quad.map(ss.ctm, Tobj), Tr });
	    Quad q2 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q3 = new PHI(qf, head, new Temp[0], 2);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    if (sub != null) {
		Quad.addEdge(q2, 1, sub, 0);
		Quad.addEdge(sub, 0, q3, 1);
	    } else {
		Quad.addEdge(q2, 1, q3, 1);
	    }
	    Quad.addEdge(tail, 0, q3, 0);
	    return new Quad[] { q0, q3 };
	}
	Quad[] boundsCheck(Quad old, Quad head, Quad tail, int length, 
			   Temp Ttst, Quad sub) {
	    Quad q0 = new CONST(qf, head, ss.extra(0),
				new Integer(length), HClass.Int);
	    Quad[] qs = _boundsCheck_(old, head, tail, ss.extra(0),
				      Quad.map(ss.ctm, Ttst), ss.extra(1), sub);
	    Quad.addEdge(q0, 0, qs[0], 0);
	    return new Quad[] { q0, qs[1] };
	}
	Quad[] boundsCheck(Quad old, Quad head, Quad tail, Temp Tobj, 
			   Temp Ttst, Quad sub) {
	    Quad q0 = new ALENGTH(qf, head, ss.extra(0),
				  Quad.map(ss.ctm, Tobj));
	    Quad[] qs = _boundsCheck_(old, head, tail, ss.extra(0),
				      Quad.map(ss.ctm, Ttst), ss.extra(1), sub);
	    Quad.addEdge(q0, 0, qs[0], 0);
	    return new Quad[] { q0, qs[1] };
	}
	private Quad _boundsCheck_(Quad old, Quad head, Temp Tlen, Temp Ttst,
				   Temp Textra1) {
	    assert Tlen.tempFactory()==head.qf.tempFactory();
	    assert Ttst.tempFactory()==head.qf.tempFactory();
	    assert Textra1.tempFactory()==head.qf.tempFactory();
	    QuadFactory qf = head.qf;
	    Quad q0 = new OPER(qf, head, Qop.ICMPGT, Tlen,
			       new Temp[] { Tlen, Ttst });
	    Quad q1 = new CJMP(qf, head, Tlen, new Temp[0]);
	    Temp Tz = Tlen; // reuse this temp.
	    Quad q2 = new CONST(qf, head, Tz, new Integer(0), HClass.Int);
	    Quad q3 = new OPER(qf, head, Qop.ICMPGT, Tz,
			       new Temp[] { Tz, Ttst });
	    Quad q4 = new CJMP(qf, head, Tz, new Temp[0]);
	    Temp Tex = Tlen, Textra2 = Ttst; // reuse temps again.
	    Quad q5 = new PHI(qf, head, new Temp[0], 2);
	    Quad q6 = _throwException_(qf, old, HCarrayindexE);
	    Quad.addEdges(new Quad[] { q0, q1, q5, q6 });
	    Quad.addEdge(q1, 1, q2, 0);
	    Quad.addEdges(new Quad[] { q2, q3, q4, head });
	    Quad.addEdge(q4, 1, q5, 1);
	    return q0;
	}
	private Quad[] _boundsCheck_(Quad old, Quad head, Quad tail, Temp Tlen,
				   Temp Ttst, Temp Textra1, Quad sub) {
	    if (isHandled(old, HCarrayindexE))
		return new Quad[] { _boundsCheck_(old,head,Tlen,Ttst,Textra1), tail };
	    assert Tlen.tempFactory()==head.qf.tempFactory();
	    assert Ttst.tempFactory()==head.qf.tempFactory();
	    assert Textra1.tempFactory()==head.qf.tempFactory();
	    QuadFactory qf = head.qf;
	    Quad q0 = new OPER(qf, head, Qop.ICMPGT, Tlen,
			       new Temp[] { Tlen, Ttst });
	    Quad q1 = new CJMP(qf, head, Tlen, new Temp[0]);
	    Temp Tz = Tlen; // reuse this temp.
	    Quad q2 = new CONST(qf, head, Tz, new Integer(0), HClass.Int);
	    Quad q3 = new OPER(qf, head, Qop.ICMPGT, Tz,
			       new Temp[] { Tz, Ttst });
	    Quad q4 = new CJMP(qf, head, Tz, new Temp[0]);
	    Temp Tex = Tlen, Textra2 = Ttst; // reuse temps again.
	    Quad q5 = new PHI(qf, head, new Temp[0], 2);
	    Quad q6 = new PHI(qf, head, new Temp[0], 2);
	    if (sub != null)
		Quad.addEdges(new Quad[] { q0, q1, q5, sub, q6 });
	    else // no substitution Quad provided
		Quad.addEdges(new Quad[] { q0, q1, q5, q6 });
	    Quad.addEdge(q1, 1, q2, 0);
	    Quad.addEdges(new Quad[] { q2, q3, q4, head });
	    Quad.addEdge(q4, 1, q5, 1);
	    Quad.addEdge(tail, 0, q6, 1);
	    return new Quad[] { q0, q6 };
	}
	Quad minusCheck(Quad old, Quad head, Temp Ttst) {
	    QuadFactory qf = head.qf;
	    Temp Tz = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tz, new Integer(0), HClass.Int);
	    Quad q1 = new OPER(qf, head, Qop.ICMPGT, Tz,
			       new Temp[] { Tz, Quad.map(ss.ctm, Ttst) });
	    Quad q2 = new CJMP(qf, head, Tz, new Temp[0]);
	    Quad q3 = _throwException_(qf, old, HCnegativearrayE);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    Quad.addEdge(q2, 1, q3, 0);
	    return q0;
	}
	Quad[] minusCheck(Quad old, Quad head, Quad tail, Temp Ttst, Quad sub) {
	    if (isHandled(old, HCnegativearrayE))
		return new Quad[] { minusCheck(old, head, Ttst), tail };
	    QuadFactory qf = head.qf;
	    Temp Tz = ss.extra(0);
	    Quad q0 = new CONST(qf, head, Tz, new Integer(0), HClass.Int);
	    Quad q1 = new OPER(qf, head, Qop.ICMPGT, Tz,
			       new Temp[] { Tz, Quad.map(ss.ctm, Ttst) });
	    Quad q2 = new CJMP(qf, head, Tz, new Temp[0]);
	    Quad q3 = new PHI(qf, head, new Temp[0], 2);
	    Quad.addEdges(new Quad[] { q0, q1, q2, head });
	    Quad.addEdge(q2, 1, sub, 0);
	    Quad.addEdge(sub, 0, q3, 1);
	    Quad.addEdge(tail, 0, q3, 0);
	    return new Quad[] { q0, q3 };
	}
	private Quad _throwException_(QuadFactory qf, Quad old, HClass HCex) {
	    List l = ss.hm.registry(HCex, ss.hm.handlers(old));
	    if (ss.coalesce && l.size()>0) {
		// if we've already made an exception of this type, just
		// save a trailer for the fixup.
		Quad q = new NOP(qf, old);
		l.add(q);
		return q;
	    }
	    Temp Tex = ss.hm.Tex, Tex2 = ss.extra(0), Tnull = ss.extra(1);
	    Quad q0 = new NEW(qf, old, Tex, HCex);
	    Quad q1 = new CALL(qf, old, HCex.getConstructor(new HClass[0]),
			       new Temp[] { Tex }, null, Tex2, false, false,
			       new Temp[0]);
	    Quad q5 = new MOVE(qf, old, Tex, Tex2);
	    Quad q6 = new PHI(qf, old, new Temp[0], 2);
	    Quad q7 = _throwException_(qf, old, Tex);
	    Quad.addEdges(new Quad[] { q0, q1, q6, q7 });
	    Quad.addEdge(q1, 1, q5, 0);
	    Quad.addEdge(q5, 0, q6, 1);
	    // save the header so we can reuse this exception-generation code.
	    if (ss.coalesce) l.add(q0);
	    return q0;
	}
	private Quad _throwException_(QuadFactory qf, Quad old, Temp Tex) {
	    HandlerSet hs = ss.hm.handlers(old);
	    // if ss.coalesce==true, we coalesce even the no-handler case.
	    if (hs==null && !ss.coalesce) {
		THROW q0 = new THROW(qf, old, Tex);
		ss.hm.register(q0);
		return q0;
	    } else {
		NOP q0 = new NOP(qf, old);
		ss.hm.register(q0, hs);
		if (Tex == ss.hm.Tex) return q0; // done already.
		// else...
		MOVE q1 = new MOVE(qf, old, ss.hm.Tex, Tex);
		Quad.addEdge(q1, 0, q0, 0);
		return q1;
	    }
	}
	private CONST defaultConst(Quad head, Temp Tdst, HClass cls) {
	    cls = defaultClass(cls);
	    return new CONST(head.qf, head, Quad.map(ss.ctm, Tdst), 
			     defaultValue(cls), cls);
	}
    }
    static final HClass defaultClass(HClass cls) {
	if (!cls.isPrimitive()) return HClass.Void;
	if (cls == HClass.Boolean ||
	    cls == HClass.Byte ||
	    cls == HClass.Char ||
	    cls == HClass.Int ||
	    cls == HClass.Short) return HClass.Int;
	if (cls == HClass.Double) return HClass.Double;
	if (cls == HClass.Float) return HClass.Float;
	if (cls == HClass.Long) return HClass.Long;
	throw new Error("Ack!  What kinda default class is this?!");
    }
    static final Object defaultValue(HClass cls) {
	if (cls == HClass.Void) return null;
	if (cls == HClass.Double) return new Double(0);
	if (cls == HClass.Float) return new Float(0);
	if (cls == HClass.Int) return new Integer(0);
	if (cls == HClass.Long) return new Long(0);
	throw new Error("Ack!  What kinda default value is this?!");
    }
}
