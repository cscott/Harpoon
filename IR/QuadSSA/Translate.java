// Translate.java, created Sat Aug  8 10:53:03 1998 by cananian
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
 * @version $Id: Translate.java,v 1.68 1998-09-14 21:48:33 cananian Exp $
 */

class Translate  { // not public.
    /** <code>State</code> represents the stack, local variable, and block
     *  context (for try and monitor quads) of a given bytecode Instr. */
    static class State { // inner class
	/** Current temps used for each position of stack.
	 *  <code>null</code>s are valid placeholders for empty spaces
	 *  in double-word representations. */
	Temp stack[];
	/** Current temps used for local variables */
	Temp lv[];
	/** Current monitor nesting level */
	int monitorDepth;
	/** All try blocks for this method. */
	ExceptionEntry allTries[];
	/** The footer for this block. */
	FOOTER footer;

	static class BlockContext {
	/** previous footer block for nested monitors (exitMonitor uses) */
	    FOOTER oldFooter;
	/** Temp to store exit destination instr index in. */
	    Temp exitTemp;
	/** Continuation TransStates at exit of monitor block. */
	    Vector continuation;
	/** Indicates the type of context this is. */
	    int type;
	    static final int MONITOR = 0;
	    static final int JSR = 1;
	/** Constructor */
	    BlockContext(FOOTER of, Temp et, Vector c, int ty) {
		oldFooter = of; exitTemp = et; continuation = c; type = ty;
	    }
	    BlockContext(FOOTER of, int ty) {
		this(of, new Temp(), new Vector(), ty);
	    }
	}
	/** A stack of try/monitor context information. */
	BlockContext nest[];

