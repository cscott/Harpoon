// Translate.java, created Wed Dec  9 00:19:51 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Bytecode.Op;
import harpoon.IR.Bytecode.Operand;
import harpoon.IR.Bytecode.OpClass;
import harpoon.IR.Bytecode.OpConstant;
import harpoon.IR.Bytecode.OpField;
import harpoon.IR.Bytecode.OpLocalVariable;
import harpoon.IR.Bytecode.OpMethod;
import harpoon.IR.Bytecode.Instr;
import harpoon.IR.Bytecode.InGen;
import harpoon.IR.Bytecode.InCti;
import harpoon.IR.Bytecode.InMerge;
import harpoon.IR.Bytecode.InRet;
import harpoon.IR.Bytecode.InSwitch;
import harpoon.IR.Bytecode.Code.ExceptionEntry;
import harpoon.IR.Bytecode.Liveness; // liveness analysis on local variables
import harpoon.IR.Quads.HANDLER.ProtectedSet;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Collections.AbstractMapEntry;
import harpoon.Util.Default;
import harpoon.Util.MapComparator;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

/**
 * <code>Translate</code> is a utility class which implements a
 * bytecode-to-quad translation.  The result is a simple quad
 * form with no phi/sigma functions or exception handlers.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translate.java,v 1.3.2.1 2002-02-27 08:36:33 cananian Exp $
 */
final class Translate { // not public.
    static final private class StaticState {
	/** <code>QuadFactory</code> to use for this method. 
	 *  (contains <code>TempFactory</code> to use for this method.) */
	final QuadFactory qf;
	/** <code>Temp</code>s representing the bytecode stack. */
	final Temp[] stack;
	/** <code>Temp</code>s used for local variables. */
	final Temp lv[];
	/** Extra <code>Temp</code>s used for temporary values during
	 *  translation. */
	final List extra;
	/** HEADER quad for method. */
	final HEADER header;
	/** Try block information from original bytecode. */
	final ExceptionEntry[] tryBlocks;
	/** Mapping from <code>InMerge</code>s to corresponding 
	 *  <code>PHI</code>s. */
	final MergeMap mergeMap;
	/** Liveness information from bytecode. */
	final Liveness liveness;
	/** Mapping from <tryBlock,CallStack> to a HANDLER. */
	final SortedMap transHandler;
	/** To-do list for HANDLER construction. */
	final Stack todoHandler;

	/** Constructor. */
	StaticState(QuadFactory qf, int max_stack, int max_locals,
		    ExceptionEntry[] tryBlocks, HEADER header,
		    Liveness liveness) {
	    this.qf = qf;
	    stack = new Temp[max_stack];
	    for (int i=0; i<max_stack; i++)
		stack[i] = new Temp(qf.tempFactory(), "stk"+i+"_");
	    lv = new Temp[max_locals];
	    for (int i=0; i<max_locals; i++)
		lv[i] = new Temp(qf.tempFactory(), "lv"+i+"_");
	    extra = new ArrayList();

	    this.tryBlocks = tryBlocks;
	    this.header = header;
	    this.mergeMap = new MergeMap();
	    this.liveness = liveness;
	    this.transHandler =
		new TreeMap(new Comparator() {
		    final Comparator mapComparator =
		        new MapComparator(null,null);
		    public int compare(Object o1, Object o2) {
			List l1 = (List) o1, l2 = (List) o2;
			assert l1.size()==l2.size() && l1.size()==2;
			ExceptionEntry e1 = (ExceptionEntry) l1.get(0);
			ExceptionEntry e2 = (ExceptionEntry) l2.get(0);
			// null is larger than anything else.
			if (e1==null) return (e2==null)?0:1;
			if (e2==null) return (e1==null)?0:-1;
			int c = e1.compareTo(e2);
			if (c!=0) return c;
			Map m1 = (Map) l1.get(1);
			Map m2 = (Map) l2.get(1);
			return mapComparator.compare(m1, m2);
		    }
		});
	    this.todoHandler = new Stack();
	}	
	/** Get an "extra" <code>Temp</code>. */
	final Temp extra(int n) {
	    while (n >= extra.size())
		extra.add(new Temp(qf.tempFactory(),
				   "extra"+extra.size()+"_"));
	    return (Temp)extra.get(n);
	}
    }
    /** Auxillary class to implement mergeMap. */
    static final private class MergeMap {
	private final Map h = new HashMap();
	private List getList(InMerge m, Map calls) {
	    return (List) h.get(Arrays.asList(new Object[] {m, calls }));
	}

	boolean contains(InMerge m, Map calls) {
	    return (getList(m, calls)!=null);
	}
	PHI get(InMerge m, Map calls) {
	    return (PHI) getList(m, calls).get(0);
	}
	int arity(InMerge m, Map calls) {
	    return ((Integer) getList(m, calls).get(1)).intValue();
	}
	void put(InMerge m, Map calls, PHI p, int arity) {
	    h.put(Arrays.asList(new Object[] { m, calls }),
		  Arrays.asList(new Object[] { p, new Integer(arity) }));
	}
	public String toString() { return h.toString(); }

	void fixupPhis(State s) { // eliminate null limbs
	    for (Iterator i=h.entrySet().iterator(); i.hasNext(); ) {
		Map.Entry me = (Map.Entry) i.next();
		InMerge in= (InMerge) ((List)me.getKey()).get(0);
		PHI phi   = (PHI)     ((List)me.getValue()).get(0);
		int arity =((Integer) ((List)me.getValue()).get(1)).intValue();

		while (arity < phi.arity())
		    phi = phi.shrink(phi.arity()-1); // null branch.
		// might as well kill one-input phi functions while we're at it
		if (arity==1)
		    Quad.addEdge(phi.prev(0), phi.prevEdge(0).which_succ(),
				 phi.next(0), phi.nextEdge(0).which_pred());
		else s.recordHandler(in, phi, phi);
		    
	    }
	}
    }
    /** Auxillary stack to track JSR return addresses on the main stack. */
    static final private class JSRStack {
	/** Index of return address on the stack, or local variable. */
	final int index;
	/** Target of the JSR. */
	final Instr target;
	/** Link to rest of the list. */
	final JSRStack next;
	/** Constructor. Indices are always in increasing order. */
	JSRStack(int index, Instr target, JSRStack next) {
	    this.index=index; this.target=target; this.next=next;
	    assert next==null || index > next.index;
	    assert target!=null;
	}
	/** Find index. */
	static Instr getTarget(JSRStack js, int index) {
	    if (js==null || index > js.index) return null;
	    if (js.index==index) return js.target;
	    return getTarget(js.next, index);
	}
	/** Insert index into stack. */
	static JSRStack insert(JSRStack js, int index, Instr target) {
	    if (js==null || index > js.index)
		return new JSRStack(index, target, js);
	    return new JSRStack(js.index, js.target,
				insert(js.next, index, target));
	}
	/** Remove index from stack. */
	static JSRStack remove(JSRStack js, int index) {
	    if (js==null || index > js.index) return js;
	    if (js.index==index) return js.next;
	    JSRStack nnxt = remove(js.next, index);
	    if (js.next == nnxt) return js;
	    return new JSRStack(js.index, js.target, nnxt);
	}
	/** Human-readable string for debugging. */
	public String toString() { return asMap(this).toString(); }
	/** Collection view of target <code>Instr</code>s in stack. */
	static Map asMap(final JSRStack js) {
	    return new AbstractMap () {
		public Set entrySet() {
		    return new AbstractSet() {
			public int size() {
			    int size=0;
			    for (JSRStack jsp=js; jsp!=null; jsp=jsp.next)
				size++;
			    return size;
			}
			public boolean isEmpty() { return js==null; }
			public Iterator iterator() {
			    return new UnmodifiableIterator() {
				private JSRStack jsp=js;
				public boolean hasNext() { return jsp!=null; }
				public Object next() {
				    if (jsp==null)
					throw new NoSuchElementException();
				    final JSRStack oldjsp = jsp;
				    jsp = jsp.next;
				    return new AbstractMapEntry() {
					public Object getKey() {
					    return new Integer(oldjsp.index);
					}
					public Object getValue() {
					    return oldjsp.target;
					}
				    }; // END anonymous Map.Entry
				}
			    }; // END anonymous Iterator
			}
		    }; // END anonymous AbstractSet
		}
	    }; // END anonymous AbstractMap
	}
    }
    /** Auxilliary stack to track LONG values on main stack. */
    static final private class LongStack {
	/** Index of topmost LONG value on main stack. */
	final int index;
	/** Link to the rest of the LongStack. */
	final LongStack next;
	/** Constructor. */
	LongStack(int index, LongStack next) {
	    this.index = index; this.next = next;
	}
	/** Make data human-readable for debugging. */
	public String toString() {
	    StringBuffer sb=new StringBuffer("[");
	    for (LongStack lsp = this; lsp!=null; lsp=lsp.next) {
		sb.append(lsp.index);
		if (lsp.next!=null) sb.append(", ");
	    }
	    sb.append("]");
	    return sb.toString();
	}
    }
    static final private class State {
	/** Pointer to the static state for this method. */
	private final StaticState ss;
	/** Current size of the stack. */
	private final int stackSize;
	/** LongStack to track long values on the stack. */
	private final LongStack ls;
	/** JSRStack to track jsr targets. */
	private final JSRStack js;
	/** JSRStack to track jsr targets in local variables. */
	private final JSRStack jlv;
	/** Keep track of JSR call stack during translation. */
	private final Map calls; // unmodifiable.

