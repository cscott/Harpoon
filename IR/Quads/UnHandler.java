// UnHandler.java, created Wed Dec 23 23:30:32 1998 by cananian
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>UnHandler</code> make exception handling explicit and removes
 * the <code>HANDLER</code> quads from the graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UnHandler.java,v 1.1.2.6 1999-01-23 07:58:21 cananian Exp $
 */
final class UnHandler {
    // entry point.
    public static final Quad unhandler(final QuadFactory qf, final Code code) {
	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)code.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final HandlerMap hm = new HandlerMap(qf, old_method);
	final CloningTempMap ctm = new CloningTempMap(code.qf.tempFactory(),
						      qf.tempFactory());
	final StaticState ss = new StaticState(qf, qm, hm, ctm);
	visitAll(new Visitor(new TempInfo(), ss), old_header);
	// now qm contains mappings from old to new, we just have to link them.
	for (Enumeration e = code.getElementsE(); e.hasMoreElements(); ) {
	    Quad old = (Quad) e.nextElement();
	    // link next.
	    Edge[] el = old.nextEdge();
	    for (int i=0; i<el.length; i++)
		Quad.addEdge(qm.getFoot((Quad)el[i].from()),el[i].which_succ(),
			     qm.getHead((Quad)el[i].to()), el[i].which_pred());
	}
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
	start.visit(v);
	final StaticState ss = v.ss;
	Util.assert(ss.qm.contains(start));
	Quad[] ql = start.next();
	for (int i=0; i<ql.length; i++) {
	    if (ss.qm.contains(ql[i])) continue; // skip if already done.
	    Visitor vv = (i==ql.length-1)? v : // don't clone if never reused
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
	// static types for convenience/efficiency.
	static final Type top=new Top();
	static final Type nonnull=new NonNull();
    }
    private static class Top extends Type {
	boolean isTop() { return true; }
    }
    private static class NonNull extends Type {
	boolean isNonNull() { return true; }
    }
    private static class IntConst extends Type {
	final int value;
	IntConst(int value) { this.value = value; }
	boolean isIntConst() { return true; }
	int getConstValue() { return value; }
    }
    private static class FixedArray extends NonNull {
	final int length;
	FixedArray(int length) { this.length = length; }
	boolean isFixedArray() { return true; }
	int getArrayLength() { return length; }
    }

    /** Mapping of temps to information known about their values at a
     *  given program point. */
    private static class TempInfo implements Cloneable {
	final private Hashtable h;
	private TempInfo(Hashtable h) { this.h = h; }
	public TempInfo() { this.h = new Hashtable(); }

	/** Get the type of a given temp. */
	public Type get(Temp t) { 
	    Type ty=(Type)h.get(t); return (ty==null)?Type.top:ty;
	}
	/** Put a type for a temp. */
	public void put(Temp t, Type ty) {
	    if (ty==null || ty.isTop()) h.remove(t);
	    else h.put(t, ty);
	}
	/** Clear all type information (ie at program merge points) */
	public void clear() { h.clear(); }
	/** Clone the temp info (ie at program splits) */
	public Object clone() {
	    return new TempInfo((Hashtable)h.clone());
	}
    }

    /** mapping from old quads to new quads. */
    private static class QuadMap {
	final private Hashtable h = new Hashtable();
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
	
	/** mapping HandlerSet -> test tree */
	void register(NOP from, HandlerSet to) {
	    get(Hhs, to).addElement(from);
	}
	private final Hashtable Hhs = new Hashtable();

	/** lonely throws with null handlersets. */
	void register(THROW q) { Vthrows.addElement(q); }
	private final Vector Vthrows = new Vector();

	/** HANDLER->Vector.  Later, link all NOPs in Vector to PHI. */
	void register(NOP from, HANDLER to) {
	    get(Hhandler, to).addElement(from);
	}
	private final Hashtable Hhandler = new Hashtable();

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
	    // first attach throws to footer.
	    FOOTER newQf = (FOOTER) newQh.next(0);
	    for (Enumeration e=Vthrows.elements(); e.hasMoreElements(); )
		newQf = newQf.attach((THROW)e.nextElement(), 0);
	    
