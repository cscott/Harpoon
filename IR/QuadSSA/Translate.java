// Translate.java, created Sat Aug  8 10:53:03 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
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
import harpoon.IR.Bytecode.InSwitch;
import harpoon.IR.Bytecode.Code.ExceptionEntry;
import harpoon.Util.Util;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;

/**
 * <code>Translate</code> is a utility class to implement the
 * actual Bytecode-to-QuadSSA translation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translate.java,v 1.85 1998-10-12 01:49:21 cananian Exp $
 */

class Translate  { // not public.
    /** Static State has all of the per-method (constant) information. */
    static class StaticState {
	/** All try blocks for this method. */
	ExceptionEntry allTries[];
	/** The footer for this block. */
	FOOTER footer;
	/** A Temp containing the constant zero. */
	Temp Tzero;
	/** A Temp containing the constant 'null'. */
	Temp Tnull;
	/** A Temp used for exception values. */
	Temp Tex;
	
	/** Make new StaticState */
	StaticState(ExceptionEntry allTries[], FOOTER footer,
		    Temp Tzero, Temp Tnull, Temp Tex) {
	    this.allTries = allTries;
	    this.footer   = footer;
	    this.Tzero    = Tzero;
	    this.Tnull    = Tnull;
	    this.Tex      = Tex;
	}
    }
    static class StackElement {
	Temp t;
	StackElement next; // singly-linked list.
	StackElement(Temp t, StackElement next) {
	    this.t = t; this.next = next;
	}
    }
    /** <code>State</code> represents the stack, local variable, and block
     *  context (for try and monitor quads) of a given bytecode Instr. */
    static class State { // inner class
	/** Current temps used for each position of stack.
	 *  <code>null</code>s are valid placeholders for empty spaces
	 *  in double-word representations. */
	StackElement stack;
	/** Current size of the stack. */
	int stackSize;
	/** Current temps used for local variables */
	Temp lv[];
	/** Stack of continuation TransStates at ret from jsr block. */
	Vector continuation[];

	/** special secret store of temps for the stack. */
	private Temp[] stackNames;

	/** Constructor. */
	private State(StackElement stack, int stackSize,
		      Temp lv[], Vector continuation[], Temp[] stackNames) {
	    this.stack = stack; 
	    this.stackSize = stackSize;
	    this.lv = lv;
	    this.continuation = continuation;
	    this.stackNames = stackNames;
	}
	/** Make new state by popping top of stack */
	State pop() { return pop(1); }
	/** Make new state by popping multiple entries off top of stack */
	State pop(int n) {
	    StackElement s = stack;
	    for (int i=0; i < n; i++)
		s = s.next;
	    return new State(s, stackSize-n, lv, continuation, stackNames);
	}
	/** Make new state by pushing temp onto top of stack. */
	State push(Temp t) {
	    StackElement s = new StackElement(t, stack);
	    return new State(s, stackSize+1, lv, continuation, stackNames);
	}
	/** Make new stack by pushing some temp onto top of stack. */
	State push() { 
	    return push(stackNames[stackSize]); 
	}
	/** Return the name of an 'extra' temp n above the stack top. */
	Temp extra(int n) {
	    return stackNames[stackSize+n];
	}
	/** Returns the 'n'th entry in the stack. */
	Temp stack(int n) {
	    StackElement s;
	    for (s = stack; n>0; n--)
		s = s.next;
	    return s.t;
	}

	/** Make new state by clearing all but the top entry of the stack. */
	State enterCatch() {
	    StackElement s = new StackElement(stack.t, null);
	    return new State(s, 1, lv, continuation, stackNames);
	}
	/** Make new state by entering a JSR/RET block. The "return address"
	 *  <code>t</code> gets pushed on top of the stack. */
	State enterJSR(Temp t) {
	    Vector c[] = (Vector[]) grow(this.continuation);
	    c[0] = new Vector();
	    return new State(stack, stackSize, lv, c, stackNames).push(t);
	}
	/** Make new state, as when exiting a JSR/RET block. */
	State exitJSR() {
	    Vector c[] = (Vector[]) shrink(this.continuation);
	    return new State(stack, stackSize, lv, c, stackNames).pop();
	}
	/** Scrub state prior to entering PHI (use canonical names). */
	State scrub() {
	    StackElement nstk = null;
	    for (int i=stackSize-1; i >= 0; i--)
		nstk = new StackElement(stackNames[i], nstk);
	    return new State(nstk, stackSize, lv, continuation, stackNames);
	}

	/** Initialize state with temps corresponding to parameters. */
	State(Temp[] locals, Temp[] stackNames) {
	    this(null, 0, locals, new Vector[0], stackNames);
	}
	/** Creates a new State object identical to this one. */
	public Object clone() {
	    return new State(stack, stackSize,
			     (Temp[]) lv.clone(), 
			     (Vector[]) continuation.clone(),
			     stackNames);
	}

	// Utility functions... ///////////////////////////////

	/** Makes a new array by popping first 'n' elements off. */
	private static final Object[] shrink(Object[] src, int n) {
	    Util.assert(src.length>=n);
	    Object[] dst = (Object[]) Array.newInstance(src.getClass()
							.getComponentType(),
							src.length-n);
	    System.arraycopy(src, n, dst, 0, dst.length);
	    return dst;
	}
	/** Makes a new array by popping the first element off. */
	private static final Object[] shrink(Object[] src) 
	{ return shrink(src,1); }

	/** Make a new array by pushing on 'n' elements to front. */
	private static final Object[] grow(Object[] src, int n) {
	    Object[] dst = (Object[]) Array.newInstance(src.getClass()
							.getComponentType(),
							src.length+n);
	    System.arraycopy(src, 0, dst, n, src.length);
	    return dst;
	}
	/** Make a new array by pushing a new element onto front. */
	private static final Object[] grow(Object[] src) 
	{ return grow(src,1); }
    }

    /** Extended state to keep track of translation process. */
    static class TransState {
	/** State to use when translating <code>Instr</code> <Code>in</code> */
	State initialState;
	/** Next <code>Instr</code> to translate. */
	Instr in;
	/** <code>Quad</code> to append translation of 
	    <code>in</code> to. */
	Quad  header; 
	/** Which exit edge of <code>header</code> to append the translation
	 * of <code>in</code> to. */
	int   which_succ;
	/** Constructor. */
	TransState(State initialState, Instr in, Quad header, int which_succ) {
	    this.initialState = initialState;
	    this.in = in;
	    this.header = header;
	    this.which_succ = which_succ;
	}
    }
    /** Keep track of MERGE instrs and the PHI quads that they correspond to.*/
    static class MergeMap {
	private Hashtable phimap, predmap, statemap;
	MergeMap() { 
	    phimap = new Hashtable(); 
	    predmap= new Hashtable();
	    statemap = new Hashtable();
	}
	void put(Instr in, PHI phi, int which_pred, State s) {
	    phimap.put(in, phi); 
	    predmap.put(in, new Integer(which_pred)); 
	    statemap.put(in, s);
	}
	PHI getPhi(Instr in) 
	{ return (PHI) phimap.get(in); }
	int getPred(Instr in) 
	{ return ((Integer) predmap.get(in)).intValue(); }
	State getState(Instr in)
	{ return (State) statemap.get(in); }
    }