	/** private constructor */
	private State(StaticState ss, int stackSize,
		      LongStack ls, JSRStack js, JSRStack jlv,
		      Map calls) {
	    this.ss = ss; this.stackSize = stackSize;
	    this.ls = ls; this.js = js; this.jlv = jlv;
	    this.calls = calls;
	}
	/** public constructor */
	State(QuadFactory qf, int max_stack, int max_locals, 
	      ExceptionEntry[] tryBlocks, HEADER header, Liveness liveness) {
	    this.ss = new StaticState(qf, max_stack, max_locals, tryBlocks,
				      header, liveness);
	    this.stackSize = 0;
	    this.ls = null;
	    this.js = null;
	    this.jlv= null;
	    this.calls = Default.EMPTY_MAP;
	}

	State pop()       { return pop(1); }
	State pop(int n)  { 
	    LongStack lsp = ls;
	    while (lsp!=null && lsp.index >= stackSize-n) lsp=lsp.next;
	    JSRStack jsp = js;
	    while (jsp!=null && jsp.index >= stackSize-n) jsp=jsp.next;
	    return new State(ss, stackSize-n, lsp, jsp, jlv, calls);
	}
	State push()      { return push(1); }
	State push(int n) {
	    return new State(ss, stackSize+n, ls, js, jlv, calls);
	}
	State pushLong()  {
	    return new State(ss, stackSize+2,
			     new LongStack(stackSize+1, ls), js, jlv, calls);
	}
	private State pushRetAddr(Instr target) { // called from enterJSR
	    return new State(ss, stackSize+1, ls,
			     new JSRStack(stackSize, target, js), jlv, calls);
	}
	
	Temp stack(int n) { return ss.stack[stackSize-n-1]; }
	Temp extra(int n) { return ss.extra(n); }
	Temp lv   (int n) { return ss.lv[n]; }

	boolean isLong(int n) {
	    int i = stackSize-n-1;
	    for (LongStack lp = ls; lp!=null && lp.index>=i; lp=lp.next)
		if (lp.index==i) return true;
	    return false;
	}

	/** mark a local variable as *not* containing a return address. */
	State clearLV(int lvIndex) {
	    JSRStack jlvp = JSRStack.remove(jlv, lvIndex);
	    return (jlv==jlvp) ? this :
		new State(ss, stackSize, ls, js, jlvp, calls);
	}
	/** track a return address going from local variables to the stack */
	State trackLV2S(int lvFrom, int stkTo) {
	    if (!isRetAddrLV(lvFrom)) return this;
	    JSRStack jsp=JSRStack.insert(js, stackSize-stkTo-1,
					 getJSRtargetLV(lvFrom));
	    return new State(ss, stackSize, ls, jsp, jlv, calls);
	}
	/** track a return address going from the stack to a local variable */
	State trackS2LV(int stkFrom, int lvTo) {
	    // lv is being rewritten either way, so remove lvTo from lv state.
	    JSRStack jlvp = JSRStack.remove(jlv, lvTo);
	    if (isRetAddrS(stkFrom))
		jlvp = JSRStack.insert(jlvp, lvTo, getJSRtargetS(stkFrom));
	    return (jlv==jlvp) ? this :
		new State(ss, stackSize, ls, js, jlvp, calls);
	}
	/** track a return address going from the stack to the stack. */
	State trackS2S(State old, int oldFrom, int newTo) {
	    if (!old.isRetAddrS(oldFrom)) return this;
	    JSRStack jsp = JSRStack.insert(js, stackSize-newTo-1,
					   old.getJSRtargetS(oldFrom));
	    return new State(ss, stackSize, ls, jsp, jlv, calls);
	}
	boolean isRetAddrS(int n)
	{ return JSRStack.getTarget(js, stackSize-n-1)!=null; }
	boolean isRetAddrLV(int n)
	{ return JSRStack.getTarget(jlv, n)!=null; }
	Instr getJSRtargetLV(int n)
	{ return JSRStack.getTarget(jlv, n); }
	Instr getJSRtargetS(int n)
	{ return JSRStack.getTarget(js, stackSize-n-1); }

	QuadFactory qf()  { return ss.qf; }
	TempFactory tf()  { return ss.qf.tempFactory(); }

	HEADER header() { return ss.header; }
	METHOD method() { return (METHOD) ss.header.next(1); }
	FOOTER footer() { return (FOOTER) ss.header.next(0); }

	MergeMap mergeMap() { return ss.mergeMap; }

	Iterator targets() {
	    final Set s = new HashSet();
	    targets2set(js, s);
	    targets2set(jlv,s);
	    return s.iterator();
	}
	private void targets2set(final JSRStack jp, final Set s) {
	    for (JSRStack p = jp; p!=null; p=p.next)
		s.add(p.target);
	}

	State enterCatch() { return new State(ss, 1, null, null, jlv, calls); }

	Stack todoHandler() { return ss.todoHandler; }

	METHOD fixupHandlers() {
	    // use the transHandler map to add proper edges from METHOD to
	    // all known HANDLER nodes.
	    METHOD qMold = method();
	    METHOD qM = new METHOD(qf(), qMold, qMold.params(), 
				   1 + ss.transHandler.size()/*arity*/);
	    // link up.
	    Edge e = qMold.nextEdge(0);
	    Quad.addEdge(qM, 0, (Quad)e.to(), e.which_pred());
	    Quad.addEdge(header(), 1, qM, 0);
	    // iterate through handlers, linking them.
	    int i=1;
	    for (Iterator it=ss.transHandler.values().iterator();it.hasNext();)
		Quad.addEdge(qM, i++, (HANDLER)it.next(), 0);
	    return qM;
	}
	HANDLER getHandler(ExceptionEntry tryBlock) {
	    List pair = Arrays.asList(new Object[] { tryBlock, this.calls });
	    // look up <tryBlock, callStack> tuple.
	    if (ss.transHandler.containsKey(pair))
		return (HANDLER) ss.transHandler.get(pair);
	    // ok, hafta make it from scratch.
	    State hS = this.enterCatch(); // preserve callstack.
	    HANDLER h = newHandler(hS, pair, tryBlock.handler(),
				   tryBlock.caughtException());
	    TransState ts = new TransState(hS, tryBlock.handler(), h, 0);
	    // add handler to 'todo' list.
	    ss.todoHandler.add(ts);
	    // return new handler.
	    return h;
	}
	HANDLER newHandler(State s, List hpair,
			   HCodeElement src, HClass caughtException) {
	    HANDLER h = new HANDLER(ss.qf, src, s.stack(0), caughtException,
				    new HANDLER.HashProtectSet());
	    // null hpair is special: it means we're synthesizing an outer
	    // handler context for a synchronized method.
	    if (hpair==null)
		hpair=Arrays.asList(new Object[] { null, Default.EMPTY_MAP });
	    // add <tryBlock, callStack> mapping.
	    ss.transHandler.put(hpair, h);
	    // return new handler.
	    return h;
	}
	HandlerSet handlers(Instr orig) {
	    HandlerSet hs = null;
	    for (int i=0; i<ss.tryBlocks.length; i++)
		if (ss.tryBlocks[i].inTry(orig)) {
		    HANDLER h = getHandler(ss.tryBlocks[i]);
		    hs = new HandlerSet(h, hs);
		}
	    return hs;
	}
	void recordHandler(Instr orig, Quad start, Quad end) {
	    for (HandlerSet hs=handlers(orig); hs!=null; hs=hs.next)
		recordHandler(hs.h, start, end);
	}
	void recordHandler(HANDLER h, Quad start, Quad end) {
		recordHandler(new HashSet(), start, end, 
			      h.protectedSet);
	}
	private void recordHandler(Set done, Quad start, Quad end,
				   ProtectedSet s) {
	    s.insert(start); done.add(start);
	    if (start!=end) {
		Quad next[] = start.next();
		for (int i=0; i<next.length; i++)
		    if (!done.contains(next[i]))
			recordHandler(done, next[i], end, s);
	    }
	}
	/** update state when we reach a phi (remove dead return addresses) */
	State purgeDead(Instr phi) {
	    Map lv_targets = new HashMap(JSRStack.asMap(jlv));
	    // remove any targets that are not live at this phi.
	    lv_targets.keySet().retainAll(ss.liveness.liveSet(phi));
	    // add in targets live in the stack.
	    Set targets = new HashSet(JSRStack.asMap(js).values());
	    targets.addAll(lv_targets.values());
	    // now retain only those entries in the call stack
	    // corresponding to live targets.
	    Map ncalls = new HashMap(calls);
	    ncalls.keySet().retainAll(targets);
	    // for efficiency, don't create a new state if maps are identical.
	    return (calls.equals(ncalls)) ? this :
		new State(ss, stackSize, ls, js, jlv,
			  Collections.unmodifiableMap(ncalls));
	}
	/** return (unmodifiable) call state map. */
	Map calls() { return calls; }
	/** create a new state when we enter a subroutine. */
	State enterJSR(InCti jsr, Instr target) {
	    assert !calls.containsKey(target); // no recursiveness.
	    Map ncalls = new HashMap(calls);
	    ncalls.put(target, jsr);
	    return new State(ss, stackSize, ls, js, jlv,
			     Collections.unmodifiableMap(ncalls))
		.pushRetAddr(target);
	}
    }