	    // next do HandlerSets
	    for (Enumeration e=Hhs.keys(); e.hasMoreElements(); ) {
		HandlerSet hs = (HandlerSet)e.nextElement();
		Vector v = get(Hhs, hs);
		Util.assert(hs!=null && v.size()>0); // should be!
		PHI phi = new PHI(qf, (Quad)v.elementAt(0),
				  new Temp[0], v.size() /*arity*/);
		// link all to in of phi
		for (int i=0; i<v.size(); i++) {
		    Edge ed = ((NOP)v.elementAt(i)).prevEdge(0);
		    Quad.addEdge((Quad)ed.from(), ed.which_succ(), phi, i);
		}
		// now build instanceof tree. exception in Tex.
		Quad head = phi; int which_succ = 0;
		for (Enumeration ee=hs.elements(); ee.hasMoreElements(); ) {
		    HANDLER h = (HANDLER)ee.nextElement();
		    HClass HCex = h.caughtException();
		    if (HCex==null || !ee.hasMoreElements()) {
			// catch 'any' or last catch
			NOP q0 = new NOP(qf, h);
			Quad.addEdge(head, which_succ, q0, 0);
			register(q0, h);
			break; // no more uncaught exceptions.
		    } else {
			Quad q0 = new INSTANCEOF(qf, h, Textra, Tex, HCex);
			Quad q1 = new CJMP(qf, h, Textra, new Temp[0]);
			NOP  q2 = new NOP(qf, h);
			Quad.addEdge(head, which_succ, q0, 0);
			Quad.addEdge(q0, 0, q1, 0);
			Quad.addEdge(q1, 1, q2, 0);
			head = q1; which_succ = 0;
			register(q2, h);
		    }
		} // end 'for each handler in handler set'
	    } // end 'for each handler set'

	    // FINALLY link to handlers
	    for (Enumeration e=Hhandler.keys(); e.hasMoreElements(); ) {
		HANDLER h = (HANDLER) e.nextElement();
		Vector v = get(Hhandler, h); // note that v can be size 0.
		Edge ed; Quad tail;
		if (v.size()==0) {
		    // MAKE impossible CJMP
		    Temp Te = Quad.map(ctm, h.exceptionTemp());
		    Quad q0 = new CONST(qf, h, Te, null, HClass.Void);
		    Quad q1 = new OPER(qf, h, Qop.ACMPEQ, Textra,
				       new Temp[] { Te, Te });
		    Quad q2 = new CJMP(qf, h, Textra, new Temp[0]);
		    // link immediately following METHOD (it better be safe)
		    ed = newQm.nextEdge(0);
		    Quad.addEdges(new Quad[] { newQm, q0, q1, q2 });
		    Quad.addEdge(q2, 1, (Quad)ed.to(), ed.which_pred());
		    // link into handler.
		    ed = qm.getFoot(h).nextEdge(0); tail = q2;
		} else {
		    PHI  q0 = new PHI(qf, h, new Temp[0], v.size() /*arity*/);
		    for (int i=0; i<v.size(); i++) {
			ed = ((NOP)v.elementAt(i)).prevEdge(0);
			Quad.addEdge((Quad)ed.from(), ed.which_succ(), q0, i);
		    }
		    MOVE q1 = new MOVE(qf, h, Quad.map(ctm, h.exceptionTemp()),
				       Tex);
		    Quad.addEdge(q0, 0, q1, 0);
		    // link into handler.
		    ed = qm.getFoot(h).nextEdge(0); tail = q1;
		}
		Quad.addEdge(tail, 0, (Quad)ed.to(), ed.which_pred());
	    }
	    // done. (need to trim unreachable?)
	}