    /** Return a <code>Quad</code> representation of the method code in
     *  <code>bytecode</code>. */
    static final Quad trans(harpoon.IR.Bytecode.Code bytecode) {
	boolean isStatic = bytecode.getMethod().isStatic();

	// set up initial state.
	HClass[] paramTypes = bytecode.getMethod().getParameterTypes();
	String[] paramNames = bytecode.getMethod().getParameterNames();
	Temp[] params = new Temp[paramNames.length+(isStatic?0:1)];
	int offset = 0;
	if (!isStatic)
	    params[offset++] = new Temp("this");
	for (int i=0; i<paramNames.length; i++)
	    params[offset+i] = new Temp((paramNames[i]==null)?"param"+i:
					paramNames[i]);

	Temp[] locals = new Temp[bytecode.getMaxLocals()];
	// we may use as many as 5 'extra' variables above stack top.
	Temp[] stack  = new Temp[bytecode.getMaxStack() + 5];

	// Initialize stack names
	for (int i=0; i < stack.length; i++)
	    stack[i] = new Temp("stk");

	// copy parameter Temps into locals.
	int j = 0;
	for (int i=0; i<params.length; i++) {
	    locals[i+j] = params[i];
	    if (i>=offset && isLongDouble(paramTypes[i-offset])) {
		locals[i+ ++j] = null;
	    }
	}
	// use generic names for the rest of the locals.
	for (int i=params.length+j; i<locals.length; i++)
	    locals[i] = new Temp("lv$"+i);
	
	// deterimine if this is a synchronized method.
	boolean isSynchronized = Modifier.isSynchronized(bytecode.getMethod()
							 .getModifiers());

	Instr firstInstr = (Instr) bytecode.getRootElement();

	HEADER quads = new METHODHEADER(firstInstr, null, params);
	FOOTER footer= new FOOTER(firstInstr);
	quads.footer = footer;

	StaticState SS = new StaticState(bytecode.getTryBlocks(), footer,
					 new Temp("$zero"), new Temp("$null"),
					 new Temp("$ex"));
	State s = new State(locals, stack);

	Quad q1 = new CONST(quads, SS.Tnull, null, HClass.Void);
	Quad q2 = new CONST(quads, SS.Tzero, new Integer(0), HClass.Int);
	Quad.addEdge(quads, 0, q1, 0);
	Quad.addEdge(q1, 0, q2, 0);

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
	Quad q = q2;
	if (isSynchronized) {
	    if (!isStatic) { // virtual synchronized is easy.
		lock = s.lv[0]; // 'this'
		q = new MONITORENTER(firstInstr, lock);
		Quad.addEdge(q2, 0, q, 0);
	    } else { // static synchronized, what a kludge.
		lock = new Temp(); // lock is Class.forName(this.class)
		HClass strC = HClass.forClass(String.class);
		HClass exC = HClass.forClass(NoClassDefFoundError.class);
		Quad qq0 = new CONST(quads, s.extra(0), 
				     bytecode.getMethod().getDeclaringClass()
				     .getName(), strC);
		Quad qq1 = new CALL(quads, 
				    HClass.forClass(Class.class)
				    .getMethod("forName", 
					       new HClass[] { strC } ),
				    null, qq0.def(), lock, SS.Tex, false);
		Quad qq2 = new OPER(quads, "acmpeq", s.extra(0),
				    new Temp[] { SS.Tex, SS.Tnull });
		Quad qq3 = new CJMP(quads, qq2.def()[0], new Temp[0]);
		Quad qq4 = new OPER(quads, "acmpeq", s.extra(0),
				    new Temp[] { lock, SS.Tnull });
		Quad qq5 = new CJMP(quads, qq4.def()[0], new Temp[0]);
		Quad qq6 = new MONITORENTER(quads, lock);
		// handle exceptions of various kinds.
		Quad qq7 = new PHI(quads, new Temp[0], 2);
		Quad qq8 = transNewException(SS, exC, SS.Tex,
					     new TransState(s, firstInstr,
							    qq7, 0));
		Quad qq9 = new THROW(quads, SS.Tex);
		// okay, link 'em up.
		Quad.addEdges(new Quad[] {  q2, qq0, qq1, qq2, qq3 } );
		Quad.addEdge(qq3, 0, qq7, 0);
		Quad.addEdge(qq3, 1, qq4, 0);
		Quad.addEdges(new Quad[] { qq4, qq5, qq6} );
		Quad.addEdge(qq5, 1, qq7, 1);
		Quad.addEdge(qq8, 0, qq9, 0);
		SS.footer.attach(qq9, 0);
		q = qq6;
	    }
	}

	// translate using state.
	trans(SS, new TransState(s, firstInstr, q, 0));

	// if method is synchronized, place MONITOREXIT at bottom(s).
	if (isSynchronized) {
	    Util.assert(lock!=null);
	    // for all predecessors of FOOTER
	    for (int i=0; i < footer.prev.length; i++) {
		// static synchronized methods have a single exception
		// exit *before* the monitor is entered.
		if (isStatic&&i==0) continue;
		// put a MONITOREXIT before the return/throw/whatever.
		Quad Qexit = footer.prev(i);
		Util.assert(Qexit.prev.length==1); // only one predecessor.
		Quad Qm = new MONITOREXIT(Qexit.source, lock);
		Edge e = Qexit.prevEdge(0);
		Quad.addEdge((Quad)e.from(), e.which_succ(), Qm, 0);
		Quad.addEdge(Qm, 0, (Quad)e.to(), e.which_pred());
	    }
	}

	// return result.
	return quads;
    }

    /** Translate a block starting with a given <code>TransState</code>.<p> 
     *  Start at <code>ts.in</code> using <code>ts.initialState</code>. */
    static final void trans(StaticState SS, TransState ts0) {
	Stack todo = new Stack(); todo.push(ts0);
	MergeMap mm = new MergeMap();
	MergeMap handlers = new MergeMap();

	while (!todo.empty()) {
	    TransState ts = (TransState) todo.pop();
	    // convenient abbreviations of TransState fields.
	    State s = ts.initialState;

	    // Are we entering a JSR/RET block?
	    if ((ts.in.getOpcode() == Op.JSR) ||
		(ts.in.getOpcode() == Op.JSR_W)) {

		State ns = s.enterJSR(SS.Tnull);
		Instr in = ts.in.next()[1];
		if (in instanceof InMerge) in = in.next()[0];
		trans(SS, new TransState(ns, in, ts.header, ts.which_succ));

		// make PHI after RET
		TransState tsi = 
		    (TransState) ns.continuation[0].elementAt(0);
		tsi = new TransState(tsi.initialState.exitJSR(), 
				     ts.in.next()[0] /* after JSR */,
				     tsi.header, tsi.which_succ);
		PHI phi = null;
		State phiState = tsi.initialState;
		for (int i=1; i < ns.continuation[0].size(); i++) {
		    if (i==1) { // make
			phi = new PHI(tsi.in, new Temp[0],
				      ns.continuation[0].size());
			Quad.addEdge(tsi.header, tsi.which_succ, phi, 0);
			tsi = new TransState(phiState, tsi.in, phi, 0);
		    }
		    TransState c = 
			(TransState) ns.continuation[0].elementAt(i); 
		    for (int j=0; j<phiState.stackSize; j++) {
			if (phiState.stack(j)==null) continue;
			Quad q2 = new MOVE(c.in, 
				      phiState.stack(j),
				      c.initialState.stack(j));
			Quad.addEdge(c.header, c.which_succ, q2, 0);
			c = new TransState(c.initialState, c.in, q2, 0);
		    }
		    Quad.addEdge(c.header, c.which_succ, phi, i);
		}

		ns = tsi.initialState.exitJSR();
		todo.push(tsi);
		continue;
	    }
	    // Are we exiting a JSR block?
	    else if (s.continuation.length>0 && ts.in.getOpcode() == Op.RET) {
		s.continuation[0].addElement(ts);
		// we'll fix up the dangling end later.
		continue;
	    }
	    // None of the above.
	    else {
		TransState nts[] = transInstr(SS, ts, mm, handlers);
		for (int i=nts.length-1; i>=0; i--)
		    todo.push(nts[i]);
		continue;
	    }
	}
	// done.
	return;
    }