	/** Constructor. */
	private State(Temp stack[], Temp lv[], int monitorDepth, 
		      BlockContext nest[], ExceptionEntry allTries[],
		      FOOTER footer) {
	    this.stack = stack; this.lv = lv;
	    this.monitorDepth = monitorDepth;
	    this.nest = nest;
	    this.allTries = allTries;
	    this.footer = footer;
	}
	/** Make new state by popping top of stack */
	State pop() { return pop(1); }
	/** Make new state by popping multiple entries off top of stack */
	State pop(int n) {
	    Temp stk[] = (Temp[]) shrink(this.stack, n);
	    return new State(stk, lv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by pushing temp onto top of stack */
	State push(Temp t) {
	    Temp stk[] = (Temp[]) grow(this.stack);
	    stk[0] = t;
	    return new State(stk, lv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by changing the temp corresponding to an lv. */
	State assignLV(int lv_index, Temp t) {
	    Temp nlv[] = (Temp[]) lv.clone();
	    nlv[lv_index] = t;
	    return new State(stack, nlv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by renaming a the Stack or Local Variable slots.
	 */
	State rename(Temp Told, Temp Tnew) {
	    Temp nlv[] = this.lv;
	    Temp stk[] = this.stack;
	    for (int i=0; i<nlv.length;i++)
		if (nlv[i]==Told) {
		    if (nlv==this.lv) nlv = (Temp[]) this.lv.clone();
		    nlv[i]=Tnew;
		}
	    for (int i=0; i<stk.length;i++)
		if (stk[i]==Told) {
		    if (stk==this.stack) stk = (Temp[]) this.stack.clone();
		    stk[i]=Tnew;
		}
	    if (nlv==this.lv && stk==this.stack) return this;
	    return new State(stk, nlv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by renaming all the Stack and Local Variable slots.
	 */
	State rename() {
	    Temp stk[] = new Temp[this.stack.length];
	    Temp nlv[] = new Temp[this.lv.length];
	    for (int i=0; i<this.stack.length; i++)
		if (this.stack[i] == null)
		    stk[i] = null;
		else
		    stk[i] = new Temp(this.stack[i]);
	    for (int i=0; i<this.lv.length; i++)
		if (this.lv[i] == null)
		    nlv[i] = null;
		else
		    nlv[i] = new Temp(this.lv[i]);
	    return new State(stk, nlv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by clearing all by the top entry of the stack. */
	State enterCatch() {
	    Temp stk[] = new Temp[] { this.stack[0] };
	    return new State(stk, lv, monitorDepth, nest, allTries, footer);
	}
	/** Make new state by entering a monitor block. */
	State enterMonitor(FOOTER newFooter) {
	    BlockContext ns[] = (BlockContext[]) grow(this.nest);
	    ns[0] = new BlockContext(this.footer, BlockContext.MONITOR);
	    return new State(stack, lv, monitorDepth+1, ns, allTries, 
			     newFooter);
	}
	/** Make new state by exiting a monitor block. */
	State exitMonitor() {
	    Util.assert(monitorDepth>0);
	    BlockContext ns[] = (BlockContext[]) shrink(this.nest);
	    return new State(stack, lv, monitorDepth-1, ns, allTries, 
			     this.nest[0].oldFooter);
	}
	/** Make new state by entering a JSR/RET block. The "return address"
	 *  <code>t</code> gets pushed on top of the stack. */
	State enterJSR(Temp t) {
	    BlockContext ns[] = (BlockContext[]) grow(this.nest);
	    ns[0] = new BlockContext(this.footer, BlockContext.JSR);
	    return new State(stack, lv, monitorDepth, ns, allTries, footer).
		push(t);
	}
	/** Make new state, as when exiting a JSR/RET block. */
	State exitJSR() {
	    BlockContext ns[] = (BlockContext[]) shrink(this.nest);
	    return new State(stack, lv, monitorDepth, ns, allTries, 
			     this.nest[0].oldFooter);
	}

	/** Initialize state with temps corresponding to parameters. */
	State(Temp[] locals, ExceptionEntry[] allTries, FOOTER footer) {
	    this(new Temp[0], locals, 0, new BlockContext[0], 
		 allTries, footer);
	}
	/** Creates a new State object identical to this one. */
	public Object clone() {
	    return new State((Temp[]) stack.clone(), 
			     (Temp[]) lv.clone(), 
			     monitorDepth,
			     (BlockContext[]) nest.clone(), 
			     allTries,
			     footer);
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
	private Hashtable phimap, predmap;
	MergeMap() { 
	    phimap = new Hashtable(); 
	    predmap= new Hashtable();
	}
	void put(Instr in, PHI phi, int which_pred) 
	{ phimap.put(in, phi); predmap.put(in, new Integer(which_pred)); }
	PHI getPhi(Instr in) 
	{ return (PHI) phimap.get(in); }
	int getPred(Instr in) 
	{ return ((Integer) predmap.get(in)).intValue(); }
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
	State s = new State(locals, bytecode.getTryBlocks(),
			    new FOOTER(firstInstr));

	HEADER quads = new METHODHEADER(firstInstr, s.footer, params);

	Temp Tnull = new Temp("$null");
	Temp Tzero = new Temp("$zero");
	Quad q1 = new CONST(quads, Tnull, null, HClass.Void);
	Quad q2 = new CONST(quads, Tzero, new Integer(0), HClass.Int);
	Quad.addEdge(quads, 0, q1, 0);
	Quad.addEdge(q1, 0, q2, 0);

	// if method is synchronized, place inside MONITOR
	Quad q = q2;
	if (isSynchronized) {
	    MONITOR Qm = new MONITOR(firstInstr, s.lv[0],
				     new HEADER(firstInstr, s.footer));
	    Quad.addEdge(q2, 0, Qm, 0);

	    quads.footer = new FOOTER(firstInstr);
	    quads.footer.attach(Qm, 0);

	    q = Qm.block;
	}

	// translate using state.
	trans(new TransState(s, firstInstr, q, 0),
	      Tzero, Tnull);

	// FIXME: move all RETURN/THROW inside MONITOR body to
	// outside (for synchronized methods only).

	// return result.
	return quads;
    }

    /** Translate a block starting with a given <code>TransState</code>.<p> 
     *  Start at <code>ts.in</code> using <code>ts.initialState</code>. */
    static final void trans(TransState ts0, Temp Tzero, Temp Tnull) {
	Stack todo = new Stack(); todo.push(ts0);
	MergeMap mm = new MergeMap();
	MergeMap handlers = new MergeMap();

	while (!todo.empty()) {
	    TransState ts = (TransState) todo.pop();
	    // convenience abbreviations of TransState fields.
	    State s = ts.initialState;

	    // Are we entering a new MONITOR or JSR/RET block?
	    if ((ts.in.getOpcode() == Op.MONITORENTER) ||
		(ts.in.getOpcode() == Op.JSR) ||
		(ts.in.getOpcode() == Op.JSR_W)) {
		boolean isMonitor = (ts.in.getOpcode() == Op.MONITORENTER);

		Quad q; State ns;
		if (isMonitor) { // MONITOR
		    // Make header nodes for block.
		    HEADER monitorBlock = new HEADER(ts.in,
						     new FOOTER(ts.in));
		    // Recursively generate monitor quads.
		    ns = s.enterMonitor(monitorBlock.footer).pop();
		    trans(new TransState(ns, ts.in.next()[0], monitorBlock, 0),
			  Tzero, Tnull);
		    // Make and link MONITOR
		    Temp Ttested = new Temp(s.stack[0]);
		    q = new MONITOR(ts.in, Ttested, monitorBlock);
		    // Create null pointer test.
		    TransState r[] = transNullCheck(s.stack[0], Ttested,
						    Tnull, q, handlers, ts);
		    for (int i=r.length-1; i>=0; i--)
			todo.push(r[i]);
		} else { // JSR/RET
		    q = null;
		    ns = s.enterJSR(Tnull);
		    Instr in = ts.in.next()[1];
		    if (in instanceof InMerge) in = in.next()[0];
		    trans(new TransState(ns, ts.in.next()[1].next()[0], 
					 ts.header, ts.which_succ),
			  Tzero, Tnull);
		}

		// make PHI & FOOTER at exit of MONITOR/JSR
		PHI exitBlock =
		    new PHI(ts.in, new Temp[] { ns.nest[0].exitTemp },
		            ns.nest[0].continuation.size());

		if (isMonitor)
		    ns.footer.attach(exitBlock, 0);
		else // JSR/RET
		    q = exitBlock;
		
		// make post-block quads.
		int i;
		for (i=0; i<ns.nest[0].continuation.size(); i++) {
		    TransState tsi = 
			(TransState) ns.nest[0].continuation.elementAt(i);
		    // exitTemp=i; goto exitBlock;
		    CONST c0 = new CONST(tsi.in, new Temp(ns.nest[0].exitTemp),
					new Integer(i), HClass.Int);
		    Quad.addEdge(tsi.header, tsi.which_succ, c0, 0);
		    //  jmp
		    Quad.addEdge(c0, 0, exitBlock, i);
		    exitBlock.src[0][i] = c0.dst;
		    
		    // different test for final val.
		    if (i==ns.nest[0].continuation.size()-1)
			break;

		    // if exitTemp==i then goto continuation[i]
		    Quad q0 = new CONST(tsi.in, new Temp(),
					new Integer(i), HClass.Int);
		    Quad q1 = new OPER(tsi.in, "icmpeq", new Temp(),
				       new Temp[] { ns.nest[0].exitTemp,
						    q0.def()[0] });
		    LAMBDA q2 = new CJMP(tsi.in, q1.def()[0],
					 new Temp[] { ns.nest[0].exitTemp });
		    stuff(q2);
		    Quad.addEdge(q, 0, q0, 0);
		    Quad.addEdge(q0,0, q1, 0);
		    Quad.addEdge(q1,0, q2, 0);
		    Instr in;
		    if (isMonitor) {
			ns = tsi.initialState.exitMonitor();
			in = tsi.in.next()[0];
		    } else { // if (isJSR)
			ns = tsi.initialState.exitJSR();
			in = ts.in.next()[0];
		    }
		    todo.push(new TransState(ns, in, q2, 1));
		    q = q2;
		}
		// default branch.
		TransState tsi = 
		    (TransState) ns.nest[0].continuation.elementAt(i);
		Instr in;
		if (isMonitor) {
		    ns = tsi.initialState.exitMonitor();
		    in = tsi.in.next()[0];
		} else { // if (isJSR)
		    ns = tsi.initialState.exitJSR();
		    in = ts.in.next()[0];
		}
		todo.push(new TransState(ns, in, q, 0));
		continue;
	    }
	    // Are we exiting a MONITOR or JSR block?
	    else if (s.nest.length>0 &&
		     ((s.nest[0].type == State.BlockContext.MONITOR &&
		       ts.in.getOpcode() == Op.MONITOREXIT) ||
		      (s.nest[0].type == State.BlockContext.JSR &&
		       ts.in.getOpcode() == Op.RET))) {
		State ns = (ts.in.getOpcode()==Op.MONITOREXIT)?s.pop():s;
		s.nest[0].continuation.
		    addElement(new TransState(ns, ts.in,
					      ts.header, ts.which_succ));
		// we'll fix up the dangling end later.
		continue;
	    }
	    // None of the above.
	    else {
		TransState nts[] = transInstr(ts, mm, handlers, Tzero, Tnull);
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
    static final TransState[] transInstr(TransState ts, 
					 MergeMap mm, MergeMap handlers,
					 Temp Tzero, Temp Tnull) {
	// Dispatch to correct specific function.
	//System.out.println("Translating "+Op.toString(ts.in.getOpcode()));
	if (ts.in instanceof InGen)    return transInGen(ts, handlers, 
							 Tzero, Tnull);
	if (ts.in instanceof InSwitch) return transInSwitch(ts);
	if (ts.in instanceof InCti)    return transInCti(ts, handlers, 
							 Tzero, Tnull);
	if (ts.in instanceof InMerge)  return transInMerge(ts, mm);
	throw new Error("Unknown Instr type.");
    }

    /** Translate an <code>InGen</code>. 
     *  @return a <Code>TransState[]</code> of length zero or one. */
    static final TransState[] transInGen(TransState ts, MergeMap handlers,
					 Temp Tzero, Temp Tnull) {
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
		ns = s.pop(2).push(null).push(new Temp()); // 64-bit val.
	    else
		ns = s.pop(2).push(new Temp()); // 32-bit val

	    Temp Tobj  = s.stack[1]; Temp TobjTested = new Temp(Tobj);
	    Temp Tindex= s.stack[0]; Temp TindexTested = new Temp(Tindex);
	    // the actual operation.
	    Quad q0= new AGET(in, ns.stack[0], TobjTested, TindexTested);
	    // bounds check
	    r = transBoundsCheck(Tobj, TobjTested, Tindex, TindexTested,
				 Tnull, Tzero, q0, handlers, ts);
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
	    TransState ts1 = ts;
	    Temp Tobj, Tindex, Tsrc;
	    if (in.getOpcode()==Op.DASTORE ||
		in.getOpcode()==Op.LASTORE) { // 64-bit val.
		ns = s.pop(4);
		Tobj   = s.stack[3];
		Tindex = s.stack[2];
		Tsrc   = s.stack[0];
	    } else { // 32-bit val.
		ns = s.pop(3);
		Tobj   = s.stack[2];
		Tindex = s.stack[1];
		Tsrc   = s.stack[0];
	    }
	    
	    // funky additional check for AASTORE
	    if (in.getOpcode() == Op.AASTORE) {
		// AASTORE also throws ArrayStoreException.
		HClass HCase = HClass.forClass(ArrayStoreException.class);
		Temp Tex = new Temp();
		Quad   qq0 = new COMPONENTOF(in, new Temp(), Tobj, Tsrc);
		LAMBDA qq1 = new CJMP(in, qq0.def()[0], new Temp[] {Tobj});
		stuff(qq1);
		Quad   qq2 = transNewException(HCase, Tex, Tnull, in, qq1, 0);
		r = transThrow(new TransState(ts.initialState.push(Tex)
					      .rename(Tobj, qq1.dst[0][0]), 
					      ts.in, qq2, 0),
			       handlers, Tnull, false);
		// fixup lambda
		Temp TobjTested = qq1.dst[0][1];
		ns = ns.rename(Tobj, TobjTested);
		Tobj = TobjTested;
		// link
		Quad.addEdge(ts.header, ts.which_succ, qq0, 0);
		Quad.addEdge(qq0, 0, qq1, 0);
		ts1 = new TransState(ts.initialState, ts.in, qq1, 1);
	    }
	    // the actual operation.
	    Temp TobjTested = new Temp(Tobj);
	    Temp TindexTested = new Temp(Tindex);

	    Quad q0= new ASET(in, TobjTested, TindexTested, Tsrc);
	    ns = ns.rename(Tobj, TobjTested).rename(Tindex, TindexTested);
	    // bounds check
	    TransState r2[] = transBoundsCheck(Tobj, TobjTested,
					       Tindex, TindexTested,
					       Tnull, Tzero,
					       q0, handlers, ts1);
	    // merge TransState arrays.
	    r = mergeTS(r, r2);
	    // set up next state.
	    q = ts.header.next()[ts.which_succ];
	    last = q0;
	    // done.
	    break;
	    }
	case Op.ACONST_NULL:
	    ns = s.push(Tnull);
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
		ns = s.pop().push(new Temp());

		HClass HCex=HClass.forClass(NegativeArraySizeException.class);
		Temp Tex   = new Temp();

		// check whether count>=0.
		Temp Tsize = s.stack[0];
		Temp TsizeTested;
		Quad q2 = new OPER(in, "icmpge", new Temp(),
				   new Temp[] { s.stack[0], Tzero });
		LAMBDA q3 = new CJMP(in, q2.def()[0], 
				     new Temp[] { s.stack[0] });
		stuff(q3);
		Quad q4 = transNewException(HCex, Tex, Tnull, in, q3, 0);
		r = transThrow(new TransState(s.push(Tex)
					      .rename(Tsize, q3.dst[0][0]), 
					      in, q4, 0),
			       handlers, Tnull, false);
		TsizeTested = q3.dst[0][1];
		Quad q5 = new ANEW(in, ns.stack[0], hc, 
				   new Temp[] { TsizeTested });
		ns = ns.rename(Tsize, TsizeTested);
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
	    ns = s.pop().push(new Temp());

	    Temp Tobj  = s.stack[0];
	    Temp TobjTested = new Temp(Tobj);

	    // actual operation:
	    Quad q0 = new ALENGTH(in, ns.stack[0], TobjTested);
	    // null check.
	    r = transNullCheck(Tobj, TobjTested, Tnull, q0, handlers, ts);
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
	    ns = s.pop().assignLV(opd.getIndex(), 
				  new Temp(s.lv[opd.getIndex()]));
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack[0]);
	    break;
	    }
	case Op.BIPUSH:
	case Op.SIPUSH:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		int val = ((Byte)opd.getValue()).intValue();
		ns = s.push(new Temp("$const"));
		q = new CONST(in, ns.stack[0], new Integer(val), HClass.Int);
		break;
	    }
	case Op.CHECKCAST:
	    // translate as:
	    //  if (obj!=null && !(obj instanceof class))
	    //     throw new ClassCastException();
	    {
		OpClass opd = (OpClass) in.getOperand(0);
		Temp Tobj = s.stack[0];
		Temp Tr0 = new Temp();
		Temp Tr1 = new Temp();
		Temp Tex = new Temp("$checkcast");

		HClass HCex = HClass.forClass(ClassCastException.class);

		// make quads
		Quad q1 = new OPER(in, "acmpeq", Tr0, // equal is true
				   new Temp[] { Tobj, Tnull } ); 
		LAMBDA q2 = new CJMP(in, Tr0, new Temp[] { Tobj } );
		stuff(q2);
		Quad q3 = new INSTANCEOF(in, Tr1, q2.dst[0][0], opd.value());
		LAMBDA q4 = new CJMP(in, Tr1, new Temp[] { q2.dst[0][0] } );
		stuff(q4);
		Quad q5 = transNewException(HCex, Tex, Tnull, in, q4, 0);
		r = transThrow(new TransState(s.push(Tex)
					      .rename(Tobj, q4.dst[0][0]), 
					      in, q5, 0),
			       handlers, Tnull, false);
		Quad q6 = new PHI(in, 
				  new Temp[] { new Temp(Tobj) },
				  new Temp[][]{new Temp[] { q2.dst[0][1],
							    q4.dst[0][1]}}, 2);
		// link quads.
		Quad.addEdge(q1, 0, q2, 0);
		Quad.addEdge(q2, 0, q3, 0);
		Quad.addEdge(q2, 1, q6, 0);
		Quad.addEdge(q3, 0, q4, 0);
		Quad.addEdge(q4, 1, q6, 1);
		// and setup the next state.
		ns = s.rename(Tobj, q6.def()[0]);
		q = q1;
		last = q6;
		break;
	    }
	case Op.D2F:
	case Op.D2I:
	case Op.L2F:
	case Op.L2I:
	    ns = s.pop(2).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()) /* "d2f" or "d2i" */,
			 ns.stack[0], new Temp[] { s.stack[0] });
	    break;
	case Op.D2L:
	case Op.L2D:
	    ns = s.pop(2).push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()) /* "d2l" or "l2d" */,
			 ns.stack[0], new Temp[] { s.stack[0] });
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
	    ns = s.pop(4).push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // dadd, ddiv or dmul
			 ns.stack[0], new Temp[] { s.stack[2], s.stack[0] });
	    break;
	case Op.DCMPG:
	case Op.DCMPL:
	    ns = s.pop(4).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode())/*"dcmpg" or "dcmpl"*/,
			 ns.stack[0], new Temp[] { s.stack[2], s.stack[0] });
	    break;
	case Op.DCONST_0:
	case Op.DCONST_1:
	case Op.LCONST_0:
	case Op.LCONST_1:
	    {
		OpConstant opd = (OpConstant) in.getOperand(0);
		ns = s.push(null).push(new Temp("$const"));
		q = new CONST(in, ns.stack[0], opd.getValue(), opd.getType());
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
	    ns = s.pop(2).push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()) /*"dneg" or "lneg"*/, 
			 ns.stack[0], new Temp[] {s.stack[0]});
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
	    ns = s.pop(2).assignLV(opd.getIndex(), 
				  new Temp(s.lv[opd.getIndex()]));
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack[0]);
	    break;
	    }
	case Op.DUP:
	    ns = s.push(s.stack[0]);
	    q = null;
	    break;
	case Op.DUP_X1:
	    ns = s.pop(2).push(s.stack[0]).push(s.stack[1]).push(s.stack[0]);
	    q = null;
	    break;
	case Op.DUP_X2:
	    ns = s.pop(3).push(s.stack[0]).push(s.stack[2]).push(s.stack[1])
		.push(s.stack[0]);
	    q = null;
	    break;
	case Op.DUP2:
	    ns = s.push(s.stack[1]).push(s.stack[0]);
	    q = null;
	    break;
	case Op.DUP2_X1:
	    ns = s.pop(3).push(s.stack[1]).push(s.stack[0])
		.push(s.stack[2]).push(s.stack[1]).push(s.stack[0]);
	    q = null;
	    break;
	case Op.DUP2_X2:
	    ns = s.pop(4).push(s.stack[1]).push(s.stack[0])
		.push(s.stack[3]).push(s.stack[2])
		.push(s.stack[1]).push(s.stack[0]);
	    q = null;
	    break;
	case Op.F2D:
	case Op.F2L:
	case Op.I2D:
	case Op.I2L:
	    ns = s.pop().push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // "f2d" or "f2l"
			 ns.stack[0], new Temp[] {s.stack[0]});
	    break;
	case Op.F2I:
	case Op.I2B:
	case Op.I2C:
	case Op.I2F:
	case Op.I2S:
	    ns = s.pop().push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()),
			 ns.stack[0], new Temp[] {s.stack[0]});
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
	    ns = s.pop(2).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // fadd, fdiv, ...
			 ns.stack[0], new Temp[] {s.stack[1], s.stack[0]});
	    break;
	case Op.IDIV:
	case Op.IREM:
	    {
	    ns = s.pop(2).push(new Temp());

	    // if (divisor==0) throw new ArithmeticException();
	    HClass HCex = HClass.forClass(ArithmeticException.class);
	    Temp Tex = new Temp();

	    Temp Tdiv = s.stack[0];
	    Temp TdivTested;

	    Quad q0 = new OPER(in, "icmpeq", new Temp(),
			       new Temp[] { Tdiv, Tzero } );
	    LAMBDA q1 = new CJMP(in, q0.def()[0], new Temp[] { Tdiv });
	    stuff(q1); TdivTested = q1.dst[0][0];
	    Quad q2 = transNewException(HCex, Tex, Tnull, in, q1, 1);
	    r = transThrow(new TransState(s.push(Tex)
					  .rename(Tdiv,q1.dst[0][1]),
					  in, q2, 0),
			   handlers, Tnull, false);
	    // actual division operation:
	    Quad q3 = new OPER(in, Op.toString(in.getOpcode()), // idiv/irem
			       ns.stack[0],
			       new Temp[] {s.stack[1], TdivTested});
	    // link quads.
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q3, 0);
	    // setup next state.
	    q = q0; last = q3; ns = ns.rename(Tdiv, TdivTested);
	    break;
	    }
	case Op.LDIV:
	case Op.LREM:
	    {
	    ns = s.pop(4).push(null).push(new Temp());

	    // if (divisor==0) throw new ArithmeticException();
	    HClass HCex = HClass.forClass(ArithmeticException.class);
	    Temp Tex = new Temp();

	    Temp Tdiv = s.stack[0];

	    Quad q0 = new CONST(in, new Temp("$zeroL"), 
				new Long(0), HClass.Long);
	    Quad q1 = new OPER(in, "lcmpeq", new Temp(),
			       new Temp[] { Tdiv, q0.def()[0] } );
	    LAMBDA q2 = new CJMP(in, q1.def()[0], new Temp[] { Tdiv });
	    stuff(q2);
	    Quad q3 = transNewException(HCex, Tex, Tnull, in, q2, 1);
	    r = transThrow(new TransState(s.push(Tex)
					  .rename(Tdiv, q2.dst[0][1]), 
					  in, q3, 0),
			   handlers, Tnull, false);
	    // actual division operation:
	    Temp TdivTested = q2.dst[0][0];
	    Quad q4 = new OPER(in, Op.toString(in.getOpcode()), // ldiv/lrem
			       ns.stack[0], 
			       new Temp[] {s.stack[2], TdivTested});
	    // link quads.
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q2, 0, q4, 0);
	    // setup next state.
	    q = q0; last = q4; ns = ns.rename(Tdiv, TdivTested);
	    break;
	    }
	case Op.FCMPG:
	case Op.FCMPL:
	    ns = s.pop(2).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode())/*"fcmpg" or "fcmpl"*/,
			 ns.stack[0], new Temp[] { s.stack[1], s.stack[0] });
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
		ns = s.push(new Temp("$const"));
		q = new CONST(in, ns.stack[0], opd.getValue(), opd.getType());
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
	    ns = s.pop().push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), 
			 ns.stack[0], new Temp[] {s.stack[0]});
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
	    ns = s.pop().assignLV(opd.getIndex(), 
				  new Temp(s.lv[opd.getIndex()]));
	    q = new MOVE(in, ns.lv[opd.getIndex()], s.stack[0]);
	    break;
	    }
	case Op.GETFIELD:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.pop().push(null).push(new Temp());
	    else // 32-bit value.
		ns = s.pop().push(new Temp());

	    // actual operation:
	    Temp TobjTested = new Temp(s.stack[0]);
	    Quad q0 = new GET(in, ns.stack[0], opd.value(), TobjTested);
	    // null check.
	    r = transNullCheck(s.stack[0], TobjTested, 
			       Tnull, q0, handlers, ts);
	    // setup next state.
	    q = ts.header.next()[ts.which_succ]; 
	    last = q0;
	    break;
	    }
	case Op.GETSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.push(null).push(new Temp());
	    else // 32-bit value.
		ns = s.push(new Temp());
	    q = new GET(in, ns.stack[0], opd.value());
	    break;
	    }
	case Op.IINC:
	    {
		OpLocalVariable opd0 = (OpLocalVariable) in.getOperand(0);
		OpConstant opd1 = (OpConstant) in.getOperand(1);
		Temp constant = new Temp("$const");
		ns = s.assignLV(opd0.getIndex(),
				new Temp(s.lv[opd0.getIndex()]));
		q = new CONST(in, constant, opd1.getValue(), opd1.getType());
		Quad.addEdge(q,
			     new OPER(in, "iadd", ns.lv[opd0.getIndex()],
				      new Temp[] { s.lv[opd0.getIndex()], 
						       constant})
			     );
		last = q.next(0);
		break;
	    }
	case Op.INSTANCEOF:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.pop().push(new Temp());
	    q = new INSTANCEOF(in, ns.stack[0], s.stack[0], opd.value());
	    break;
	    }
	case Op.INVOKEINTERFACE:
	case Op.INVOKESPECIAL:
	case Op.INVOKESTATIC:
	case Op.INVOKEVIRTUAL:
	    {
	    boolean isStatic = (in.getOpcode()==Op.INVOKESTATIC);
	    OpMethod opd = (OpMethod) in.getOperand(0);
	    HClass paramtypes[] = opd.value().getParameterTypes();
	    Temp param[] = new Temp[paramtypes.length];
	    int i,j;
	    for (i=param.length-1, j=0; i>=0; i--, j++) {
		param[i] = s.stack[j];
		if (isLongDouble(paramtypes[i])) j++;
	    }
	    Temp objectref = isStatic?null:s.stack[j];
	    Temp Tex = new Temp();
	    if (opd.value().getReturnType()==HClass.Void) { // no return value.
		ns = s.pop(j+(isStatic?0:1));
		q = new CALL(in, opd.value(), objectref, param, Tex);
	    } else if (!isLongDouble(opd.value().getReturnType())) {
		// 32-bit return value.
		ns = s.pop(j+(isStatic?0:1)).push(new Temp());
		q = new CALL(in, opd.value(), objectref, param, 
			     ns.stack[0], Tex);
	    } else { // 64-bit return value.
		ns = s.pop(j+(isStatic?0:1)).push(null).push(new Temp());
		q = new CALL(in, opd.value(), objectref, param, 
			     ns.stack[0], Tex);
	    }
	    // check for thrown exception.
	    Quad q1 = new OPER(in, "acmpeq", new Temp(),
			       new Temp[] { Tex, Tnull });
	    LAMBDA q2 = new CJMP(in, q1.def()[0], new Temp[] { Tex });
	    stuff(q2);
	    r = transThrow(new TransState(s.push(q2.dst[0][0]), in, q2, 0),
			   handlers, Tnull, false);
	    Quad.addEdge(q,  0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    last = q2; which_succ = 1;
	    // null dereference check.
	    if (!isStatic) {
		HClass HCex = HClass.forClass(NullPointerException.class);
		Temp Tex2 = new Temp();

		// test objectref against null.
		Quad q3 = new OPER(in, "acmpeq", new Temp(),
				   new Temp[] { objectref, Tnull } );
		LAMBDA q4 = new CJMP(in, q3.def()[0], new Temp[] {objectref} );
		stuff(q4);
		Quad q5 = transNewException(HCex, Tex2, Tnull, in, q4, 1);
		TransState[] r2 = 
		    transThrow(new TransState(s.push(Tex2)
					      .rename(objectref, q4.dst[0][1]),
					      in, q5, 0),
			       handlers, Tnull, false);
		// rewrite links.
		((CALL)q).objectref = q4.dst[0][0];
		Quad.addEdge(q3, 0, q4, 0);
		Quad.addEdge(q4, 0, q,  0);
		q = q3; 
		ns = ns.rename(objectref, q4.dst[0][0]);
		r = mergeTS(r, r2);
	    }
	    }
	    break;
	case Op.LCMP: // break this up into lcmpeq, lcmpgt, etc.
	    { // optimization doesn't particularly like this.
	    ns = s.pop(4).push(new Temp());
	    Temp left  = s.stack[2];
	    Temp right = s.stack[0];
	    Quad q0 = new OPER(in, "lcmpeq", new Temp(),
			       new Temp[] { left, right });
	    LAMBDA q1 = new CJMP(in, q0.def()[0], 
				 new Temp[] { left, right });
	    stuff(q1);
	    Quad q2 = new OPER(in, "lcmpgt", new Temp(),
			       new Temp[] { q1.dst[0][0], q1.dst[1][0] });
	    LAMBDA q3 = new CJMP(in, q0.def()[0],
				 new Temp[] { q1.dst[0][0], q1.dst[1][0] });
	    stuff(q3);
	    Quad q4 = new CONST(in, new Temp(), new Integer(-1), HClass.Int);
	    Quad q5 = new CONST(in, new Temp(), new Integer(1), HClass.Int);
	    PHI q6 = new PHI(in,
			     new Temp[] { ns.stack[0], 
					  new Temp(left), new Temp(right)}, 3);
	    // result
	    q6.src[0] = new Temp[] {Tzero, q4.def()[0], q5.def()[0]};
	    // left
	    q6.src[1] = new Temp[] {q1.dst[0][1], q3.dst[0][0], q3.dst[0][1]};
	    // right
	    q6.src[2] = new Temp[] {q1.dst[1][1], q3.dst[1][0], q3.dst[1][1]};
	    // link.
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q1, 1, q6, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q4, 0);
	    Quad.addEdge(q3, 1, q5, 0);
	    Quad.addEdge(q4, 0, q6, 1);
	    Quad.addEdge(q5, 0, q6, 2);
	    // setup next state.
	    q = q0; last = q6;
	    ns=ns.rename(left,q6.dst[1]).rename(right,q6.dst[2]);
	    break;
	    }
	case Op.LDC:
	case Op.LDC_W:
	case Op.LDC2_W:
	    {
	    OpConstant opd = (OpConstant) in.getOperand(0);
	    if (isLongDouble(opd.getType()))
		ns = s.push(null).push(new Temp());
	    else
		ns = s.push(new Temp());
	    q = new CONST(in, ns.stack[0], opd.getValue(), opd.getType());
	    break;
	    }
	case Op.LSHL:
	case Op.LSHR:
	case Op.LUSHR:
	    ns = s.pop(3).push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // lshl
			 ns.stack[0], new Temp[] { s.stack[1], s.stack[0] });
	    break;
	case Op.MONITORENTER:
	case Op.MONITOREXIT:
	    Util.assert(false); // should be caught at higher level.
	case Op.MULTIANEWARRAY:
	    {
		OpClass opd0 = (OpClass) in.getOperand(0);
		OpConstant opd1 = (OpConstant) in.getOperand(1);
		int dims = ((Integer) opd1.getValue()).intValue();
		ns = s.pop(dims).push(new Temp());
		Temp Tdims[] = new Temp[dims];
		for (int i=0; i<dims; i++)
		    Tdims[i] = s.stack[(dims-1)-i];
		// check dimensions.
		HClass HCex=HClass.forClass(NegativeArraySizeException.class);
		Temp Tex = new Temp();
		Quad Qp = new PHI(in, new Temp[0], dims);

		last = ts.header; which_succ = ts.which_succ;
		Temp TdimsTested[] = new Temp[Tdims.length];
		Temp TdimsFailed[] = new Temp[Tdims.length];
		State failState = s.push(Tex);
		for (int i=0; i<dims; i++) {
		    Quad q0 = new OPER(in, "icmpgt", new Temp(),
				       new Temp[] { Tzero, Tdims[i] });
		    LAMBDA q1 = new CJMP(in, q0.def()[0], 
					 new Temp[] { Tdims[i] });
		    // fixup lambda
		    stuff(q1);
		    TdimsTested[i] = q1.dst[0][0];
		    TdimsFailed[i] = q1.dst[0][1];
		    ns = ns.rename(Tdims[i], TdimsTested[i]);
		    failState = failState.rename(Tdims[i], TdimsFailed[i]);
		    // add edges
		    Quad.addEdge(last, which_succ, q0, 0);
		    Quad.addEdge(q0, 0, q1, 0);
		    Quad.addEdge(q1, 1, Qp, i);
		    last=q1; which_succ=0;
		}
		// the exception exit.
		Quad Qn = transNewException(HCex, Tex, Tnull, in, Qp, 0);
		r = transThrow(new TransState(failState, in, Qn, 0),
			       handlers, Tnull, false);

		// the actual operation:
		Quad Qa = new ANEW(in, ns.stack[0], opd0.value(), TdimsTested);
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

		ns = s.pop().push(new Temp());
		
		HClass HCex=HClass.forClass(NegativeArraySizeException.class);
		Temp Tex = new Temp();
		Temp Tindex = s.stack[0];
		Temp TindexTested;

		// ensure that size>=0
		Quad q0 = new OPER(in, "icmpge", new Temp(),
				   new Temp[] { Tindex, Tzero });
		LAMBDA q1 = new CJMP(in, q0.def()[0], new Temp[] {Tindex});
		stuff(q1); TindexTested = q1.dst[0][1];
		Quad q2 = transNewException(HCex, Tex, Tnull, in, q1, 0);
		r = transThrow(new TransState(s.push(Tex)
					      .rename(Tindex, q1.dst[0][0]), 
					      in, q2, 0),
			       handlers, Tnull, false);
		// actual operation:
		Quad q3 = new ANEW(in, ns.stack[0], arraytype, 
				   new Temp[] { TindexTested });
		// link
		Quad.addEdge(q0, 0, q1, 0);
		Quad.addEdge(q1, 1, q3, 0);
		// make next state.
		q = q0; last=q3;
		ns = ns.rename(Tindex, TindexTested);
		break;
	    }
	case Op.NEW:
	    {
	    OpClass opd = (OpClass) in.getOperand(0);
	    ns = s.push(new Temp());
	    q = new NEW(in, ns.stack[0], opd.value());
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
	    Temp Tobj;
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType())) { // 64-bit value.
		ns = s.pop(3);
		Tobj = s.stack[2];
	    }
	    else {
		ns = s.pop(2);
		Tobj = s.stack[1];
	    }
	    Temp TobjTested = new Temp(Tobj);
	    last = new SET(in, opd.value(), TobjTested, s.stack[0]);
	    // null check.
	    r = transNullCheck(Tobj, TobjTested, Tnull, last, 
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
	    q = new SET(in, opd.value(), s.stack[0]);
	    break;
	    }
	case Op.SWAP:
	    ns = s.pop(2).push(s.stack[0]).push(s.stack[1]);
	    q = null;
	    break;

	default:
	    throw new Error("Unknown InGen opcode.");
	}
	if (last == null) last = q;
	// make & return next translation states to hit.
	TransState nr;
	if (q!=null) {
	    // Link new quad if necessary.
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	    nr = new TransState(ns, in.next()[0], last, which_succ);
	} else {
	    nr= new TransState(ns, in.next()[0], 
			       ts.header, ts.which_succ);
	}
	return mergeTS(new TransState[] { nr }, r);
    }
    /** 
     * Translate a single <Code>InMerge</code> using a <code>MergeMap</code>.
     */
    static final TransState[] transInMerge(TransState ts, MergeMap mm) {
	InMerge in = (InMerge) ts.in;
	State s = ts.initialState; // abbreviation.
	TransState[] result = new TransState[0]; // eventual result.

	// First, look up the InMerge in the MergeMap.
	PHI phi = mm.getPhi(in);
	if (phi==null) {
	    // Create new State.
	    State ns = s.rename();
	    // Create list of all local variables and stack slots.
	    Temp nt[] = collateTemps(ns);
	    phi = new PHI(in, nt, in.arity());
	    mm.put(in, phi, 0);
	    // Create new state & keep it around.
	    result = new TransState[] { new TransState(ns, in.next()[0],
						       phi, 0) };
	}
	// Look up the edge to use.
	int which_pred = mm.getPred(in);
	// Set up one argument to phi function.
	Temp nt[] = collateTemps(s);
	Util.assert(phi.dst.length == nt.length);
	for (int i=0; i<phi.dst.length; i++)
	    phi.src[i][which_pred] = nt[i];
	// link
	Quad.addEdge(ts.header, ts.which_succ, phi, which_pred);
	// increment which_pred
	mm.put(in, phi, which_pred+1);
	// done
	return result;
    }
    /** Translate a single <code>InSwitch</code>. */
    static final TransState[] transInSwitch(TransState ts) {
	InSwitch in = (InSwitch) ts.in;
	State s = ts.initialState;
	State ns = s.pop();
	Instr nxt[] = in.next();
	// make keys array.
	int keys[] = new int[nxt.length-1];
	for (int i=0; i<keys.length; i++)
	    keys[i] = in.key(i+1);
	// make & link SWITCH quad.
	SWITCH q = new SWITCH(in, s.stack[0], keys, collateTemps(ns));
	Quad.addEdge(ts.header, ts.which_succ, q, 0);
	// Make next states.
	TransState[] r = new TransState[nxt.length];
	for (int i=0; i<nxt.length-1; i++) {
	    r[i] = new TransState(ns.rename(), nxt[i+1], q, i);
	    q.assign(collateTemps(r[i].initialState), i);
	}
	Util.assert(keys.length == nxt.length-1);
	r[keys.length] = new TransState(ns.rename(), nxt[0], q, keys.length);
	q.assign(collateTemps(r[keys.length].initialState), keys.length);
	return r;
    }
    /** Translate a single <code>InCti</code>. */
    static final TransState[] transInCti(TransState ts, MergeMap handlers,
					 Temp Tzero, Temp Tnull) {
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
	    q = new RETURN(in, s.stack[0]);
	    r = new TransState[0];
	    s.footer.attach(q, 0);
	    break;
	case Op.RETURN:
	    q = new RETURN(in);
	    r = new TransState[0];
	    s.footer.attach(q, 0);
	    break;
	case Op.ATHROW:
	    r = transThrow(ts, handlers, Tnull, true);
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
		q = new OPER(in, "acmpeq", new Temp(), 
			     new Temp[] { s.stack[1], s.stack[0] });
		LAMBDA q2 = new CJMP(in, q.def()[0], collateTemps(ns));
		Quad.addEdge(q, 0, q2, 0);
		int iffalse=0, iftrue=1;
		if (in.getOpcode()==Op.IF_ACMPNE) { // invert things for NE.
		    iffalse=1; iftrue=0;
		}
		r = new TransState[] {
		    new TransState(ns.rename(), in.next()[0], q2, iffalse),
		    new TransState(ns.rename(), in.next()[1], q2, iftrue)
		};
		q2.assign(collateTemps(r[0].initialState), iffalse);
		q2.assign(collateTemps(r[1].initialState), iftrue);
		break;
	    }
	case Op.IFNULL:
	case Op.IFNONNULL:
	    {
		State ns = s.pop();
		Quad q0 = new OPER(in, "acmpeq", new Temp(), 
				   new Temp[] { s.stack[0], Tnull });
		LAMBDA q1 = new CJMP(in, q0.def()[0], collateTemps(ns));
		Quad.addEdge(q0, 0, q1, 0);
		int iffalse=0, iftrue=1;
		if (in.getOpcode()==Op.IFNONNULL) { // invert things
		    iffalse=1; iftrue=0;
		}
		q = q0;
		r = new TransState[] {
		    new TransState(ns.rename(), in.next()[0], q1, iffalse),
		    new TransState(ns.rename(), in.next()[1], q1, iftrue)
		};
		q1.assign(collateTemps(r[0].initialState), iffalse);
		q1.assign(collateTemps(r[1].initialState), iftrue);
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
		    q = new OPER(in, op, new Temp(),
				 new Temp[] { s.stack[0], Tzero });
		} else {
		    ns = s.pop(2);
		    q = new OPER(in, op, new Temp(),
				 new Temp[] { s.stack[1], s.stack[0] } );
		}
		LAMBDA Qc = new CJMP(in, q.def()[0], collateTemps(ns));
		Quad.addEdge(q, 0, Qc, 0);
		r = new TransState[] {
		    new TransState(ns.rename(), in.next()[0], Qc, invert?1:0),
		    new TransState(ns.rename(), in.next()[1], Qc, invert?0:1)
		};
		Qc.assign(collateTemps(r[0].initialState), invert?1:0);
		Qc.assign(collateTemps(r[1].initialState), invert?0:1);
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
    static final TransState[] transThrow(TransState ts, 
					 MergeMap handlers, Temp Tnull,
					 boolean possiblyNull)
    {
	Vector rTS = new Vector();
	ExceptionEntry[] allTries = ts.initialState.allTries;
	State ns = ts.initialState.enterCatch();
	Quad header = ts.header; int which_succ = ts.which_succ;
	Temp Tex = ns.stack[0]; // where the Exception is.
	if (possiblyNull) {
	    HClass hc = HClass.forClass(NullPointerException.class);
	    // Handle 'throw null;':
	    //   Exception Tex = stack.top();
	    //   if (Tex == null) {
	    //     try { Tex = new NullPointerException(); }
	    //     catch (Throwable t) { Tex = t; }
	    //   }
	    Quad q2 = new OPER(ts.in, "acmpeq", new Temp(),
			       new Temp[] { Tex, Tnull } );
	    LAMBDA q3 = new CJMP(ts.in, q2.def()[0], new Temp[] { Tex } );
	    stuff(q3);
	    Quad q4 = new NEW(ts.in, new Temp(), hc);
	    Quad q5 = new CALL(ts.in, hc.getConstructor(new HClass[0]),
			       q4.def()[0], new Temp[0], 
			       new Temp()/*exception*/);
	    Quad q6 = new OPER(ts.in, "acmpeq", new Temp(),
			       new Temp[] { q5.def()[0], Tnull } );
	    LAMBDA q7 = new CJMP(ts.in, q6.def()[0], 
				 new Temp[] { q5.def()[0], q4.def()[0] });
	    stuff(q7);
	    Quad q8 = new PHI(ts.in, 
			      new Temp[] { new Temp(Tex) },
			      new Temp[][] { new Temp[] {q3.dst[0][0], 
							 q7.dst[0][0],
							 q7.dst[1][1] } }, 3);
	    // Link these:
	    Quad.addEdge(header, which_succ, q2, 0);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q8, 0);
	    Quad.addEdge(q3, 1, q4, 0);
	    Quad.addEdge(q4, 0, q5, 0);
	    Quad.addEdge(q5, 0, q6, 0);
	    Quad.addEdge(q6, 0, q7, 0);
	    Quad.addEdge(q7, 0, q8, 1);
	    Quad.addEdge(q7, 1, q8, 2);
	    // Update state.
	    header = q8; which_succ = 0; Tex = q8.def()[0];
	}
	// Now look up through the try blocks for potential handlers.
	int i; for (i=0; i<allTries.length; i++) {
	    if (allTries[i].inTry(ts.in)) {
		Instr hI = allTries[i].handler();
		// expand phi.
		PHI phi = handlers.getPhi(hI);
		if (phi==null) {
		    phi=new PHI(hI, new Temp[] {new Temp()}, 0);
		    // make a new TransState
		    rTS.addElement(new TransState(ns.pop().push(phi.dst[0]), 
						  hI, phi, 0));
		}
		phi.grow(new Temp[] { Tex });
		handlers.put(hI, phi, phi.prev.length);

		if (allTries[i].caughtException()==null) { // catch any.
		    Quad.addEdge(header, which_succ, phi, phi.prev.length-1);
		    break; // no more try processing.
		} else {
		    Quad q1 = new INSTANCEOF(ts.in, new Temp(), Tex,
					     allTries[i].caughtException());
		    LAMBDA q2 = new CJMP(ts.in, q1.def()[0],
					 new Temp[] { Tex });
		    // fixup lambda/phi functions.
		    stuff(q2);
		    phi.src[0][phi.prev.length-1] = q2.dst[0][1];
		    // link quads.
		    Quad.addEdge(header, which_succ, q1, 0);
		    Quad.addEdge(q1, 0, q2, 0);
		    Quad.addEdge(q2, 1, phi, phi.prev.length-1);
		    header = q2; which_succ = 0;
		    Tex = q2.dst[0][0];
		}
	    }
	}
	if (i==allTries.length) { // didn't break early on 'catch any'
	    // exception not caught in any try.  Throw it.
	    Quad q = new THROW(ts.in, Tex);
	    Quad.addEdge(header, which_succ, q, 0);
	    ts.initialState.footer.attach(q, 0);
	}
	// grok rTS into TransState[]
	TransState[] r = new TransState[rTS.size()];
	rTS.copyInto(r);
	return r;
    }
    static final Quad transNewException(HClass exClass, Temp Tex, Temp Tnull,
					Instr in, Quad header, int which_succ)
    {
	Quad q0 = new NEW(in, new Temp(), exClass);
	Quad q1 = new CALL(in, exClass.getConstructor(new HClass[0]),
			   q0.def()[0], new Temp[0], new Temp() /*ex*/);
	// check whether the constructor threw an exception.
	Quad q2 = new OPER(in, "acmpeq", new Temp(),
			   new Temp[] { q1.def()[0], Tnull } );
	LAMBDA q3 = new CJMP(in, q2.def()[0], 
			     new Temp[] { q0.def()[0], q1.def()[0] });
	stuff(q3);
	Quad q4 = new PHI(in, 
			  new Temp[] { Tex },
			  new Temp[][]{new Temp[]{q3.dst[1][0],q3.dst[0][1]}},
			  2);
	Quad.addEdge(header, which_succ, q0, 0);
	Quad.addEdge(q0, 0, q1, 0);
	Quad.addEdge(q1, 0, q2, 0);
	Quad.addEdge(q2, 0, q3, 0);
	Quad.addEdge(q3, 0, q4, 0);
	Quad.addEdge(q3, 1, q4, 1);
	return q4;
    }
    static final TransState[] transNullCheck(Temp Tobj, Temp Ttested,
					     Temp Tnull,
					     Quad q, MergeMap handlers,
					     TransState ts) {
	// actual operation to perform is q.
	HClass HCex = HClass.forClass(NullPointerException.class);
	Temp Tex = new Temp();

	Quad q0 = new OPER(ts.in, "acmpeq", new Temp(),
			   new Temp[] { Tobj, Tnull } );
	LAMBDA q1 = new CJMP(ts.in, q0.def()[0],
			     new Temp[] { Tobj } );
	Quad q2 = transNewException(HCex, Tex, Tnull, ts.in, q1, 1);
	TransState[] r = transThrow(new TransState(ts.initialState.push(Tex), 
						   ts.in, q2, 0),
				    handlers, Tnull, false);
	// fixup lambda function.
	q1.dst[0][0] = Ttested;
	q1.dst[0][1] = new Temp(Tobj);
	// link quads.
	Quad.addEdge(ts.header, ts.which_succ, q0, 0);
	Quad.addEdge(q0, 0, q1, 0);
	Quad.addEdge(q1, 0, q, 0);

	return r;
    }
    static final TransState[] transBoundsCheck(Temp Tobj,  Temp TobjTested,
					       Temp Tindex, Temp TindexTested,
					       Temp Tnull, Temp Tzero,
					       Quad q, MergeMap handlers,
					       TransState ts) {
	// if (obj==null) throw new NullPointerException();
	// if (0<=index && index<obj.length) do(q); /* actual operation */
	// else throw new ArrayIndexOutOfBoundsException();

	HClass HCnull = HClass.forClass(NullPointerException.class);
	HClass HCoob  = HClass.forClass(ArrayIndexOutOfBoundsException.class);
	Temp Tex   = new Temp();

	Quad q0= new OPER(ts.in, "acmpeq", new Temp(),
			  new Temp[] { Tobj, Tnull });
	LAMBDA q1= new CJMP(ts.in, q0.def()[0], new Temp[] {Tobj});
	stuff(q1); q1.dst[0][0] = TobjTested;
	Quad q2 = transNewException(HCnull, new Temp(), Tnull,
				    ts.in, q1, 1);
	// array bounds check.
	Quad q3 = new OPER(ts.in, "icmpge", new Temp(),
			   new Temp[] { Tindex, Tzero });
	LAMBDA q4 = new CJMP(ts.in, q3.def()[0], new Temp[] { Tindex });
	stuff(q4);
	Quad q5 = new ALENGTH(ts.in, new Temp("$len"), TobjTested);
	Quad q6 = new OPER(ts.in, "icmpgt", new Temp(),
			   new Temp[] { q5.def()[0], q4.dst[0][1] });
	LAMBDA q7 = new CJMP(ts.in, q6.def()[0], new Temp[] { q4.dst[0][1] });
	stuff(q7); q7.dst[0][1] = TindexTested;
	Quad q8 = new PHI(ts.in, new Temp[0], 2);
	Quad q9 = transNewException(HCoob, new Temp(), Tnull,
				    ts.in, q8, 0);
	// throw exception if necessary.
	Quad q10= new PHI(ts.in,
			  new Temp[] { Tex },
			  new Temp[][]{new Temp[]{ q2.def()[0],
						   q9.def()[0] }}, 2);
	TransState[] r = transThrow(new TransState(ts.initialState.push(Tex), 
						   ts.in, q10, 0),
				    handlers, Tnull, false);
	// link.
	Quad.addEdge(ts.header, ts.which_succ, q0, 0);
	Quad.addEdge(q0, 0, q1, 0);
	Quad.addEdge(q1, 0, q3, 0);
	Quad.addEdge(q2, 0, q10,0);
	Quad.addEdge(q3, 0, q4, 0);
	Quad.addEdge(q4, 0, q8,0);
	Quad.addEdge(q4, 1, q5, 0);
	Quad.addEdge(q5, 0, q6, 0);
	Quad.addEdge(q6, 0, q7, 0);
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
    /** Count non-null entries in an array. */
    private static final int countNonNull(Object[] oa) {
	int r=0;
	for (int i=0; i<oa.length; i++)
	    if (oa[i]!=null) r++;
	return r;
    }
    /** Combine stack and local variables in single Temp array. */
    private static final Temp[] collateTemps(State s) {
	Temp t[] = new Temp[countNonNull(s.stack)+countNonNull(s.lv)];
	int j=0;
	for (int i=0; i<s.stack.length; i++)
	    if (s.stack[i]!=null)
		t[j++] = s.stack[i];
	for (int i=0; i<s.lv.length; i++)
	    if (s.lv[i]!=null)
		t[j++] = s.lv[i];
	Util.assert(t.length == j);
	return t;
    }
    /** fill the dst slots in a lambda with new Temps based on the src slots */
    private static final void stuff(LAMBDA l) {
	for (int i=0; i < l.src.length; i++)
	    for (int j=0; j < l.dst[i].length; j++)
		l.dst[i][j] = new Temp(l.src[i]);
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