	// make hashtables appear to map directly to vectors.
	private static Vector get(Hashtable h, Object o) {
	    Vector v = (Vector) h.get(o);
	    if (v==null) { v = new Vector(); h.put(o, v); }
	    return v;
	}
    }

    /** Static state for visitor. */
    private static final class StaticState {
	final QuadFactory qf;
	final QuadMap qm;
	final HandlerMap hm;
	final CloningTempMap ctm;
	final Vector extra = new Vector(4);
	StaticState(QuadFactory qf, QuadMap qm, HandlerMap hm,
		    CloningTempMap ctm) {
	    this.qf = qf; this.qm = qm; this.hm = hm; this.ctm = ctm;
	}
	Temp extra(int i) {
	    while (extra.size() <= i)
		extra.addElement(new Temp(qf.tempFactory(),
					  "un"+extra.size()+"_"));
	    return (Temp) extra.elementAt(i);
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

	Visitor(TempInfo ti, StaticState ss)
	{ this.qf = ss.qf; this.ti = ti; this.ss = ss; }

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    Temp d[] = q.def();
	    for (int i=0; i<d.length; i++)
		ti.put(d[i], Type.top);
	}
	public void visit(AGET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq;
	    Type Tobj = ti.get(q.objectref());
	    Type Tind = ti.get(q.index());
	    if (! (Tobj.isFixedArray() &&
		   Tind.isIntConst() &&
		   Tind.getConstValue() < Tobj.getArrayLength() &&
		   Tind.getConstValue() >= 0) )
		if (Tobj.isFixedArray()) // don't make ALENGTH quad
		    head = boundsCheck(q,head,Tobj.getArrayLength(),q.index());
		else // have to query for ALENGTH
		    head = boundsCheck(q,head, q.objectref(), q.index());
	    if (!Tobj.isNonNull())
		head = nullCheck(q, head, q.objectref());
	    ss.qm.put(q, head, nq);
	    ti.put(q.dst(), Type.top);
	    ti.put(q.objectref(), alsoNonNull(Tobj));
	}
	public void visit(ALENGTH q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq;
	    Type Tobj = ti.get(q.objectref());
	    if (!Tobj.isNonNull())
		head = nullCheck(q, head, q.objectref());
	    ss.qm.put(q, head, nq);
	    if (Tobj.isFixedArray())
		ti.put(q.dst(), new IntConst(Tobj.getArrayLength()));
	    else
		ti.put(q.dst(), Type.top);
	    ti.put(q.objectref(), alsoNonNull(Tobj));
	}
	public void visit(ANEW q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq;
	    Type Tdim = null;
	    for (int i=q.dimsLength()-1; i>=0; i--) {
		Tdim = ti.get(q.dims(i));
		if ( ! (Tdim.isIntConst() && Tdim.getConstValue() >= 0))
		    head = minusCheck(q, head, q.dims(i));
	    }
	    ss.qm.put(q, head, nq);
	    if (Tdim.isIntConst()) // type of first array dimension.
		ti.put(q.dst(), new FixedArray(Tdim.getConstValue()));
	    else ti.put(q.dst(), Type.nonnull);
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
		Util.assert(false); // FIXME
	    }
	    ti.put(q.objectref(), alsoNonNull(Tobj));
	}
	public void visit(ASET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head = nq;
	    Type Tobj = ti.get(q.objectref());
	    Type Tind = ti.get(q.index());
	    // do COMPONENTOF test.
	    head = componentCheck(q, head, q.objectref(), q.src());
	    if (! (Tobj.isFixedArray() &&
		   Tind.isIntConst() &&
		   Tind.getConstValue() < Tobj.getArrayLength() &&
		   Tind.getConstValue() >= 0) )
		if (Tobj.isFixedArray()) // don't make ALENGTH quad.
		    head = boundsCheck(q,head,Tobj.getArrayLength(),q.index());
		else // have to query for ALENGTH
		    head = boundsCheck(q,head, q.objectref(), q.index());
	    if (!Tobj.isNonNull())
		head = nullCheck(q, head, q.objectref());
	    ss.qm.put(q, head, nq);
	    ti.put(q.objectref(), alsoNonNull(Tobj));
	}
	public void visit(CALL q) {
	    Quad nq, head;
	    // if retex==null, add the proper checks.
	    if (q.retex()!=null) nq=head=(Quad)q.clone(qf, ss.ctm);
	    else {
		Temp Tex = ss.extra(0), Tnull = ss.extra(1), Tr = Tnull;
		head = new CALL(qf, q, q.method(), Quad.map(ss.ctm,q.params()),
				Quad.map(ss.ctm, q.retval()),
				Tex, q.isVirtual());
		Quad q0 = new CONST(qf, q, Tnull, null, HClass.Void);
		Quad q1 = new OPER(qf, q, Qop.ACMPEQ, Tr,
				   new Temp[] { Tex, Tnull });
		Quad q2 = new CJMP(qf, q, Tr, new Temp[0]);
		Quad q3 = new NOP(qf, q); // argh.
		Quad q4 = _throwException_(qf, q, Tex);
		Quad.addEdges(new Quad[] { head, q0, q1, q2, q4 });
		Quad.addEdge(q2, 1, q3, 0);
		nq = q3;
	    }
	    // if non-static, check that receiver is not null.
	    if (!q.isStatic() && !ti.get(q.params(0)).isNonNull())
		head = nullCheck(q, head, q.params(0));
	    ss.qm.put(q, head, nq);
	    // nothing known about return values or exceptions thrown.
	    if (q.retval()!=null) ti.put(q.retval(), Type.top);
	    if (q.retex()!=null) ti.put(q.retex(), Type.top);
	    if (!q.isStatic()) ti.put(q.params(0),
				      alsoNonNull(ti.get(q.params(0))));
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
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    if (!q.isStatic()) {
		Type Tobj = ti.get(q.objectref());
		if (!Tobj.isNonNull())
		    head = nullCheck(q, head, q.objectref());
		ti.put(q.objectref(), alsoNonNull(Tobj));
	    }
	    ss.qm.put(q, head, nq);
	    ti.put(q.dst(), Type.top);
	}
	/*public void visit(HEADER q);*/
	/*public void visit(INSTANCEOF q);*/
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
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    Type Tlck = ti.get(q.lock());
	    if (!Tlck.isNonNull())
		head = nullCheck(q, head, q.lock());
	    ss.qm.put(q, head, nq);
	    ti.put(q.lock(), alsoNonNull(Tlck));
	}
	public void visit(MONITOREXIT q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    Type Tlck = ti.get(q.lock());
	    if (!Tlck.isNonNull())
		head = nullCheck(q, head, q.lock());
	    ss.qm.put(q, head, nq);
	    ti.put(q.lock(), alsoNonNull(Tlck));
	}
	public void visit(MOVE q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    Type Tsrc = ti.get(q.src());
	    ss.qm.put(q, nq, nq);
	    ti.put(q.dst(), Tsrc);
	}
	public void visit(NEW q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    ti.put(q.dst(), Type.nonnull);
	}
	/*public void visit(NOP q);*/
	public void visit(OPER q) {
	    // if we were really ambitious, we'd do constant prop
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    Type Td = Type.top;
	    if (q.operandsLength()==2) {
		Type Tl = ti.get(q.operands(0));
		Type Tr = ti.get(q.operands(1));
		switch (q.opcode()) {
		case Qop.LDIV:
		case Qop.LREM:
		    head = longZeroCheck(q, head, q.operands(1));
		    break;
		case Qop.IDIV:
		    if (Tl.isIntConst() && Tr.isIntConst() &&
			Tr.getConstValue()!=0)
			Td = new IntConst(Tl.getConstValue() / 
					  Tr.getConstValue());
		    else head = intZeroCheck(q, head, q.operands(1));
		    break;
		case Qop.IREM:
		    if (Tl.isIntConst() && Tr.isIntConst() &&
			Tr.getConstValue()!=0)
			Td = new IntConst(Tl.getConstValue() %
					  Tr.getConstValue());
		    else head = intZeroCheck(q, head, q.operands(1));
		    break;
		case Qop.IADD:
		    if (Tl.isIntConst() && Tr.isIntConst())
			Td = new IntConst(Tl.getConstValue() +
					  Tr.getConstValue());
		    break;
		case Qop.ISUB:
		    if (Tl.isIntConst() && Tr.isIntConst())
			Td = new IntConst(Tl.getConstValue() -
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
	    ss.qm.put(q, head, nq);
	}
	/*public void visit(PHI q);*/
	/*public void visit(RETURN q);*/
	public void visit(SET q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    if (!q.isStatic()) {
		Type Tobj = ti.get(q.objectref());
		if (!Tobj.isNonNull())
		    head = nullCheck(q, head, q.objectref());
		ti.put(q.objectref(), alsoNonNull(Tobj));
	    }
	    ss.qm.put(q, head, nq);
	}
	/*public void visit(SIGMA q);*/
	/*public void visit(SWITCH q);*/
	public void visit(THROW q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head=nq;
	    Type Tthr = ti.get(q.throwable());
	    if (!Tthr.isNonNull())
		head = nullCheck(q, head, q.throwable());
	    ss.qm.put(q, head, nq);
	    ti.put(q.throwable(), alsoNonNull(Tthr));
	}
	public void visit(TYPECAST q) {
	    // translate as:
	    //  if (obj!=null && !(obj instanceof class))
	    //     throw new ClassCastException();
	    Quad nq = (Quad) q.clone(qf, ss.ctm), head;
	    Type Tobj = ti.get(q.objectref());
	    Temp Tr = ss.extra(0);
	    Quad q1 = new INSTANCEOF(qf, q, Tr,
				     Quad.map(ss.ctm, q.objectref()),
				     q.hclass());
	    Quad q2 = new CJMP(qf, q, Tr, new Temp[0]);
	    Quad q3 = _throwException_(qf, q, HCclasscastE);
	    Quad.addEdges(new Quad[] { q1, q2, q3 });
	    Quad.addEdge(q2, 1, nq, 0);
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
		head = q4;
	    }
	    ss.qm.put(q, head, nq);
	    // no change to type info, since we don't keep track of class
	}

	/// Add 'nonnull' attribute
	Type alsoNonNull(Type in) {
	    return in.isNonNull()?in:Type.nonnull;
	}

	//////////////// exceptions.
	private static final HClass HCarraystoreE = 
	    HClass.forName("java.lang.ArrayStoreException");
	private static final HClass HCnullpointerE =
	    HClass.forName("java.lang.NullPointerException");
	private static final HClass HCarrayindexE =
	    HClass.forName("java.lang.ArrayIndexOutOfBoundsException");
	private static final HClass HCnegativearrayE =
	    HClass.forName("java.lang.NegativeArraySizeException");
	private static final HClass HCarithmeticE =
	    HClass.forName("java.lang.ArithmeticException");
	private static final HClass HCclasscastE =
	    HClass.forName("java.lang.ClassCastException");

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
	Quad componentCheck(Quad old, Quad head, Temp Tobj, Temp Tsrc) {
	    QuadFactory qf = head.qf;
	    Temp Tr = ss.extra(0);
	    Quad q0 = new COMPONENTOF(qf, head, Tr,
				      Quad.map(ss.ctm, Tobj),
				      Quad.map(ss.ctm, Tsrc));
	    Quad q1 = new CJMP(qf, head, Tr, new Temp[0]);
	    Quad q2 = _throwException_(qf, old, HCarraystoreE);
	    Quad.addEdges(new Quad[] { q0, q1, q2 });
	    Quad.addEdge(q1, 1, head, 0);
	    return q0;
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
	Quad boundsCheck(Quad old, Quad head, int length, Temp Ttst) {
	    Quad q0 = new CONST(qf, head, ss.extra(0),
				new Integer(length), HClass.Int);
	    Quad q1 = _boundsCheck_(old, head, ss.extra(0),
				    Quad.map(ss.ctm, Ttst), ss.extra(1));
	    Quad.addEdge(q0, 0, q1, 0);
	    return q0;
	}
	Quad boundsCheck(Quad old, Quad head, Temp Tobj, Temp Ttst) {
	    Quad q0 = new ALENGTH(qf, head, ss.extra(0),
				  Quad.map(ss.ctm, Tobj));
	    Quad q1 = _boundsCheck_(old, head, ss.extra(0),
				    Quad.map(ss.ctm, Ttst), ss.extra(1));
	    Quad.addEdge(q0, 0, q1, 0);
	    return q0;
	}
	private Quad _boundsCheck_(Quad old, Quad head, Temp Tlen, Temp Ttst,
				   Temp Textra1) {
	    Util.assert(Tlen.tempFactory()==head.qf.tempFactory());
	    Util.assert(Ttst.tempFactory()==head.qf.tempFactory());
	    Util.assert(Textra1.tempFactory()==head.qf.tempFactory());
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
	private Quad _throwException_(QuadFactory qf, Quad old, HClass HCex) {
	    Temp Tex = ss.hm.Tex, Tex2 = ss.extra(0), Tnull = ss.extra(1);
	    Quad q0 = new NEW(qf, old, Tex, HCex);
	    Quad q1 = new CALL(qf, old, HCex.getConstructor(new HClass[0]),
			       new Temp[] { Tex }, null, Tex2, false);
	    Quad q2 = new CONST(qf, old, Tnull, null, HClass.Void);
	    Quad q3 = new OPER(qf, old, Qop.ACMPEQ, Tnull,
			       new Temp[] { Tex2, Tnull });
	    Quad q4 = new CJMP(qf, old, Tnull, new Temp[0]);
	    Quad q5 = new MOVE(qf, old, Tex, Tex2);
	    Quad q6 = new PHI(qf, old, new Temp[0], 2);
	    Quad q7 = _throwException_(qf, old, Tex);
	    Quad.addEdges(new Quad[] { q0, q1, q2, q3, q4, q5, q6, q7 });
	    Quad.addEdge(q4, 1, q6, 1);

	    return q0;
	}
	private Quad _throwException_(QuadFactory qf, Quad old, Temp Tex) {
	    HandlerSet hs = ss.hm.handlers(old);
	    if (hs==null) {
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
    }
}