    /** Extended state to keep track of translation process. */
    static final private class TransState {
	/** Initial state for translation. */
	final State initialState;
	/** <code>Instr</code> to translate. */
	final Instr in;
	/** <code>Quad</code> to which to append the translation. */
	final Quad  header;
	/** Exit edge of <code>header</code> to which to append the 
	 *  translation. */
	final int   which_succ;
	/** Constructor. */
	TransState(State initialState, Instr in, Quad header, int which_succ) {
	    this.initialState = initialState;
	    this.in = in;
	    this.header = header;
	    this.which_succ = which_succ;
	}
    }

    /** Return a <code>Quad</code> representation of the method code
     *  in <code>bytecode</code>. */
    static final Quad trans(harpoon.IR.Bytecode.Code bytecode,
			    harpoon.IR.Quads.Code  parent) {
	HMethod method   = bytecode.getMethod();
	boolean isStatic = method.isStatic();
	int     offset   = isStatic?0:1;

	ExceptionEntry[] tb = bytecode.getTryBlocks();

	// Make QuadFactory.
	QuadFactory qf = parent.qf;

	// Find first bytecode instruction, make header and footer quads.
	Instr firstInstr = (Instr) bytecode.getRootElement();

	FOOTER footer = new FOOTER(qf, firstInstr, 1);
	HEADER header = new HEADER(qf, firstInstr);
	Quad.addEdge(header, 0, footer, 0);

	// deterimine if this is a synchronized method.
	boolean isSynchronized=Modifier.isSynchronized(method.getModifiers());

	// find how much stack to allocate.  when translating a synchronized
	// method, we need at least one stack space (for a constructed try
	// block) even if the original method needed none.
	int maxstack = bytecode.getMaxStack();
	if (isSynchronized && maxstack < 1) maxstack=1;

	// set up initial state.
	State s=new State(qf, maxstack, bytecode.getMaxLocals(),
			  tb /* exception handling information */,
			  header /* header quad for method. */,
			  new Liveness(bytecode) /*local variable liveness*/);

 	// Make parameter array, then make METHOD.
	HClass[] paramTypes = method.getParameterTypes();
	Temp[] params = new Temp[paramTypes.length+offset];
	
	for (int i=0, j=0; i<params.length; i++) {
	    params[i] = s.lv(i+j);
	    if (i>=offset && isLongDouble(paramTypes[i-offset])) j++;
	}
	/** Make METHOD with arity 1; we'll fill in the proper arity later. */
	METHOD qM = new METHOD(qf, firstInstr, params, 1);
	Quad.addEdge(header, 1, qM, 0);

	/* From section 8.4.3.5 of the Java Language Specification: */
	/* A synchronized method acquires a lock (17.1) before it
	 * executes. For a class (static) method, the lock associated
	 * with the Class object (20.3) for the method's class is
	 * used. For an instance method, the lock associated with
	 * 'this' (the object for which the method was invoked) is
	 * used. These are the same locks that can be used by the
	 * synchronized statement (14.17). */
	/* In particular, static synchronized methods are equivalent to:
	 *   synchronized (Class.forName("CLASSNAME")) { ...method body... }
	 * wrapped in a catch of ClassNotFoundException. */

	Temp lock = null;

	// if method is synchronized, place MONITORENTER at top.
	Quad q = qM;
	if (isSynchronized) {
	    if (!isStatic) { // virtual synchronized is easy.
		lock = s.lv(0); // 'this'
		q = new MONITORENTER(qf, firstInstr, lock);
		Quad.addEdge(qM, 0, q, 0);
	    } else { // static synchronized, what a kludge.
		// lock is Class.forName(this.class)
		lock = new Temp(s.tf(), "lock");
		Quad qq1 = new CONST(qf, q, lock, method.getDeclaringClass(),
				     qf.getLinker().forClass(Class.class));
		Quad qq2 = new MONITORENTER(qf, q, lock);
		// okay, link 'em up.
		Quad.addEdges(new Quad[] {  qM, qq1, qq2 });
		q = qq2;
	    }
	}

	// translate using state.
	trans(new TransState(s, firstInstr, q, 0));
	// make new handler for MONITOREXIT if synchronized
	HANDLER exithand = isSynchronized ?
	    // creating this new handler requires one stack space.
	    s.newHandler(s.enterCatch(), null, q, null) : null;
	// fixup initial METHOD block to link to the proper # of HANDLERs.
	qM = s.fixupHandlers();

	// if method is synchronized, make inclusive HANDLER w/ MONITOREXIT
	if (isSynchronized) {
	    assert lock!=null;
	    // make HANDLER for all throw exits
	    Quad Qm = new MONITOREXIT(qf, exithand, lock);
	    Quad Qt = new THROW(qf, Qm, exithand.exceptionTemp());
	    Quad.addEdges(new Quad[] { exithand, Qm, Qt });
	    s.footer().attach(Qt, 0);
	    // add just about everything to the handler set
	    s.recordHandler(exithand, q.next(0), s.footer());
	    for (int i=1; i < qM.nextLength(); i++)
		if (qM.next(i)!=exithand)
		    s.recordHandler(exithand, qM.next(i), s.footer());
	    // now insert MONITOREXIT for all RETURN predecessors of FOOTER
	    for (int i=1; i < s.footer().arity(); i++) { // skip HEADER edge
		if (!(s.footer().prev(i) instanceof RETURN)) continue;
		// put a MONITOREXIT before the return.
		Quad Qexit = s.footer().prev(i);
		assert Qexit.prev.length==1; // only one predecessor.
		Qm = new MONITOREXIT(qf, Qexit, lock);
		Edge e = Qexit.prevEdge(0);
		Quad.addEdge((Quad)e.from(), e.which_succ(), Qm, 0);
		Quad.addEdge(Qm, 0, (Quad)e.to(), e.which_pred());
	    }
	}

	// return result.
	return header;
    }

    /** Translate blocks starting with the given <code>TransState</code>s.<p>
     *  Start at <code>ts0.in</code> using <code>ts0.initialState</code>. */
    static final void trans(TransState ts0) {
	State s = ts0.initialState; // abbreviation.

	Stack todo = new Stack();
	todo.push(ts0);

	// pull stuff to do from todo stack and handler todo stack.
	while (! (todo.empty() && s.todoHandler().empty())) {
	    TransState ts = (TransState)
		( (!todo.empty()) ? todo.pop() : s.todoHandler().pop() );

	    TransState nts[] = transInstr(ts);
	    for (int i=nts.length-1; i>=0; i--)
		todo.push(nts[i]);
	}
	// JSR/RETs leave null edges into PHIs
	s.mergeMap().fixupPhis(s);
	// done.
	return;
    }

    /** Translate a single instruction.
     * @return the <code>TransState</code>s of the following instructions.
     */
    static final TransState[] transInstr(TransState ts) {
	Instr in = ts.in; byte opc = in.getOpcode();
	// Dispatch to correct specific function.
	if (in instanceof InGen)    return transInGen(ts);
	if (in instanceof InSwitch) return transInSwitch(ts);
	if (in instanceof InCti)    return transInCti(ts);
	if (in instanceof InMerge)  return transInMerge(ts);
	throw new Error("Unknown Instr type.");
    }