    /** Translate a single instruction, using a <code>MergeMap</code>.
     * @return the <code>TransState</code>s of the following instructions.
     */
    static final TransState[] transInstr(StaticState SS, TransState ts, 
					 MergeMap mm, MergeMap handlers) {
	// Dispatch to correct specific function.
	//System.out.println("Translating "+Op.toString(ts.in.getOpcode()));
	if (ts.in instanceof InGen)    return transInGen(SS, ts, handlers);
	if (ts.in instanceof InSwitch) return transInSwitch(SS, ts);
	if (ts.in instanceof InCti)    return transInCti(SS, ts, handlers);
	if (ts.in instanceof InMerge)  return transInMerge(SS, ts, mm);
	throw new Error("Unknown Instr type.");
    }

    /** Translate an <code>InGen</code>. 
     *  @return a <Code>TransState[]</code> of length zero or one. */
    static final TransState[] transInGen(StaticState SS, TransState ts, 
					 MergeMap handlers) {
	InGen in = (InGen) ts.in;
	State s = ts.initialState;
	State ns;
	Quad q;
	Quad last = null; int which_succ = 0;
	TransState[] r = new TransState[0]; // only CHECKCAST/INVOKE* use this.

	switch(in.getOpcode()) {
	case Op.AALOAD:
	case Op.BALOAD:
	case Op.CALOAD:
	case Op.DALOAD:
	case Op.FALOAD:
	case Op.IALOAD:
	case Op.LALOAD:
	case Op.SALOAD:
	    {
	    if (in.getOpcode()==Op.DALOAD ||
		in.getOpcode()==Op.LALOAD)
		ns = s.pop(2).push(null).push(); // 64-bit val.
	    else
		ns = s.pop(2).push(); // 32-bit val

	    Temp Tobj  = s.stack(1);
	    Temp Tindex= s.stack(0);
	    // the actual operation.
	    Quad q0= new AGET(in, ns.stack(0), Tobj, Tindex);
	    // bounds check
	    r = transBoundsCheck(SS, Tobj, Tindex, q0, handlers, ts);
	    q = ts.header.next()[ts.which_succ];
	    last = q0;
	    // done.
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
	    Temp Tobj, Tindex, Tsrc;
	    if (in.getOpcode()==Op.DASTORE ||
		in.getOpcode()==Op.LASTORE) { // 64-bit val.
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
	    Quad q0= new ASET(in, Tobj, Tindex, Tsrc);
	    last = q0;

	    // funky additional check for AASTORE
	    if (in.getOpcode() == Op.AASTORE) {
		// AASTORE also throws ArrayStoreException.
		HClass HCase = HClass.forClass(ArrayStoreException.class);
		Quad qq0 = new COMPONENTOF(in, s.extra(0), Tobj, Tsrc);
		Quad qq1 = new CJMP(in, qq0.def()[0], new Temp[0]);
		Quad qq2 = transNewException(SS, HCase, SS.Tex, 
					     new TransState(ts.initialState, 
							    in, qq1, 0));
		r = transThrow(SS, new TransState(ts.initialState.push(SS.Tex),
						  ts.in, qq2, 0),
			       handlers, false);
		// link
		Quad.addEdge(qq0, 0, qq1, 0);
		Quad.addEdge(qq1, 1, q0,  0);
		q0 = qq0;
	    }
	    // bounds check
	    TransState r2[] = transBoundsCheck(SS, Tobj, Tindex, 
					       q0, handlers, ts);
	    // merge TransState arrays.
	    r = mergeTS(r, r2);
	    // set up next state.
	    q = ts.header.next()[ts.which_succ];
	    // done.
	    break;
	    }
	case Op.ACONST_NULL:
	    ns = s.push(SS.Tnull);
	    q = null;
	    break;
	case Op.ALOAD:
	case Op.ALOAD_0:
	case Op.ALOAD_1:
	case Op.ALOAD_2:
	case Op.ALOAD_3:
	    {
		OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
		ns = s.push(s.lv[opd.getIndex()]);
		q = null;
		break;
	    }
	case Op.ANEWARRAY:
	    {
		// if (count<0) throw new NegativeArraySizeException();
		OpClass opd = (OpClass) in.getOperand(0);
		HClass hc = HClass.forDescriptor("[" + 
						 opd.value().getDescriptor());
		ns = s.pop().push();

		HClass HCex=HClass.forClass(NegativeArraySizeException.class);

		// check whether count>=0.
		Quad q2 = new OPER(in, "icmpge", s.extra(0),
				   new Temp[] { s.stack(0), SS.Tzero });
		Quad q3 = new CJMP(in, q2.def()[0], new Temp[0]);
		Quad q4 = transNewException(SS, HCex, SS.Tex, 
					    new TransState(s, in, q3, 0));
		r = transThrow(SS, new TransState(s.push(SS.Tex), in, q4, 0),
			       handlers, false);
		Quad q5 = new ANEW(in, ns.stack(0), hc, 
				   new Temp[] { s.stack(0) });
		// link
		Quad.addEdge(q2, 0, q3, 0);
		Quad.addEdge(q3, 1, q5, 0);

		q = q2;
		last = q5;
		break;
	    }
	case Op.ARRAYLENGTH:
	    {
	    // if (objref==null) throw new NullPointerException()
	    ns = s.pop().push();

	    Temp Tobj  = s.stack(0);

	    // actual operation:
	    Quad q0 = new ALENGTH(in, ns.stack(0), Tobj);
	    // null check.
	    r = transNullCheck(SS, Tobj, q0, handlers, ts);
	    // setup next state
	    q = ts.header.next()[ts.which_succ];
	    last = q0;
	    break;
	    }
	case Op.ASTORE:
	case Op.ASTORE_0:
	case Op.ASTORE_1:
	case Op.ASTORE_2:
	case Op.ASTORE_3:
	    {
	    OpLocalVariable opd = (OpLocalVariable) in.getOperand(0);
	    ns = s.pop();
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack(0));
	    break;
	    }
	case Op.BIPUSH:
	case Op.SIPUSH:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		int val = ((Byte)opd.getValue()).intValue();
		ns = s.push();
		q = new CONST(in, ns.stack(0), new Integer(val), HClass.Int);
		break;
	    }
	case Op.CHECKCAST:
	    // translate as:
	    //  if (obj!=null && !(obj instanceof class))
	    //     throw new ClassCastException();
	    {
		OpClass opd = (OpClass) in.getOperand(0);
		ns = s;
		Temp Tobj = s.stack(0);

		HClass HCex = HClass.forClass(ClassCastException.class);

		// make quads
		Quad q1 = new OPER(in, "acmpeq", s.extra(0), // equal is true
				   new Temp[] { Tobj, SS.Tnull } ); 
		Quad q2 = new CJMP(in, q1.def()[0], new Temp[0]);
		Quad q3 = new INSTANCEOF(in, s.extra(0), Tobj, opd.value());
		Quad q4 = new CJMP(in, q3.def()[0], new Temp[0]);
		Quad q5 = transNewException(SS, HCex, SS.Tex, 
					    new TransState(s, in, q4, 0));
		r = transThrow(SS, new TransState(s.push(SS.Tex), in, q5, 0),
			       handlers, false);
		Quad q6 = new PHI(in, new Temp[0], 2);
		// link quads.
		Quad.addEdges(new Quad[] { q1, q2, q3, q4 });
		Quad.addEdge(q2, 1, q6, 0);
		Quad.addEdge(q4, 1, q6, 1);
		// and setup the next state.
		ns = s;
		q = q1;
		last = q6;
		break;
	    }
	case Op.D2F:
	case Op.D2I:
	case Op.L2F:
	case Op.L2I:
	    ns = s.pop(2).push();
	    q = new OPER(in, Op.toString(in.getOpcode()) /* "d2f" or "d2i" */,
			 ns.stack(0), new Temp[] { s.stack(0) });
	    break;
	case Op.D2L:
	case Op.L2D:
	    ns = s.pop(2).push(null).push();
	    q = new OPER(in, Op.toString(in.getOpcode()) /* "d2l" or "l2d" */,
			 ns.stack(0), new Temp[] { s.stack(0) });
	    break;
	case Op.DADD:
	case Op.DDIV:
	case Op.DMUL:
	case Op.DREM:
	case Op.DSUB:
	case Op.LADD:
	case Op.LAND:
	case Op.LMUL:
	case Op.LOR:
	case Op.LSUB:
	case Op.LXOR:
	    ns = s.pop(4).push(null).push();
	    q = new OPER(in, Op.toString(in.getOpcode()), // dadd, ddiv or dmul
			 ns.stack(0), new Temp[] { s.stack(2), s.stack(0) });
	    break;
	case Op.DCMPG:
	case Op.DCMPL:
	    {
	    boolean isDCMPG = (in.getOpcode()==Op.DCMPG);
	    ns = s.pop(4).push();
	    Quad q0 = new OPER(in, "dcmpgt", s.extra(0),
			       isDCMPG ?
			       new Temp[] { s.stack(0), s.stack(2) } :
			       new Temp[] { s.stack(2), s.stack(0) } );
	    Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(in, "dcmpeq", s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q3 = new CJMP(in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(in, ns.stack(0), new Integer( 0), HClass.Int);
	    Quad q6 = new CONST(in, ns.stack(0), new Integer( 1), HClass.Int);
	    Quad q7 = new PHI(in, new Temp[0], 3);
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
	case Op.DCONST_0:
	case Op.DCONST_1:
	case Op.LCONST_0:
	case Op.LCONST_1:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		ns = s.push(null).push();
		q = new CONST(in, ns.stack(0), opd.getValue(), opd.getType());
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
		ns = s.push(null).push(s.lv[opd.getIndex()]);
		q = null;
		break;
	    }
	case Op.DNEG:
	case Op.LNEG:
	    ns = s.pop(2).push(null).push();
	    q = new OPER(in, Op.toString(in.getOpcode()) /*"dneg" or "lneg"*/, 
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
	    ns = s.pop(2);
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack(0));
	    break;
	    }
	case Op.DUP:
	    ns = s.push(s.stack(0));
	    q = null;
	    break;
	case Op.DUP_X1:
	    ns = s.pop(2).push(s.stack(0)).push(s.stack(1)).push(s.stack(0));
	    q = null;
	    break;
	case Op.DUP_X2:
	    ns = s.pop(3).push(s.stack(0)).push(s.stack(2)).push(s.stack(1))
		.push(s.stack(0));
	    q = null;
	    break;
	case Op.DUP2:
	    ns = s.push(s.stack(1)).push(s.stack(0));
	    q = null;
	    break;
	case Op.DUP2_X1:
	    ns = s.pop(3).push(s.stack(1)).push(s.stack(0))
		.push(s.stack(2)).push(s.stack(1)).push(s.stack(0));
	    q = null;
	    break;
	case Op.DUP2_X2:
	    ns = s.pop(4).push(s.stack(1)).push(s.stack(0))
		.push(s.stack(3)).push(s.stack(2))
		.push(s.stack(1)).push(s.stack(0));
	    q = null;
	    break;
	case Op.F2D:
	case Op.F2L:
	case Op.I2D:
	case Op.I2L:
	    ns = s.pop().push(null).push();
	    q = new OPER(in, Op.toString(in.getOpcode()), // "f2d" or "f2l"
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.F2I:
	case Op.I2B:
	case Op.I2C:
	case Op.I2F:
	case Op.I2S:
	    ns = s.pop().push();
	    q = new OPER(in, Op.toString(in.getOpcode()),
			 ns.stack(0), new Temp[] {s.stack(0)});
	    break;
	case Op.FADD:
	case Op.FDIV:
	case Op.FMUL:
	case Op.FREM:
	case Op.FSUB:
	case Op.IADD:
	case Op.IAND:
	case Op.IMUL:
	case Op.IOR:
	case Op.ISHL:
	case Op.ISHR:
	case Op.ISUB:
	case Op.IUSHR:
	case Op.IXOR:
	    ns = s.pop(2).push();
	    q = new OPER(in, Op.toString(in.getOpcode()), // fadd, fdiv, ...
			 ns.stack(0), new Temp[] {s.stack(1), s.stack(0)});
	    break;
	case Op.IDIV:
	case Op.IREM:
	    {
	    ns = s.pop(2).push();

	    // if (divisor==0) throw new ArithmeticException();
	    HClass HCex = HClass.forClass(ArithmeticException.class);

	    Quad q0 = new OPER(in, "icmpeq", s.extra(0),
			       new Temp[] { s.stack(0), SS.Tzero } );
	    Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
	    Quad q2 = transNewException(SS, HCex, SS.Tex, 
					new TransState(s, in, q1, 1));
	    r = transThrow(SS, new TransState(s.push(SS.Tex), in, q2, 0),
			   handlers, false);
	    // actual division operation:
	    Quad q3 = new OPER(in, Op.toString(in.getOpcode()), // idiv/ldiv
			       ns.stack(0),
			       new Temp[] {s.stack(1), s.stack(0)});
	    // link quads.
	    Quad.addEdges(new Quad[] { q0, q1, q3 } );
	    // setup next state.
	    q = q0; last = q3;
	    break;
	    }
	case Op.LDIV:
	case Op.LREM:
	    {
	    ns = s.pop(4).push(null).push();

	    // if (divisor==0) throw new ArithmeticException();
	    HClass HCex = HClass.forClass(ArithmeticException.class);

	    Quad q0 = new CONST(in, s.extra(0),
				new Long(0), HClass.Long);
	    Quad q1 = new OPER(in, "lcmpeq", s.extra(0),
			       new Temp[] { s.stack(0), q0.def()[0] } );
	    Quad q2 = new CJMP(in, q1.def()[0], new Temp[0]);
	    Quad q3 = transNewException(SS, HCex, SS.Tex, 
					new TransState(s, in, q2, 1));
	    r = transThrow(SS, new TransState(s.push(SS.Tex), in, q3, 0),
			   handlers, false);
	    // actual division operation:
	    Quad q4 = new OPER(in, Op.toString(in.getOpcode()), // idiv/ldiv
			       ns.stack(0), 
			       new Temp[] {s.stack(2), s.stack(0)});
	    // link quads.
	    Quad.addEdges(new Quad[] { q0, q1, q2, q4 } );
	    // setup next state.
	    q = q0; last = q4;
	    break;
	    }
	case Op.FCMPG:
	case Op.FCMPL:
	    {
	    boolean isFCMPG = (in.getOpcode()==Op.FCMPG);
	    ns = s.pop(2).push();
	    Quad q0 = new OPER(in, "fcmpgt", s.extra(0),
			       isFCMPG ?
			       new Temp[] { s.stack(0), s.stack(1) } :
			       new Temp[] { s.stack(1), s.stack(0) } );
	    Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(in, "fcmpeq", s.extra(0),
			       new Temp[] { s.stack(1), s.stack(0) });
	    Quad q3 = new CJMP(in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(in, ns.stack(0), new Integer( 0), HClass.Int);
	    Quad q6 = new CONST(in, ns.stack(0), new Integer( 1), HClass.Int);
	    Quad q7 = new PHI(in, new Temp[0], 3);
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
		q = new CONST(in, ns.stack(0), opd.getValue(), opd.getType());
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
		ns = s.push(s.lv[opd.getIndex()]);
		q = null;
		break;
	    }
	case Op.FNEG:
	case Op.INEG:
	    ns = s.pop().push();
	    q = new OPER(in, Op.toString(in.getOpcode()), 
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
	    ns = s.pop();
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack(0));
	    break;
	    }
	case Op.GETFIELD:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.pop().push(null).push();
	    else // 32-bit value.
		ns = s.pop().push();

	    // actual operation:
	    Quad q0 = new GET(in, ns.stack(0), opd.value(), s.stack(0));
	    // null check.
	    r = transNullCheck(SS, s.stack(0), q0, handlers, ts);
	    // setup next state.
	    q = ts.header.next()[ts.which_succ]; 
	    last = q0;
	    break;
	    }
	case Op.GETSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.push(null).push();
	    else // 32-bit value.
		ns = s.push();
	    q = new GET(in, ns.stack(0), opd.value());
	    break;
	    }
	case Op.IINC:
	    {
		OpLocalVariable opd0 = (OpLocalVariable) in.getOperand(0);
		OpConstant opd1 = (OpConstant) in.getOperand(1);
		Temp constant = s.extra(0);
		ns = s;
		q = new CONST(in, constant, opd1.getValue(), opd1.getType());
		Quad.addEdge(q, 0,
			     new OPER(in, "iadd", ns.lv[opd0.getIndex()],
				      new Temp[] { s.lv[opd0.getIndex()], 
						       constant}), 0);
		last = q.next(0);
		break;
	    }
	case Op.INSTANCEOF:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.pop().push();
	    q = new INSTANCEOF(in, ns.stack(0), s.stack(0), opd.value());
	    break;
	    }
	case Op.INVOKEINTERFACE:
	case Op.INVOKESPECIAL:
	case Op.INVOKESTATIC:
	case Op.INVOKEVIRTUAL:
	    {
	    boolean isSpecial = (in.getOpcode()==Op.INVOKESPECIAL);
	    boolean isStatic = (in.getOpcode()==Op.INVOKESTATIC);
	    OpMethod opd = (OpMethod) in.getOperand(0);
	    HClass paramtypes[] = opd.value().getParameterTypes();
	    Temp param[] = new Temp[paramtypes.length];
	    int i,j;
	    for (i=param.length-1, j=0; i>=0; i--, j++) {
		param[i] = s.stack(j);
		if (isLongDouble(paramtypes[i])) j++;
	    }
	    Temp objectref = isStatic?null:s.stack(j);
	    Temp Tex = SS.Tex;
	    if (opd.value().getReturnType()==HClass.Void) { // no return value.
		ns = s.pop(j+(isStatic?0:1));
		q = new CALL(in, opd.value(), objectref, param, 
			     null, Tex, isSpecial);
	    } else if (!isLongDouble(opd.value().getReturnType())) {
		// 32-bit return value.
		ns = s.pop(j+(isStatic?0:1)).push();
		q = new CALL(in, opd.value(), objectref, param, 
			     ns.stack(0), Tex, isSpecial);
	    } else { // 64-bit return value.
		ns = s.pop(j+(isStatic?0:1)).push(null).push();
		q = new CALL(in, opd.value(), objectref, param, 
			     ns.stack(0), Tex, isSpecial);
	    }
	    // check for thrown exception.
	    Quad q1 = new OPER(in, "acmpeq", ns.extra(0),
			       new Temp[] { Tex, SS.Tnull });
	    Quad q2 = new CJMP(in, q1.def()[0], new Temp[0]);
	    r = transThrow(SS, new TransState(s.push(Tex), in, q2, 0),
			   handlers, false);
	    Quad.addEdges(new Quad[] { q, q1, q2 });
	    last = q2; which_succ = 1;
	    // null dereference check (JVM disallows on uninit objects)
	    if (!isStatic && !(opd.value() instanceof HConstructor)) {
		HClass HCex = HClass.forClass(NullPointerException.class);

		// test objectref against null.
		Quad q3 = new OPER(in, "acmpeq", s.extra(0),
				   new Temp[] { objectref, SS.Tnull } );
		Quad q4 = new CJMP(in, q3.def()[0], new Temp[0]);
		Quad q5 = transNewException(SS, HCex, Tex, 
					    new TransState(s, in, q4, 1));
		Quad q6 = new PHI(in, new Temp[0], 2);
		// rewrite links.
		Quad.addEdge(q3, 0, q4, 0);
		Quad.addEdge(q4, 0, q,  0);
		Quad.addEdge(q5, 0, q6, 0);
		Quad.addEdge(q6, 0, q2.next(0), 0);
		Quad.addEdge(q2, 0, q6, 1);
		q = q3;
	    }
	    }
	    break;
	case Op.LCMP: // break this up into lcmpeq, lcmpgt, etc.
	    { // optimization doesn't work well on this, unfortunately.
	    ns = s.pop(4).push();
	    Quad q0 = new OPER(in, "lcmpeq", s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
	    Quad q2 = new OPER(in, "lcmpgt", s.extra(0),
			       new Temp[] { s.stack(2), s.stack(0) });
	    Quad q3 = new CJMP(in, q2.def()[0], new Temp[0]);
	    Quad q4 = new CONST(in, ns.stack(0), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(in, ns.stack(0), new Integer(0), HClass.Int);
	    Quad q6 = new CONST(in, ns.stack(0), new Integer(1), HClass.Int);
	    Quad q7 = new PHI(in, new Temp[0], 3);
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
	case Op.LDC:
	case Op.LDC_W:
	case Op.LDC2_W:
	    {
	    OpConstant opd = (OpConstant) in.getOperand(0);
	    if (isLongDouble(opd.getType()))
		ns = s.push(null).push();
	    else
		ns = s.push();
	    q = new CONST(in, ns.stack(0), opd.getValue(), opd.getType());
	    break;
	    }
	case Op.LSHL:
	case Op.LSHR:
	case Op.LUSHR:
	    ns = s.pop(3).push(null).push();
	    q = new OPER(in, Op.toString(in.getOpcode()), // lshl
			 ns.stack(0), new Temp[] { s.stack(1), s.stack(0) });
	    break;
	case Op.MONITORENTER:
	    ns = s.pop();
	    last = new MONITORENTER(in, s.stack(0));
	    // null dereference check.
	    r = transNullCheck(SS, s.stack(0), last, handlers, ts);
	    q = ts.header.next()[ts.which_succ];
	    break;
	case Op.MONITOREXIT:
	    ns = s.pop();
	    last = new MONITOREXIT(in, s.stack(0));
	    // null dereference check.
	    r = transNullCheck(SS, s.stack(0), last, handlers, ts);
	    q = ts.header.next()[ts.which_succ];
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
		// check dimensions.
		HClass HCex=HClass.forClass(NegativeArraySizeException.class);
		Quad Qp = new PHI(in, new Temp[0], dims);
		Quad Qn = transNewException(SS, HCex, SS.Tex, 
					    new TransState(s, in, Qp, 0));
		r = transThrow(SS, new TransState(s.push(SS.Tex), in, Qn, 0),
			       handlers, false);

		last = ts.header; which_succ = ts.which_succ;
		for (int i=0; i<dims; i++) {
		    Quad q0 = new OPER(in, "icmpgt", s.extra(0),
				       new Temp[] { SS.Tzero, Tdims[i] });
		    Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
		    Quad.addEdge(last, which_succ, q0, 0);
		    Quad.addEdge(q0, 0, q1, 0);
		    Quad.addEdge(q1, 1, Qp, i);
		    last=q1; which_succ=0;
		}
		// the actual operation:
		Quad Qa = new ANEW(in, ns.stack(0), opd0.value(), Tdims);
		Quad.addEdge(last, which_succ, Qa, 0);
		// make next state
		q = ts.header.next()[ts.which_succ];
		last = Qa; which_succ=0;
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
		    arraytype = HClass.forDescriptor("[Z"); break;
		case T_CHAR:
		    arraytype = HClass.forDescriptor("[C"); break;
		case T_FLOAT:
		    arraytype = HClass.forDescriptor("[F"); break;
		case T_DOUBLE:
		    arraytype = HClass.forDescriptor("[D"); break;
		case T_BYTE:
		    arraytype = HClass.forDescriptor("[B"); break;
		case T_SHORT:
		    arraytype = HClass.forDescriptor("[S"); break;
		case T_INT:
		    arraytype = HClass.forDescriptor("[I"); break;
		case T_LONG:
		    arraytype = HClass.forDescriptor("[J"); break;
		default:
		    throw new Error("Illegal NEWARRAY component type: "+type);
		}

		ns = s.pop().push();
		
		HClass HCex=HClass.forClass(NegativeArraySizeException.class);

		// ensure that size>=0
		Quad q0 = new OPER(in, "icmpge", s.extra(0),
				   new Temp[] { s.stack(0), SS.Tzero });
		Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
		Quad q2 = transNewException(SS, HCex, SS.Tex, 
					    new TransState(s, in, q1, 0));
		r = transThrow(SS, new TransState(s.push(SS.Tex), in, q2, 0),
			       handlers, false);
		// actual operation:
		Quad q3 = new ANEW(in, ns.stack(0), arraytype, 
				   new Temp[] { s.stack(0) });
		// link
		Quad.addEdge(q0, 0, q1, 0);
		Quad.addEdge(q1, 1, q3, 0);
		// make next state.
		q = q0; last=q3;
		break;
	    }
	case Op.NEW:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.push();
	    q = new NEW(in, ns.stack(0), opd.value());
	    break;
	    }
	case Op.NOP:
	    ns = s; q = null;
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
	    if (isLongDouble(opd.value().getType())) { // 64-bit value.
		ns = s.pop(3);
		last = new SET(in, opd.value(), s.stack(2), s.stack(0));
	    }
	    else {
		ns = s.pop(2);
		last = new SET(in, opd.value(), s.stack(1), s.stack(0));
	    }
	    // null check.
	    r = transNullCheck(SS, ((SET)last).objectref, last, 
			       handlers, ts);
	    // setup next state.
	    q = ts.header.next()[ts.which_succ];
	    break;
	    }
	case Op.PUTSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType())) // 64-bit value.
		ns = s.pop(2);
	    else
		ns = s.pop(1);
	    q = new SET(in, opd.value(), s.stack(0));
	    break;
	    }
	case Op.SWAP:
	    ns = s.pop(2).push(s.stack(0)).push(s.stack(1));
	    q = null;
	    break;

	default:
	    throw new Error("Unknown InGen opcode.");
	}
	if (last == null) last = q;
	// make & return next translation states to hit.
	TransState result[] = new TransState[r.length+1];
	System.arraycopy(r, 0, result, 1, r.length);
	if (q!=null) {
	    // Link new quad if necessary.
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	    result[0] = new TransState(ns, in.next()[0], last, which_succ);
	} else {
	    result[0] = new TransState(ns, in.next()[0], 
				       ts.header, ts.which_succ);
	}
	return result;
    }

    /** 
     * Translate a single <Code>InMerge</code> using a <code>MergeMap</code>.
     */
    static final TransState[] transInMerge(StaticState SS, TransState ts, 
					   MergeMap mm) {
	InMerge in = (InMerge) ts.in;
	State s = ts.initialState; // abbreviation.
	TransState[] result = new TransState[0]; // eventual result.

	// First, look up the InMerge in the MergeMap.
	PHI phi = mm.getPhi(in);
	if (phi==null) {
	    // create new phi
	    phi = new PHI(in, new Temp[0], in.arity());
	    State ns = s.scrub(); // canonicalize names.
	    mm.put(in, phi, 0, ns);
	    // Create new state & keep it around.
	    result = new TransState[] { new TransState(ns, in.next()[0],
						       phi, 0) };
	}
	// Look up the edge to use.
	int which_pred = mm.getPred(in);
	// look up the names to use for stack values.
	State phiState = mm.getState(in);
	Quad q = ts.header; int which_succ = ts.which_succ;
	// move stack temps around.
	for (int i=0; i<s.stackSize; i++) {
	    if (phiState.stack(i) == null) continue;
	    if (phiState.stack(i) == s.stack(i)) continue;
	    Quad q2 = new MOVE(in, phiState.stack(i), s.stack(i));
	    Quad.addEdge(q, which_succ, q2, 0);
	    q = q2; which_succ = 0;
	}
	// link
	Quad.addEdge(q, which_succ, phi, which_pred);
	// increment which_pred
	mm.put(in, phi, which_pred+1, phiState);
	// done
	return result;
    }
    /** Translate a single <code>InSwitch</code>. */
    static final TransState[] transInSwitch(StaticState SS, TransState ts) {
	InSwitch in = (InSwitch) ts.in;
	State s = ts.initialState;
	State ns = s.pop();
	Instr nxt[] = in.next();
	// make keys array.
	int keys[] = new int[nxt.length-1];
	for (int i=0; i<keys.length; i++)
	    keys[i] = in.key(i+1);
	// make & link SWITCH quad.
	Quad q = new SWITCH(in, s.stack(0), keys, new Temp[0]);
	Quad.addEdge(ts.header, ts.which_succ, q, 0);
	// Make next states.
	TransState[] r = new TransState[nxt.length];
	for (int i=0; i<nxt.length-1; i++)
	    r[i] = new TransState(ns, nxt[i+1], q, i);
	Util.assert(keys.length == nxt.length-1);
	r[keys.length] = new TransState(ns, nxt[0], q, keys.length);
	return r;
    }
    /** Translate a single <code>InCti</code>. */
    static final TransState[] transInCti(StaticState SS, 
					 TransState ts, MergeMap handlers) {
	InCti in = (InCti) ts.in;
	State s = ts.initialState;
	Quad q;
	TransState[] r;
	switch(in.getOpcode()) {
	case Op.ARETURN:
	case Op.DRETURN:
	case Op.FRETURN:
	case Op.IRETURN:
	case Op.LRETURN:
	    q = new RETURN(in, s.stack(0));
	    r = new TransState[0];
	    SS.footer.attach(q, 0);
	    break;
	case Op.RETURN:
	    q = new RETURN(in);
	    r = new TransState[0];
	    SS.footer.attach(q, 0);
	    break;
	case Op.ATHROW:
	    r = transThrow(SS, ts, handlers, true);
	    q = null;
	    break;
	case Op.GOTO:
	case Op.GOTO_W:
	    q = null;
	    r = new TransState[] { new TransState(s, in.next()[0], 
						  ts.header, ts.which_succ) };
	    break;
	case Op.IF_ACMPEQ:
	case Op.IF_ACMPNE:
	    {
		State ns = s.pop(2);
		q = new OPER(in, "acmpeq", s.extra(0), 
			     new Temp[] { s.stack(1), s.stack(0) });
		Quad q2 = new CJMP(in, q.def()[0], new Temp[0]);
		Quad.addEdge(q, 0, q2, 0);
		int iffalse=0, iftrue=1;
		if (in.getOpcode()==Op.IF_ACMPNE) { // invert things for NE.
		    iffalse=1; iftrue=0;
		}
		r = new TransState[] {
		    new TransState(ns, in.next()[0], q2, iffalse),
		    new TransState(ns, in.next()[1], q2, iftrue)
		};
		break;
	    }
	case Op.IFNULL:
	case Op.IFNONNULL:
	    {
		State ns = s.pop();
		Quad q0 = new OPER(in, "acmpeq", s.extra(0), 
				   new Temp[] { s.stack(0), SS.Tnull });
		Quad q1 = new CJMP(in, q0.def()[0], new Temp[0]);
		Quad.addEdge(q0, 0, q1, 0);
		int iffalse=0, iftrue=1;
		if (in.getOpcode()==Op.IFNONNULL) { // invert things
		    iffalse=1; iftrue=0;
		}
		q = q0;
		r = new TransState[] {
		    new TransState(ns, in.next()[0], q1, iffalse),
		    new TransState(ns, in.next()[1], q1, iftrue)
		};
		break;
	    }
	case Op.IFEQ:
	case Op.IFNE:
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
		byte opcode = in.getOpcode();
		State ns;

		boolean invert = false;
		String op = null;
		switch (opcode) {
		case Op.IFNE:
		case Op.IF_ICMPNE:
		    invert = true;
		case Op.IFEQ:
		case Op.IF_ICMPEQ:
		    op = "icmpeq";
		    break;
		case Op.IFLT:
		case Op.IF_ICMPLT:
		    invert = true;
		case Op.IFGE:
		case Op.IF_ICMPGE:
		    op = "icmpge";
		    break;
		case Op.IFLE:
		case Op.IF_ICMPLE:
		    invert = true;
		case Op.IFGT:
		case Op.IF_ICMPGT:
		    op = "icmpgt";
		    break;
		default: Util.assert(false);
		}
		if (opcode>=Op.IFEQ && opcode<=Op.IFLE) {
		    ns = s.pop();
		    q = new OPER(in, op, ns.extra(0),
				 new Temp[] { s.stack(0), SS.Tzero });
		} else {
		    ns = s.pop(2);
		    q = new OPER(in, op, ns.extra(0),
				 new Temp[] { s.stack(1), s.stack(0) } );
		}
		Quad Qc = new CJMP(in, q.def()[0], new Temp[0]);
		Quad.addEdge(q, 0, Qc, 0);
		r = new TransState[] {
		    new TransState(ns, in.next()[0], Qc, invert?1:0),
		    new TransState(ns, in.next()[1], Qc, invert?0:1)
		};
		break;
	    }
	case Op.JSR:
	case Op.JSR_W:
	case Op.RET:
	    Util.assert(false); // should be caught at higher level.
	default:
	    throw new Error("Unknown InCti: "+in.toString());
	}
	if (q!=null)
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	return r;
    }

    /** Translate a throw instruction, taking the try context into account.*/
    static final TransState[] transThrow(StaticState SS, TransState ts, 
					 MergeMap handlers,
					 boolean possiblyNull)
    {
	Vector rTS = new Vector();
	State ns = ts.initialState.enterCatch();
	Quad header = ts.header; int which_succ = ts.which_succ;
	Temp Tex = ns.stack(0); // where the Exception is.
	if (possiblyNull) {
	    HClass hc = HClass.forClass(NullPointerException.class);
	    // Handle 'throw null;':
	    //   Exception Tex = stack.top();
	    //   if (Tex == null) {
	    //     try { Tex = new NullPointerException(); }
	    //     catch (Throwable t) { Tex = t; }
	    //   }
	    Quad q2 = new OPER(ts.in, "acmpeq", ns.extra(0),
			       new Temp[] { Tex, SS.Tnull } );
	    Quad q3 = new CJMP(ts.in, q2.def()[0], new Temp[0]);
	    Quad q4 = new NEW(ts.in, Tex, hc);
	    Quad q5 = new CALL(ts.in, hc.getConstructor(new HClass[0]),
			       Tex, new Temp[0], null /*retval*/,
			       ns.extra(0)/*exception*/, true /*special*/);
	    Quad q6 = new OPER(ts.in, "acmpeq", ns.extra(1),
			       new Temp[] { q5.def()[0], SS.Tnull } );
	    Quad q7 = new CJMP(ts.in, q6.def()[0], new Temp[0]);
	    Quad q8 = new MOVE(ts.in, Tex, q5.def()[0]);
	    Quad q9 = new PHI(ts.in, new Temp[0], 3);
	    // Link these:
	    Quad.addEdge(header, which_succ, q2, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q9, 0);
	    Quad.addEdge(q3, 1, q4, 0);
	    Quad.addEdge(q4, 0, q5, 0);
	    Quad.addEdge(q5, 0, q6, 0);
	    Quad.addEdge(q6, 0, q7, 0);
	    Quad.addEdge(q7, 0, q9, 1);
	    Quad.addEdge(q7, 1, q8, 0);
	    Quad.addEdge(q8, 0, q9, 2);
	    // Update state.
	    header = q9; which_succ = 0;
	}
	// Now look up through the try blocks for potential handlers.
	int i; for (i=0; i<SS.allTries.length; i++) {
	    if (SS.allTries[i].inTry(ts.in)) {
		Instr hI = SS.allTries[i].handler();
		// expand phi.
		PHI phi = handlers.getPhi(hI);
		State phiState = handlers.getState(hI);
		if (phi==null) {
		    phi=new PHI(hI, new Temp[0], 0);
		    phiState = ns;
		    // make a new TransState
		    rTS.addElement(new TransState(ns, hI, phi, 0));
		}
		phi.grow(new Temp[0]);
		handlers.put(hI, phi, phi.prev.length, phiState);

		if (SS.allTries[i].caughtException()==null) { // catch any.
		    for (int j=0; j < phiState.stackSize ; j++) {
			if (phiState.stack(j) == null ||
			    phiState.stack(j) == ns.stack(j)) continue;
			Quad q1 = new MOVE(ts.in, 
					   phiState.stack(j), ns.stack(j));
			Quad.addEdge(header, which_succ, q1, 0);
			header = q1; which_succ = 0;
		    }
		    Quad.addEdge(header, which_succ, phi, phi.prev.length-1);
		    break; // no more try processing.
		} else {
		    Quad q1 = new INSTANCEOF(ts.in, phiState.extra(0), Tex,
					     SS.allTries[i].caughtException());
		    Quad q2 = new CJMP(ts.in, q1.def()[0], new Temp[0]);
		    // link quads.
		    Quad.addEdge(header, which_succ, q1, 0);
		    Quad.addEdge(q1, 0, q2, 0);
		    // make renaming move statements.
		    Quad Q = q2; int W = 1;
		    for (int j=0; j < phiState.stackSize; j++) {
			if (phiState.stack(i) == null ||
			    phiState.stack(i) == ns.stack(i)) continue;
			Quad q3 = new MOVE(ts.in,
					   phiState.stack(i), ns.stack(i));
			Quad.addEdge(Q, W, q3, 0);
			Q = q3; W = 0;
		    }
		    Quad.addEdge(Q, W, phi, phi.prev.length-1);
		    header = q2; which_succ = 0;
		}
	    }
	}
	if (i==SS.allTries.length) { // didn't break early on 'catch any'
	    // exception not caught in any try.  Throw it.
	    Quad q = new THROW(ts.in, Tex);
	    Quad.addEdge(header, which_succ, q, 0);
	    SS.footer.attach(q, 0);
	}
	// grok rTS into TransState[]
	TransState[] r = new TransState[rTS.size()];
	rTS.copyInto(r);
	return r;
    }
    // takes a state to append to and returns the final quad.
    static final Quad transNewException(StaticState SS, HClass exClass,
					Temp Tex, TransState ts) {
	State s = ts.initialState;
	Instr in = ts.in;
	Quad  header = ts.header;
	int   which_succ = ts.which_succ;

	Quad q0 = new NEW(in, Tex, exClass);
	Quad q1 = new CALL(in, exClass.getConstructor(new HClass[0]),
			   q0.def()[0], new Temp[0], null /*retval*/,
			   s.extra(0) /*ex*/, true /*special*/);
	// check whether the constructor threw an exception.
	Quad q2 = new OPER(in, "acmpeq", s.extra(1),
			   new Temp[] { q1.def()[0], SS.Tnull } );
	Quad q3 = new CJMP(in, q2.def()[0], new Temp[0]);
	Quad q4 = new MOVE(in, Tex, q1.def()[0]);
	Quad q5 = new PHI(in, new Temp[0], 2);

	Quad.addEdge(header, which_succ, q0, 0);
	Quad.addEdges(new Quad[] { q0, q1, q2, q3, q4, q5 } );
	Quad.addEdge(q3, 1, q5, 1);
	return q5;
    }
    static final TransState[] transNullCheck(StaticState SS, Temp Tobj,
					     Quad q, MergeMap handlers,
					     TransState ts) {
	HClass HCex = HClass.forClass(NullPointerException.class);
	State s = ts.initialState;

	Quad q0 = new OPER(ts.in, "acmpeq", s.extra(0),
			   new Temp[] { Tobj, SS.Tnull } );
	Quad q1 = new CJMP(ts.in, q0.def()[0], new Temp[0]);
	Quad q2 = transNewException(SS, HCex, SS.Tex, 
				    new TransState(s, ts.in, q1, 1));
	TransState[] r = 
	    transThrow(SS, new TransState(ts.initialState.push(SS.Tex), 
					  ts.in, q2, 0),
		       handlers, false);
	// actual operation is q.
	// link quads.
	Quad.addEdge(ts.header, ts.which_succ, q0, 0);
	Quad.addEdges(new Quad[] { q0, q1, q } );

	return r;
    }
    static final TransState[] transBoundsCheck(StaticState SS,
					       Temp Tobj, Temp Tindex,
					       Quad q, MergeMap handlers,
					       TransState ts) {
	// if (obj==null) throw new NullPointerException();
	// if (0<=index && index<obj.length) do(q); /* actual operation */
	// else throw new ArrayIndexOutOfBoundsException();

	HClass HCnull = HClass.forClass(NullPointerException.class);
	HClass HCoob  = HClass.forClass(ArrayIndexOutOfBoundsException.class);

	State s = ts.initialState;

	Quad q0 = new OPER(ts.in, "acmpeq", s.extra(0),
			  new Temp[] { Tobj, SS.Tnull });
	Quad q1 = new CJMP(ts.in, q0.def()[0], new Temp[0]);
	Quad q2 = transNewException(SS, HCnull, SS.Tex, 
				    new TransState(s, ts.in, q1, 1));
	// array bounds check.
	Quad q3 = new OPER(ts.in, "icmpge", s.extra(0),
			   new Temp[] { Tindex, SS.Tzero });
	Quad q4 = new CJMP(ts.in, q3.def()[0], new Temp[0]);
	Quad q5 = new ALENGTH(ts.in, s.extra(1), Tobj);
	Quad q6 = new OPER(ts.in, "icmpgt", s.extra(0),
			   new Temp[] { q5.def()[0], Tindex });
	Quad q7 = new CJMP(ts.in, q6.def()[0], new Temp[0]);
	Quad q8 = new PHI(ts.in, new Temp[0], 2);
	Quad q9 = transNewException(SS, HCoob, SS.Tex,
				    new TransState(s, ts.in, q8, 0));
	// throw exception if necessary.
	Quad q10= new PHI(ts.in, new Temp[0], 2);
	TransState[] r = 
	    transThrow(SS, new TransState(ts.initialState.push(SS.Tex), 
					  ts.in, q10, 0),
		       handlers, false);
	// link.
	Quad.addEdge(ts.header, ts.which_succ, q0, 0);
	Quad.addEdges(new Quad[] { q0, q1, q3, q4, q8 } );
	Quad.addEdge(q2, 0, q10,0);
	Quad.addEdge(q4, 1, q5, 0);
	Quad.addEdges(new Quad[] { q5, q6, q7 } );
	Quad.addEdge(q7, 0, q8, 1);
	Quad.addEdge(q7, 1, q,  0); /* actual operation, after bounds check */
	Quad.addEdge(q9, 0, q10,1);
	// done.
	return r;
    }

    // Miscellaneous utility functions. ///////////////////////////

    /** Determine if an HClass needs to be represented by one or two bytecode
     *  stack entries. */
    private static final boolean isLongDouble(HClass hc) {
	if (hc == HClass.Long || hc == HClass.Double)
	    return true;
	return false;
    }

    /** Merge two TransState arrays. */
    private static final TransState[] mergeTS(TransState[] a, TransState[] b) {
	if (a.length==0) return b;
	if (b.length==0) return a;
	TransState[] r = new TransState[a.length + b.length];
	System.arraycopy(a, 0, r, 0, a.length);
	System.arraycopy(b, 0, r, a.length, b.length);
	return r;
    }
}