    /** Translate an <code>InGen</code>. 
     * @return a <code>TransState[]</code> of length one. */
    static final TransState[] transInGen(TransState ts) {
	QuadFactory qf = ts.initialState.qf();
	InGen in = (InGen) ts.in;
	Instr inNext = in.next(0); // assert in.next().length==1;
	byte opcode = in.getOpcode();
	State s = ts.initialState;
	TransState[] r = null;
	State ns;
	Quad q;
	Quad last = null; int which_succ = 0;

	switch(opcode) {
	case Op.AALOAD:
	case Op.BALOAD:
	case Op.CALOAD:
	case Op.DALOAD:
	case Op.FALOAD:
	case Op.IALOAD:
	case Op.LALOAD:
	case Op.SALOAD:
	    {
	    if (opcode==Op.DALOAD ||
		opcode==Op.LALOAD)
		ns = s.pop(2).pushLong(); // 64-bit val.
	    else
		ns = s.pop(2).push(); // 32-bit val
	    HClass type;
	    switch(opcode) {
	    case Op.BALOAD: type = HClass.Byte; break;
	    case Op.CALOAD: type = HClass.Char; break;
	    case Op.DALOAD: type = HClass.Double; break;
	    case Op.FALOAD: type = HClass.Float; break;
	    case Op.IALOAD: type = HClass.Int; break;
	    case Op.LALOAD: type = HClass.Long; break;
	    case Op.SALOAD: type = HClass.Short; break;
	    case Op.AALOAD: type = qf.getLinker().forName("java.lang.Object");
	                    break;
	    default: throw new Error("impossible");
	    }
	    Temp Tobj  = s.stack(1);
	    Temp Tindex= s.stack(0);
	    q = new AGET(qf, in, ns.stack(0), Tobj, Tindex, type);
	    break;
	    }
	case Op.AASTORE:
	case Op.BASTORE:
	case Op.CASTORE:
	case Op.DASTORE:
	case Op.FASTORE:
	case Op.IASTORE:
	case Op.LASTORE:
	case Op.SASTORE:
	    {
	    HClass type;
	    switch(opcode) {
	    case Op.BASTORE: type = HClass.Byte; break;
	    case Op.CASTORE: type = HClass.Char; break;
	    case Op.DASTORE: type = HClass.Double; break;
	    case Op.FASTORE: type = HClass.Float; break;
	    case Op.IASTORE: type = HClass.Int; break;
	    case Op.LASTORE: type = HClass.Long; break;
	    case Op.SASTORE: type = HClass.Short; break;
	    case Op.AASTORE: type = qf.getLinker().forName("java.lang.Object");
	                    break;
	    default: throw new Error("impossible");
	    }
	    Temp Tobj, Tindex, Tsrc;
	    if (opcode==Op.DASTORE ||
		opcode==Op.LASTORE) { // 64-bit val.
		ns = s.pop(4);
		Tobj   = s.stack(3);
		Tindex = s.stack(2);
		Tsrc   = s.stack(0);
	    } else { // 32-bit val.
		ns = s.pop(3);
		Tobj   = s.stack(2);
		Tindex = s.stack(1);
		Tsrc   = s.stack(0);
	    }
	    
	    // the actual operation.
	    q = new ASET(qf, in, Tobj, Tindex, Tsrc, type);
	    break;
	    }
	case Op.ACONST_NULL:
	    ns = s.push();
	    q = new CONST(qf, in, ns.stack(0), null, HClass.Void);
	    break;
	case Op.ALOAD:
	case Op.ALOAD_0:
	case Op.ALOAD_1:
	case Op.ALOAD_2:
	case Op.ALOAD_3:
	    {
		OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
		// could be a return address, so fix up the stack.
		ns = s.push().trackLV2S(opd.getIndex(), 0);
		q = new MOVE(qf, in, ns.stack(0), s.lv(opd.getIndex()));
		break;
	    }
	case Op.ANEWARRAY:
	    {
		OpClass opd = (OpClass) in.getOperand(0);
		HClass hc = qf.getLinker().forDescriptor("[" + 
						 opd.value().getDescriptor());
		ns = s.pop().push();
		q = new ANEW(qf, in, ns.stack(0), hc,
			     new Temp[] { s.stack(0) });
		break;
	    }
	case Op.ARRAYLENGTH:
	    {
	    ns = s.pop().push();
	    q = new ALENGTH(qf, in, ns.stack(0), s.stack(0));
	    break;
	    }
	case Op.ASTORE:
	case Op.ASTORE_0:
	case Op.ASTORE_1:
	case Op.ASTORE_2:
	case Op.ASTORE_3:
	    {
	    OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
	    // value could be return address so track it.
	    ns = s.trackS2LV(0, opd.getIndex()).pop();
	    q = new MOVE(qf, in, ns.lv(opd.getIndex()), s.stack(0));
	    break;
	    }
	case Op.BIPUSH:
	case Op.SIPUSH:
	    {
	    OpConstant opd = (OpConstant) in.getOperand(0);
	    int val = ((Number)opd.getValue()).intValue();
	    ns = s.push();
	    q = new CONST(qf, in, ns.stack(0), new Integer(val), HClass.Int);
	    break;
	    }
	case Op.CHECKCAST:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    Temp Tobj = s.stack(0);
	    q = new TYPECAST(qf, in, Tobj, opd.value());
	    ns = s;
	    break;
	    }
	case Op.D2F:
	case Op.D2I:
	case Op.L2F:
	case Op.L2I:
	    ns = s.pop(2).push(); assert s.isLong(0);
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] { s.stack(0) });
	    break;
	case Op.D2L:
	case Op.L2D:
	    ns = s.pop(2).pushLong(); assert s.isLong(0) && ns.isLong(0);
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] { s.stack(0) });
	    break;
	case Op.DADD:
	case Op.DDIV:
	case Op.DMUL:
	case Op.DREM:
	case Op.LADD:
	case Op.LAND:
	case Op.LMUL:
	case Op.LOR:
	case Op.LXOR:
	case Op.LDIV:
	case Op.LREM:
	    ns = s.pop(4).pushLong();
	    assert ns.isLong(0) && s.isLong(0) && s.isLong(2);
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] { s.stack(2), s.stack(0) });
	    break;
	case Op.DSUB:
	case Op.LSUB:
	    {
	    ns = s.pop(4).pushLong();
	    assert ns.isLong(0) && s.isLong(0) && s.isLong(2);
	    Quad q0 = new OPER(qf, in, (opcode==Op.DSUB)?Qop.DNEG:Qop.LNEG,
			       s.stack(0), new Temp[] { s.stack(0) });
	    Quad q1 = new OPER(qf, in, (opcode==Op.DSUB)?Qop.DADD:Qop.LADD,
			       ns.stack(0), new Temp[]{s.stack(2),s.stack(0)});
	    Quad.addEdge(q0, 0, q1, 0);
	    q = q0; last = q1;
	    break;
	    }
	case Op.DCMPG:
	case Op.DCMPL:
	    switch (inNext.getOpcode()) {
	    case Op.IFLT: case Op.IFGT: // special cases.
	    case Op.IFLE: case Op.IFGE:
	    case Op.IFEQ: case Op.IFNE:
	    {
		boolean invert = false, swap = false;
		int op;
		switch (inNext.getOpcode()) {
		case Op.IFNE: invert = true;
		case Op.IFEQ: op = Qop.DCMPEQ; break;
		case Op.IFLT: swap = true;
		case Op.IFGT: op = Qop.DCMPGT; break;
		case Op.IFLE: swap = true;
		case Op.IFGE: op = Qop.DCMPGE; break;
		default: throw new Error("Impossible!");
		}
		// NaN handling.
		if (op!=Qop.DCMPEQ &&
		    ((opcode==Op.DCMPG && !swap) ||
		     (opcode==Op.DCMPL &&  swap) ) ) { // swap handling of NaN
		    swap=!swap; invert=true;
		    op=(op==Qop.DCMPGT)?Qop.DCMPGE:Qop.DCMPGT;
		}
		ns = s.pop(4); // IF?? pops off result of DCMP?
		q = new OPER(qf, in, op, ns.extra(0),
			     swap
			     ? new Temp[] { s.stack(0), s.stack(2) }
			     : new Temp[] { s.stack(2), s.stack(0) } );
		last = new CJMP(qf, in, ns.extra(0), new Temp[0]);
		Quad.addEdge(q, 0, last, 0);
		r = new TransState[] {
		    new TransState(ns, inNext.next(0), last, invert?1:0),
		    new TransState(ns, inNext.next(1), last, invert?0:1)
		};
		break; // done translating.
	    }
	    default: // use full expansion of dcmp
	    {
	    boolean isDCMPG = (opcode==Op.DCMPG);
	    ns = s.pop(4).push(); assert s.isLong(0) && s.isLong(2);
	    Quad q0 = new OPER(qf, in, Qop.DCMPGT, s.extra(0),
			       isDCMPG ?
			       new Temp[] { s.stack(0), s.stack(2) } :
			       new Temp[] { s.stack(2), s.stack(0) } );
	    Quad q1 = new CJMP(qf, in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(qf, in, Qop.DCMPEQ, s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q3 = new CJMP(qf, in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(qf, in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(qf, in, ns.stack(0), new Integer( 0), HClass.Int);
	    Quad q6 = new CONST(qf, in, ns.stack(0), new Integer( 1), HClass.Int);
	    Quad q7 = new PHI(qf, in, new Temp[0], 3);
	    // link.
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q1, 1, isDCMPG?q4:q6, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, isDCMPG?q6:q4, 0);
	    Quad.addEdge(q3, 1, q5, 0);
	    Quad.addEdge(q4, 0, q7, 0);
	    Quad.addEdge(q5, 0, q7, 1);
	    Quad.addEdge(q6, 0, q7, 2);
	    // setup next state.
	    q = q0; last = q7;
	    break;
	    }
	    }
	    break;
	case Op.DCONST_0:
	case Op.DCONST_1:
	case Op.LCONST_0:
	case Op.LCONST_1:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		ns = s.pushLong();
		q = new CONST(qf, in, ns.stack(0),
			      opd.getValue(), opd.getType());
		break;
	    }
	case Op.DLOAD:
	case Op.DLOAD_0:
	case Op.DLOAD_1:
	case Op.DLOAD_2:
	case Op.DLOAD_3:
	case Op.LLOAD:
	case Op.LLOAD_0:
	case Op.LLOAD_1:
	case Op.LLOAD_2:
	case Op.LLOAD_3:
	    {
		OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
		ns = s.pushLong();
		q = new MOVE(qf, in, ns.stack(0), s.lv(opd.getIndex()));
		break;
	    }
	case Op.DNEG:
	case Op.LNEG:
	    ns = s.pop(2).pushLong(); assert s.isLong(0);
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.DSTORE:
	case Op.DSTORE_0:
	case Op.DSTORE_1:
	case Op.DSTORE_2:
	case Op.DSTORE_3:
	case Op.LSTORE:
	case Op.LSTORE_0:
	case Op.LSTORE_1:
	case Op.LSTORE_2:
	case Op.LSTORE_3:
	    {
	    OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
	    // values can't be return addresses, so clear tag in stack.
	    ns = s.pop(2).clearLV(opd.getIndex()); assert s.isLong(0);
	    q = new MOVE(qf, in, ns.lv(opd.getIndex()), s.stack(0));
	    break;
	    }
	case Op.DUP:
	    // value could be a return address so track it.
	    ns = s.push().trackS2S(s, 0, 0); assert !s.isLong(0);
	    q = new MOVE(qf, in, ns.stack(0), s.stack(0));
	    break;
	case Op.DUP_X1:
	    {
	    ns = s.pop(2).push(3); assert !s.isLong(0) && !s.isLong(1);
	    Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
	    Quad q1 = new MOVE(qf, in, ns.stack(1), s.stack(1));
	    Quad q2 = new MOVE(qf, in, ns.stack(2), ns.stack(0));
	    Quad.addEdges(new Quad[] { q0, q1, q2 });
	    q = q0;
	    last = q2;
	    // track possible return addresses
	    ns = ns.trackS2S(s, 0, 0).trackS2S(s, 1, 1).trackS2S(s, 0, 2);
	    break;
	    }
	case Op.DUP_X2: // operates differently if stack contains a long value
	    {
	    assert !s.isLong(0);
	    // don't forget to track possible return addresses.
	    if (s.isLong(1)) {
		ns = s.pop(3).push().pushLong().push();
		ns = ns.trackS2S(s, 0, 0).trackS2S(s, 0, 3); // retadd != long
	    } else {
		ns = s.pop(3).push(4);
		ns = ns.trackS2S(s, 0, 0).trackS2S(s, 1, 1)
		       .trackS2S(s, 2, 2).trackS2S(s, 0, 3);
	    }
	    Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
	    Quad q1 = new MOVE(qf, in, ns.stack(1), s.stack(1));
	    Quad q2 = new MOVE(qf, in, ns.stack(2), s.stack(2));
	    Quad q3 = new MOVE(qf, in, ns.stack(3), ns.stack(0));
	    if (s.isLong(1))
		Quad.addEdges(new Quad[] { q0, q1, q3 });
	    else
		Quad.addEdges(new Quad[] { q0, q1, q2, q3 });
	    q = q0; last = q3;
	    break;
	    }
	case Op.DUP2: // either dup two short values or one long value.
	    if (s.isLong(0)) {
		ns = s.pushLong();
		q = new MOVE(qf, in, ns.stack(0), s.stack(0));
	    } else {
		// could be return address.
		ns = s.push(2).trackS2S(s, 0, 0).trackS2S(s, 1, 1);
		Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
		Quad q1 = new MOVE(qf, in, ns.stack(1), s.stack(1));
		Quad.addEdges(new Quad[] { q0, q1 } );
		q = q0; last = q1;
	    }
	    break;
	case Op.DUP2_X1: // again, different if top value is long.
	    assert !s.isLong(2);
	    if (s.isLong(0)) { // only !long in center could be ret addr.
		ns = s.pop(3).pushLong().push().pushLong().trackS2S(s, 2, 2);
		Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
		Quad q1 = new MOVE(qf, in, ns.stack(2), s.stack(2));
		Quad q2 = new MOVE(qf, in, ns.stack(3), ns.stack(0));
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		q = q0; last = q2;
	    } else { // ack.  lots of possible return addresses.
		ns = s.pop(3).push(5);
		ns = ns.trackS2S(s, 0, 0).trackS2S(s, 1, 1).trackS2S(s, 2, 2)
		       .trackS2S(s, 0, 3).trackS2S(s, 1, 4);
		Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
		Quad q1 = new MOVE(qf, in, ns.stack(1), s.stack(1));
		Quad q2 = new MOVE(qf, in, ns.stack(2), s.stack(2));
		Quad q3 = new MOVE(qf, in, ns.stack(3), ns.stack(0));
		Quad q4 = new MOVE(qf, in, ns.stack(4), ns.stack(1));
		Quad.addEdges(new Quad[] { q0, q1, q2, q3, q4 });
		q = q0; last = q4;
	    }
	    break;
	case Op.DUP2_X2: // ack.  top and next could both be long.
	    {
	    if (s.isLong(0))
		ns = s.isLong(2)
		    ? s.pushLong()
		    : s.pop(4).pushLong().push(2).pushLong()
                       .trackS2S(s, 2, 2).trackS2S(s, 3, 3);
	    else
		ns = s.isLong(2)
		    ? s.pop(4).push(2).pushLong().push(2)
                       .trackS2S(s, 0, 0).trackS2S(s, 1, 1)
                       .trackS2S(s, 0, 4).trackS2S(s, 1, 5)
		    : s.pop(4).push(6) // ack. lots of possible ret addresses
                       .trackS2S(s, 0, 0).trackS2S(s, 1, 1)
                       .trackS2S(s, 2, 2).trackS2S(s, 3, 3)
                       .trackS2S(s, 0, 4).trackS2S(s, 1, 5);
	    Quad q0 = new MOVE(qf, in, ns.stack(0), s.stack(0));
	    Quad q1 = new MOVE(qf, in, ns.stack(1), s.stack(1));
	    Quad q2 = new MOVE(qf, in, ns.stack(2), s.stack(2));
	    Quad q3 = new MOVE(qf, in, ns.stack(3), s.stack(3));
	    Quad q4 = new MOVE(qf, in, ns.stack(4), ns.stack(0));
	    Quad q5 = new MOVE(qf, in, ns.stack(5), ns.stack(1));
	    if (s.isLong(0))
		Quad.addEdge(q0, 0, q2, 0);
	    else {
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		Quad.addEdge(q4, 0, q5, 0);
	    }
	    if (s.isLong(2))
		Quad.addEdge(q2, 0, q4, 0);
	    else
		Quad.addEdges(new Quad[] { q2, q3, q4 });
	    q = q0;
	    last = s.isLong(0)?q4:q5;
	    assert s.isLong(0)==ns.isLong(0);
	    assert s.isLong(0)==ns.isLong(4);
	    assert s.isLong(2)==ns.isLong(2);
	    break;
	    }
	case Op.F2D:
	case Op.F2L:
	case Op.I2D:
	case Op.I2L:
	    ns = s.pop().pushLong();
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.F2I:
	case Op.I2B:
	case Op.I2C:
	case Op.I2F:
	case Op.I2S:
	    ns = s.pop().push();
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.FADD:
	case Op.FDIV:
	case Op.FMUL:
	case Op.FREM:
	case Op.IADD:
	case Op.IAND:
	case Op.IMUL:
	case Op.IOR:
	case Op.ISHL:
	case Op.ISHR:
	case Op.IUSHR:
	case Op.IXOR:
	case Op.IDIV:
	case Op.IREM:
	    ns = s.pop(2).push();
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] {s.stack(1), s.stack(0)});
	    break;
	case Op.FSUB:
	case Op.ISUB:
	    {
	    ns = s.pop(2).push();
	    Quad q0 = new OPER(qf, in, (opcode==Op.FSUB)?Qop.FNEG:Qop.INEG,
			       s.stack(0), new Temp[] { s.stack(0) });
	    Quad q1 = new OPER(qf, in, (opcode==Op.FSUB)?Qop.FADD:Qop.IADD,
			       ns.stack(0), new Temp[]{s.stack(1),s.stack(0)});
	    Quad.addEdge(q0, 0, q1, 0);
	    q = q0; last = q1;
	    break;
	    }
	case Op.FCMPG:
	case Op.FCMPL:
	    switch (inNext.getOpcode()) { // break this into FCMPGT,etc
	    case Op.IFLT: case Op.IFGT: // special cases.
	    case Op.IFLE: case Op.IFGE:
	    case Op.IFEQ: case Op.IFNE:
	    {
		boolean invert = false, swap = false;
		int op;
		switch (inNext.getOpcode()) {
		case Op.IFNE: invert = true;
		case Op.IFEQ: op = Qop.FCMPEQ; break;
		case Op.IFLT: swap = true;
		case Op.IFGT: op = Qop.FCMPGT; break;
		case Op.IFLE: swap = true;
		case Op.IFGE: op = Qop.FCMPGE; break;
		default: throw new Error("Impossible!");
		}
		// NaN handling.
		if (op!=Qop.FCMPEQ &&
		    ((opcode==Op.FCMPG && !swap) ||
		     (opcode==Op.FCMPL &&  swap) ) ) { // swap handling of NaN
		    swap=!swap; invert=true;
		    op=(op==Qop.FCMPGT)?Qop.FCMPGE:Qop.FCMPGT;
		}
		ns = s.pop(2); // IF?? pops off result of FCMP?
		q = new OPER(qf, in, op, ns.extra(0),
			     swap
			     ? new Temp[] { s.stack(0), s.stack(1) }
			     : new Temp[] { s.stack(1), s.stack(0) } );
		last = new CJMP(qf, in, ns.extra(0), new Temp[0]);
		Quad.addEdge(q, 0, last, 0);
		r = new TransState[] {
		    new TransState(ns, inNext.next(0), last, invert?1:0),
		    new TransState(ns, inNext.next(1), last, invert?0:1)
		};
		break; // done translating.
	    }
	    default: // use full expansion of fcmp
	    {
	    boolean isFCMPG = (opcode==Op.FCMPG);
	    ns = s.pop(2).push();
	    Quad q0 = new OPER(qf, in, Qop.FCMPGT, s.extra(0),
			       isFCMPG ?
			       new Temp[] { s.stack(0), s.stack(1) } :
			       new Temp[] { s.stack(1), s.stack(0) } );
	    Quad q1 = new CJMP(qf, in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(qf, in, Qop.FCMPEQ, s.extra(0),
			       new Temp[] { s.stack(1), s.stack(0) });
	    Quad q3 = new CJMP(qf, in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(qf, in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(qf, in, ns.stack(0), new Integer( 0), HClass.Int);
	    Quad q6 = new CONST(qf, in, ns.stack(0), new Integer( 1), HClass.Int);
	    Quad q7 = new PHI(qf, in, new Temp[0], 3);
	    // link.
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q1, 1, isFCMPG?q4:q6, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, isFCMPG?q6:q4, 0);
	    Quad.addEdge(q3, 1, q5, 0);
	    Quad.addEdge(q4, 0, q7, 0);
	    Quad.addEdge(q5, 0, q7, 1);
	    Quad.addEdge(q6, 0, q7, 2);
	    // setup next state.
	    q = q0; last = q7;
	    break;
	    }
	    }
	    break;
	case Op.FCONST_0:
	case Op.FCONST_1:
	case Op.FCONST_2:
	case Op.ICONST_M1:
	case Op.ICONST_0:
	case Op.ICONST_1:
	case Op.ICONST_2:
	case Op.ICONST_3:
	case Op.ICONST_4:
	case Op.ICONST_5:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		ns = s.push();
		q = new CONST(qf, in, ns.stack(0),
			      opd.getValue(), opd.getType());
		break;
	    }
	case Op.FLOAD:
	case Op.FLOAD_0:
	case Op.FLOAD_1:
	case Op.FLOAD_2:
	case Op.FLOAD_3:
	case Op.ILOAD:
	case Op.ILOAD_0:
	case Op.ILOAD_1:
	case Op.ILOAD_2:
	case Op.ILOAD_3:
	    {
		OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
		ns = s.push();
		q = new MOVE(qf, in, ns.stack(0), s.lv(opd.getIndex()));
		break;
	    }
	case Op.FNEG:
	case Op.INEG:
	    ns = s.pop().push();
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.FSTORE:
	case Op.FSTORE_0:
	case Op.FSTORE_1:
	case Op.FSTORE_2:
	case Op.FSTORE_3:
	case Op.ISTORE:
	case Op.ISTORE_0:
	case Op.ISTORE_1:
	case Op.ISTORE_2:
	case Op.ISTORE_3:
	    {
	    OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
	    ns = s.pop().clearLV(opd.getIndex()); // not a return address
	    q = new MOVE(qf, in, ns.lv(opd.getIndex()), s.stack(0));
	    break;
	    }
	case Op.GETFIELD:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.pop().pushLong();
	    else // 32-bit value.
		ns = s.pop().push();

	    q = new GET(qf, in, ns.stack(0), opd.value(), s.stack(0));
	    break;
	    }
	case Op.GETSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.pushLong();
	    else // 32-bit value.
		ns = s.push();
	    q = new GET(qf, in, ns.stack(0), opd.value(),null/*no objectref*/);
	    break;
	    }
	case Op.IINC:
	    {
	    OpLocalVariable opd0 = (OpLocalVariable) in.getOperand(0);
	    OpConstant opd1 = (OpConstant) in.getOperand(1);
	    Temp Tc = s.extra(0);
	    ns = s;
	    Quad q0 = new CONST(qf, in, Tc,
				opd1.getValue(), opd1.getType());
	    Quad q1 = new OPER(qf, in, Qop.IADD, ns.lv(opd0.getIndex()),
			       new Temp[] { s.lv(opd0.getIndex()), Tc});
	    Quad.addEdge(q0, 0, q1, 0);
	    q = q0; last = q1;
	    break;
	    }
	case Op.INSTANCEOF:
	    { // no protection for <code>null</code>.
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.pop().push();
	    Quad q0 = new INSTANCEOF(qf, in,
				     ns.stack(0), s.stack(0), opd.value());
	    q = q0; last = q0;
	    break;
	    }
	case Op.INVOKEINTERFACE:
	case Op.INVOKESPECIAL:
	case Op.INVOKESTATIC:
	case Op.INVOKEVIRTUAL:
	    {
	    boolean isVirtual = (opcode!=Op.INVOKESPECIAL);
	    boolean isStatic = (opcode==Op.INVOKESTATIC);
	    OpMethod opd = (OpMethod) in.getOperand(0);
	    HClass paramtypes[] = opd.value().getParameterTypes();
	    Temp param[] = new Temp[paramtypes.length+(isStatic?0:1)];
	    int j=0; // count number of entries on the stack.
	    for (int i=paramtypes.length-1; i>=0; i--, j++) {
		param[i+(isStatic?0:1)] = s.stack(j);
		if (isLongDouble(paramtypes[i])) j++;
	    }
	    if (!isStatic) param[0]=s.stack(j++); // objectref.
	    Temp Tret;
	    if (opd.value().getReturnType()==HClass.Void) { 
		// no return value.
		ns = s.pop(j);
		Tret = null;
	    } else if (!isLongDouble(opd.value().getReturnType())) {
		// 32-bit return value.
		ns = s.pop(j).push();
		Tret = ns.stack(0);
	    } else { 
		// 64-bit return value.
		ns = s.pop(j).pushLong();
		Tret = ns.stack(0);
	    }
	    // Create CALL quad.
	    q = new CALL(qf, in, opd.value(), param, Tret, null, isVirtual,
			 false /* tail call */, new Temp[0]);
	    break;
	    }
	case Op.LCMP: // break this up into lcmpeq, lcmpgt, etc.
	    switch (inNext.getOpcode()) {
	    case Op.IFEQ: case Op.IFGT: case Op.IFGE: // special cases.
	    case Op.IFNE: case Op.IFLT: case Op.IFLE:
	    {
		boolean invert = false, swap = false;
		int op;
		switch (inNext.getOpcode()) {
		case Op.IFNE: invert=true;
		case Op.IFEQ: op = Qop.LCMPEQ;
		    break;
		case Op.IFGE: invert=true;
		case Op.IFLT: swap=true; op=Qop.LCMPGT;
		    break;
		case Op.IFLE: invert=true;
		case Op.IFGT: op = Qop.LCMPGT;
		    break;
		default: throw new Error("Impossible!");
		}
		ns = s.pop(4); // IF?? pops off result of LCMP.
		q = new OPER(qf, in, op, ns.extra(0),
			     swap
			     ? new Temp[] {s.stack(0), s.stack(2)}
			     : new Temp[] {s.stack(2), s.stack(0)} );
		last = new CJMP(qf, in, ns.extra(0), new Temp[0]);
		Quad.addEdge(q, 0, last, 0);
		r = new TransState[] {
		    new TransState(ns, inNext.next(0), last, invert?1:0),
		    new TransState(ns, inNext.next(1), last, invert?0:1)
		};
		break; // done translating.
	    }
	    default: // use full expansion of lcmp
	    { // optimization doesn't work well on this, unfortunately.
	    ns = s.pop(4).push(); assert s.isLong(0) && s.isLong(2);
	    Quad q0 = new OPER(qf, in, Qop.LCMPEQ, s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q1 = new CJMP(qf, in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(qf, in, Qop.LCMPGT, s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q3 = new CJMP(qf, in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(qf, in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(qf, in, ns.stack(0), new Integer(0), HClass.Int);
	    Quad q6 = new CONST(qf, in, ns.stack(0), new Integer(1), HClass.Int);
	    Quad q7 = new PHI(qf, in, new Temp[0], 3);
	    // link.
	    Quad.addEdges(new Quad[] { q0, q1, q2, q3, q4, q7});
	    Quad.addEdge(q1, 1, q5, 0);
	    Quad.addEdge(q3, 1, q6, 0);
	    Quad.addEdge(q5, 0, q7, 1);
	    Quad.addEdge(q6, 0, q7, 2);
	    // setup next state.
	    q = q0; last = q7;
	    break;
	    }
	    }
	    break;
	case Op.LDC:
	case Op.LDC_W:
	case Op.LDC2_W:
	    {
	    OpConstant opd = (OpConstant) in.getOperand(0);
	    if (isLongDouble(opd.getType()))
		ns = s.pushLong();
	    else
		ns = s.push();
	    q = new CONST(qf, in, ns.stack(0), opd.getValue(), opd.getType());
	    break;
	    }
	case Op.LSHL:
	case Op.LSHR:
	case Op.LUSHR:
	    ns = s.pop(3).pushLong(); assert s.isLong(1);
	    q = new OPER(qf, in, Qop.forString(Op.toString(opcode)),
			 ns.stack(0), new Temp[] { s.stack(1), s.stack(0) });
	    break;
	case Op.MONITORENTER:
	    ns = s.pop();
	    q = new MONITORENTER(qf, in, s.stack(0));
	    break;
	case Op.MONITOREXIT:
	    ns = s.pop();
	    q = new MONITOREXIT(qf, in, s.stack(0));
	    break;
	case Op.MULTIANEWARRAY:
	    {
		OpClass opd0 = (OpClass) in.getOperand(0);
		OpConstant opd1 = (OpConstant) in.getOperand(1);
		int dims = ((Integer) opd1.getValue()).intValue();
		ns = s.pop(dims).push();
		Temp Tdims[] = new Temp[dims];
		for (int i=0; i<dims; i++)
		    Tdims[i] = s.stack((dims-1)-i);

		q = new ANEW(qf, in, ns.stack(0), opd0.value(), Tdims);
		break;
	    }
	case Op.NEWARRAY:
	    {
		final byte T_BOOLEAN = 4;
		final byte T_CHAR = 5;
		final byte T_FLOAT = 6;
		final byte T_DOUBLE = 7;
		final byte T_BYTE = 8;
		final byte T_SHORT = 9;
		final byte T_INT = 10;
		final byte T_LONG = 11;

		OpConstant opd = (OpConstant) in.getOperand(0);
		byte type = ((Byte) opd.getValue()).byteValue();
		HClass arraytype;
		switch(type) {
		case T_BOOLEAN:
		    arraytype = qf.getLinker().forDescriptor("[Z"); break;
		case T_CHAR:
		    arraytype = qf.getLinker().forDescriptor("[C"); break;
		case T_FLOAT:
		    arraytype = qf.getLinker().forDescriptor("[F"); break;
		case T_DOUBLE:
		    arraytype = qf.getLinker().forDescriptor("[D"); break;
		case T_BYTE:
		    arraytype = qf.getLinker().forDescriptor("[B"); break;
		case T_SHORT:
		    arraytype = qf.getLinker().forDescriptor("[S"); break;
		case T_INT:
		    arraytype = qf.getLinker().forDescriptor("[I"); break;
		case T_LONG:
		    arraytype = qf.getLinker().forDescriptor("[J"); break;
		default:
		    throw new Error("Illegal NEWARRAY component type: "+type);
		}

		ns = s.pop().push();
		q = new ANEW(qf, in, ns.stack(0), arraytype, 
			     new Temp[] { s.stack(0) });
		// crunch down array initializers.
		HClass comptype = arraytype.getComponentType();
		TransState r0 = new TransState(ns, inNext, q, 0);
		for (TransState rOld = null; r0!=rOld; ) {
		    rOld = r0; r0 = processArrayInitializers(r0, comptype);
		}
		r = new TransState[] { r0 };
		last = q; // only record handlers for q.
		break;
	    }
	case Op.NEW:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.push();
	    q = new NEW(qf, in, ns.stack(0), opd.value());
	    break;
	    }
	case Op.NOP:
	    ns = s;
	    q = null; // new NOP(qf, in);
	    break;
	case Op.POP:
	    ns = s.pop(); q = null;
	    break;
	case Op.POP2:
	    ns = s.pop(2); q = null;
	    break;
	case Op.PUTFIELD:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    Temp Tobj;
	    if (isLongDouble(opd.value().getType())) { // 64-bit value.
		assert s.isLong(0) && !s.isLong(2);
		ns = s.pop(3);
		Tobj = s.stack(2);
	    }
	    else {
		assert !s.isLong(0) && !s.isLong(1);
		ns = s.pop(2);
		Tobj = s.stack(1);
	    }
	    q = new SET(qf, in, opd.value(), Tobj, s.stack(0));
	    break;
	    }
	case Op.PUTSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType())) // 64-bit value.
		ns = s.pop(2);
	    else
		ns = s.pop(1);
	    q = new SET(qf, in, opd.value(), null/*objectref*/, s.stack(0));
	    break;
	    }
	case Op.SWAP:
	    {
	    assert !s.isLong(0) && !s.isLong(1);
	    ns = s.pop(2).push(2).trackS2S(s, 0, 1).trackS2S(s, 1, 0);
	    Quad q0 = new MOVE(qf, in, s.extra(0), s.stack(0));
	    Quad q1 = new MOVE(qf, in, ns.stack(0), s.stack(1));
	    Quad q2 = new MOVE(qf, in, ns.stack(1), s.extra(0));
	    Quad.addEdges(new Quad[] { q0, q1, q2 });
	    q = q0; last = q2;
	    break;
	    }
	default:
	    throw new Error("Unknown InGen opcode.");
	}
	if (last == null) last = q;

	// Link new quad if necessary.
	if (q!=null)
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	else {
	    last = ts.header; which_succ = ts.which_succ;
	}

	// Cover these quads with the proper try handler.
	if (q!=null) ns.recordHandler(in, q, last);

	// return next translation state.
	if (r==null)
	    r=new TransState[]{new TransState(ns, inNext, last, which_succ)};
	return r;
    }

    /** 
     * Translate a single <code>InMerge</code>.
     */
    static final TransState[] transInMerge(TransState ts) {
	State s0 = ts.initialState;
	InMerge in = (InMerge) ts.in;
	QuadFactory qf = s0.qf();
	TransState[] r;

	// purge dead return addresses.
	State s = s0.purgeDead(in);
	Map calls = s.calls();

	PHI phi;
	int arity = 0;
	if (!s.mergeMap().contains(in, calls)) { // no previous PHI
	    phi = new PHI(qf, in, new Temp[0], in.arity());
	    r = new TransState[]{new TransState(s, in.next(0), phi, 0)};
	    s.recordHandler(in, phi, phi);
	} else {
	    phi = s.mergeMap().get(in, calls);
	    arity = s.mergeMap().arity(in, calls);
	    r = new TransState[0];
	}
	// code duplication while translating JSRs can cause more than the
	// expected number of entries into a phi.
	if (arity == phi.arity()) 
	    phi = phi.grow(new Temp[0], arity); // increase capacity by 1.

	Quad.addEdge(ts.header, ts.which_succ, phi, arity);
	s.mergeMap().put(in, calls, phi, arity+1);
	return r;
    }

    /** Translate a single <code>InSwitch</code>. */
    static final TransState[] transInSwitch(TransState ts) {
	QuadFactory qf = ts.initialState.qf();
	InSwitch in = (InSwitch) ts.in;
	State s = ts.initialState;
	State ns = s.pop();
	Instr nxt[] = in.next();
	// determine length of keys array
	int klen=0, map[] = new int[nxt.length-1];
	for (int i=1; i<nxt.length; i++)
	    if (nxt[i] != nxt[0])
		map[klen++] = i; // only key entries for non-default targets.
	// make keys array.
	int keys[] = new int[klen];
	for (int i=0; i<klen; i++)
	    keys[i] = in.key(map[i]);
	// make & link SWITCH quad.
	Quad q = new SWITCH(qf, in, s.stack(0), keys, new Temp[0]);
	Quad.addEdge(ts.header, ts.which_succ, q, 0);
	// Make next states.
	TransState[] r = new TransState[klen+1];
	for (int i=0; i<klen; i++)
	    r[i] = new TransState(ns, nxt[map[i]], q, i);
	r[klen] = new TransState(ns, nxt[0], q, klen);
	// record try info
	ns.recordHandler(in, q, q);
	return r;
    }

    /** Translate a single <code>InCti</code>. */
    static final TransState[] transInCti(TransState ts) {
	QuadFactory qf = ts.initialState.qf();
	InCti in = (InCti) ts.in;
	State s = ts.initialState;
	Quad q, last=null;
	TransState[] r;

	byte opcode = in.getOpcode();
	switch(opcode) {
	case Op.ARETURN:
	case Op.DRETURN:
	case Op.FRETURN:
	case Op.IRETURN:
	case Op.LRETURN:
	case Op.RETURN: // RETURN has no return value.
	    q = new RETURN(qf, in, (opcode==Op.RETURN)? null : s.stack(0));
	    r = new TransState[0];
	    s.footer().attach(q, 0);
	    break;
	case Op.ATHROW:
	    q = new THROW(qf, in, s.stack(0));
	    r = new TransState[0];
	    s.footer().attach(q, 0);
	    break;
	case Op.GOTO:
	case Op.GOTO_W:
	    q = null;
	    r = new TransState[] { new TransState(s, in.next(0), 
						  ts.header, ts.which_succ) };
	    break;
	case Op.IF_ACMPEQ:
	case Op.IF_ACMPNE:
	    {
		State ns = s.pop(2);
		q = new OPER(qf, in, Qop.ACMPEQ, s.extra(0), 
			     new Temp[] { s.stack(1), s.stack(0) });
		Quad q2 = new CJMP(qf, in, s.extra(0), new Temp[0]);
		Quad.addEdge(q, 0, q2, 0);
		int iffalse=0, iftrue=1;
		if (opcode == Op.IF_ACMPNE) { // invert things for NE.
		    iffalse=1; iftrue=0;
		}
		r = new TransState[] {
		    new TransState(ns, in.next(0), q2, iffalse),
		    new TransState(ns, in.next(1), q2, iftrue)
		};
		last = q2;
		break;
	    }
	case Op.IFNULL:
	case Op.IFNONNULL:
	    {
		State ns = s.pop();
		Quad q0 = new CONST(qf, in, s.extra(0), null, HClass.Void);
		Quad q1 = new OPER(qf, in, Qop.ACMPEQ, s.extra(0), 
				   new Temp[] { s.stack(0), s.extra(0) });
		Quad q2 = new CJMP(qf, in, s.extra(0), new Temp[0]);
		Quad.addEdges(new Quad[] { q0, q1, q2 });

		int iffalse=0, iftrue=1;
		if (opcode == Op.IFNONNULL) { // invert things
		    iffalse=1; iftrue=0;
		}
		q = q0; last = q2;
		r = new TransState[] {
		    new TransState(ns, in.next(0), q2, iffalse),
		    new TransState(ns, in.next(1), q2, iftrue)
		};
		break;
	    }
	case Op.IFEQ:
	case Op.IFNE:
	    {
		// optimize this case.
		State ns = s.pop();
		boolean invert = (opcode==Op.IFEQ);
		q = last = new CJMP(qf, in, s.stack(0), new Temp[0]);
		r = new TransState[] {
		    new TransState(ns, in.next(0), last, invert?1:0),
		    new TransState(ns, in.next(1), last, invert?0:1)
		};
		break;
	    }
	case Op.IFLT:
	case Op.IFGE:
	case Op.IFGT:
	case Op.IFLE:
	case Op.IF_ICMPEQ:
	case Op.IF_ICMPNE:
	case Op.IF_ICMPLT:
	case Op.IF_ICMPGE:
	case Op.IF_ICMPGT:
	case Op.IF_ICMPLE:
	    {
		State ns;

		boolean invert = false;
		boolean swap   = false;
		int op;
		switch (opcode) {
		case Op.IFNE:
		case Op.IF_ICMPNE:
		    invert = true;
		case Op.IFEQ:
		case Op.IF_ICMPEQ:
		    op = Qop.ICMPEQ;
		    break;
		case Op.IFGE:
		case Op.IF_ICMPGE:
		    invert = true;
		case Op.IFLT:
		case Op.IF_ICMPLT:
		    swap = true;
		    op = Qop.ICMPGT;
		    break;
		case Op.IFLE:
		case Op.IF_ICMPLE:
		    invert = true;
		case Op.IFGT:
		case Op.IF_ICMPGT:
		    op = Qop.ICMPGT;
		    break;
		default: throw new Error("Impossible!");
		}
		if (opcode>=Op.IFEQ && opcode<=Op.IFLE) {
		    ns = s.pop();
		    q = new CONST(qf, in, s.extra(0),
				  new Integer(0), HClass.Int);
		    last = new OPER(qf, in, op, ns.extra(0),
				    swap
				    ?new Temp[] { s.extra(0), s.stack(0) }
				    :new Temp[] { s.stack(0), s.extra(0) });
		    Quad.addEdge(q, 0, last, 0);
		} else {
		    ns = s.pop(2);
		    q = new OPER(qf, in, op, ns.extra(0),
				 swap
				 ?new Temp[] { s.stack(0), s.stack(1) }
				 :new Temp[] { s.stack(1), s.stack(0) } );
		    last = q;
		}
		Quad Qc = new CJMP(qf, in, ns.extra(0), new Temp[0]);
		Quad.addEdge(last, 0, Qc, 0);
		r = new TransState[] {
		    new TransState(ns, in.next(0), Qc, invert?1:0),
		    new TransState(ns, in.next(1), Qc, invert?0:1)
		};
		last = Qc;
		break;
	    }
	case Op.JSR:
	case Op.JSR_W:
	    {
	    // futz with the state when we reach the JSR, and continue
	    // at the merge. The new call state will ensure that the 
	    // subroutine isn't merged with code outside the subroutine.
	    Instr target = in.next(1);
	    State ns = s.enterJSR(in, target);
	    // put a null in the stack location where the return address
	    // usually goes.
	    q = new CONST(qf, in, ns.stack(0), null, HClass.Void);
	    // translate starting at jsr target.
	    r = new TransState[] { new TransState(ns, target, q, 0) };
	    break;
	    }
	case Op.RET:
	    {
	    // go back to the caller, purging now-dead ret-addr.
	    OpLocalVariable opd = ((InRet)in).getOperand();
	    Instr target = s.getJSRtargetLV(opd.getIndex());
	    assert target!=null : "Unable to determine RET subroutine.";
	    Instr jsr = (Instr) s.calls().get(target);
	    Instr nxt = jsr.next(0);
	    State ns = s.purgeDead(nxt);
	    q = null; // no quad equivalent.
	    // translate starting from instruction after jsr.
	    r = new TransState[] { new TransState(ns, nxt,
						  ts.header, ts.which_succ) };
	    break;
	    }
	default:
	    throw new Error("Unknown InCti: "+in.toString());
	}
	if (q!=null) {
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	    s.recordHandler(in, q, last==null?q:last);
	}
	return r;
    }

    // Match array initializer
    private static final TransState processArrayInitializers(TransState ts,
							     HClass type)
    {
	Instr in = ts.in; // instruction after array creation.
	State s = ts.initialState;
	HandlerSet hs = s.handlers(in);
	List v = new ArrayList(); // array initializers.
	int offset = 0;
	boolean integerify = false;
	// BASTORE takes integer/byte args, stores in byte/boolean array.
	// SASTORE takes integer/byte/short args, stores in short array.
	// CASTORE takes integer/byte/short args, stores in character array.
	if (type==HClass.Boolean || type==HClass.Byte ||
	    type==HClass.Short   || type==HClass.Char)
	    integerify = true;

	do { // iterate over initialization instructions.
	    if (in.getOpcode() != Op.DUP) break; // in should be dup.
	    Instr in1 = in.next(0); // iconst, bipush, sipush, or ldc
	    if (extractConstType(in1) != HClass.Int) break;
	    Instr in2 = in1.next(0); // constant value of array init.
	    if ( integerify && extractConstType(in2) != HClass.Int) break;
	    if (!integerify && extractConstType(in2) != type)       break;
	    Instr in3 = in2.next(0); // ?astore
	    if (!isXASTORE(in3)) break;
	    // finally, check that all handlers are the same.
	    if (!HandlerSet.equals(hs, s.handlers(in ))) break;
	    if (!HandlerSet.equals(hs, s.handlers(in1))) break;
	    if (!HandlerSet.equals(hs, s.handlers(in2))) break;
	    if (!HandlerSet.equals(hs, s.handlers(in3))) break;
	    // allow only sequential initializers.
	    Number index = (Number) extractConst(in1).getValue();
	    if (v.size()==0) offset=index.intValue();
	    else if (index.intValue()!=offset+v.size()) break;
	    // okay, this is element N of array initializer.
	    Number value = (Number) extractConst(in2).getValue();
	    if (in2.getOpcode()==Op.BIPUSH || in2.getOpcode()==Op.SIPUSH)
		value = new Integer(value.intValue());
	    v.add(value);
	    in = in3.next(0);
	} while (true);
	if (v.size()==0) return ts;
	// else...
	Object[] oa = v.toArray();
	if (integerify) unintegerify(oa, type);
	Quad q=new ARRAYINIT(ts.initialState.qf(), ts.in,
			     ts.initialState.stack(0), offset, type, oa);
	Quad.addEdge(ts.header, ts.which_succ, q, 0);
	s.recordHandler(ts.in, q, q); // record the proper hanlder.
	return new TransState(ts.initialState, in, q, 0);
    }
    private static final void unintegerify(Object[] oa, HClass type) {
	for (int i=0; i<oa.length; i++) {
	    if (type==HClass.Boolean)
		oa[i] = new Boolean(0!=((Number)oa[i]).intValue());
	    if (type==HClass.Byte)
		oa[i] = new Byte(((Number)oa[i]).byteValue());
	    if (type==HClass.Short)
		oa[i] = new Short(((Number)oa[i]).shortValue());
	    if (type==HClass.Char)
		oa[i] = new Character((char)((Number)oa[i]).intValue());
	}
    }
    private static final boolean isXASTORE(Instr in) {
	byte opcode = in.getOpcode();
	switch(opcode) {
	case Op.BASTORE:
	case Op.CASTORE:
	case Op.DASTORE:
	case Op.FASTORE:
	case Op.IASTORE:
	case Op.LASTORE:
	case Op.SASTORE:
	    return true;
	default:
	    return false;
	}
    }
    private static final OpConstant extractConst(Instr in) {
	if (in instanceof InGen && in.getOpcode()!=Op.NEWARRAY) {
	    Operand[] op = ((InGen)in).getOperands();
	    if (op.length==1 && op[0] instanceof OpConstant)
		return (OpConstant) op[0];
	}
	return null;
    }
    // BIPUSH and SIPUSH return odd type info.
    private static final HClass extractConstType(Instr in) {
	OpConstant opc = extractConst(in);
	if (opc==null) return null;
	if (in.getOpcode()==Op.BIPUSH || in.getOpcode()==Op.SIPUSH)
	    return HClass.Int;
	return opc.getType();
    }

    // Miscellaneous utility functions. ///////////////////////////

    /** Determine if an HClass needs to be represented by one or two bytecode
     *  stack entries. */
    private static final boolean isLongDouble(HClass hc) {
	if (hc == HClass.Long || hc == HClass.Double)
	    return true;
	return false;
    }
}
