// Translate.java, created Sat Aug  8 10:53:03 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.ClassFile.Bytecode.Op;
import harpoon.ClassFile.Bytecode.Operand;
import harpoon.ClassFile.Bytecode.OpClass;
import harpoon.ClassFile.Bytecode.OpConstant;
import harpoon.ClassFile.Bytecode.OpField;
import harpoon.ClassFile.Bytecode.OpLocalVariable;
import harpoon.ClassFile.Bytecode.OpMethod;
import harpoon.ClassFile.Bytecode.Instr;
import harpoon.ClassFile.Bytecode.InGen;
import harpoon.ClassFile.Bytecode.InCti;
import harpoon.ClassFile.Bytecode.InMerge;
import harpoon.ClassFile.Bytecode.InSwitch;
import harpoon.ClassFile.Bytecode.Code.ExceptionEntry;
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
 * @version $Id: Translate.java,v 1.33 1998-09-03 04:19:14 cananian Exp $
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
	/** Current try/catch contexts */
	ExceptionEntry tryBlock[];
	/** Current monitor nesting level */
	int monitorDepth;

	static class BlockContext {
	/** Exit block for nested tries/monitors */
	    Quad exitBlock;
	/** Temp to store exit destination instr index in. */
	    Temp exitTemp;
	/** Continuation TransStates at exit of try/monitor block. */
	    Vector continuation;
	/** Indicates the type of context this is. */
	    int type;
	    static final int TRY = 0;
	    static final int CATCH = 1;
	    static final int MONITOR = 2;
	    static final int JSR = 3;
	/** Constructor */
	    BlockContext(Quad eb, Temp et, Vector c, int ty) {
		exitBlock = eb; exitTemp = et; continuation = c; type = ty;
	    }
	    BlockContext(int ty) {
		this(new NOP(), new Temp(), new Vector(), ty);
	    }
	}
	/** A stack of try/monitor context information. */
	BlockContext nest[];

	/** Constructor. */
	private State(Temp stack[], Temp lv[], ExceptionEntry tryBlock[],
		      int monitorDepth, BlockContext nest[]) {
	    this.stack = stack; this.lv = lv; this.tryBlock = tryBlock;
	    this.monitorDepth = monitorDepth;
	    this.nest = nest;
	    Util.assert(nest.length == monitorDepth + tryBlock.length);
	}
	/** Make new state by popping top of stack */
	State pop() { return pop(1); }
	/** Make new state by popping multiple entries off top of stack */
	State pop(int n) {
	    Temp stk[] = (Temp[]) shrink(this.stack, n);
	    return new State(stk, lv, tryBlock, monitorDepth, nest);
	}
	/** Make new state by pushing temp onto top of stack */
	State push(Temp t) {
	    Temp stk[] = (Temp[]) grow(this.stack);
	    stk[0] = t;
	    return new State(stk, lv, tryBlock, monitorDepth, nest);
	}
	/** Make new state by changing the temp corresponding to an lv. */
	State assignLV(int lv_index, Temp t) {
	    Temp nlv[] = (Temp[]) lv.clone();
	    nlv[lv_index] = t;
	    return new State(stack, nlv, tryBlock, monitorDepth, nest);
	}
	/** Make new state by renaming all the Stack and Local Variable slots.
	 */
	State merge() {
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
	    return new State(stk, nlv, tryBlock, monitorDepth, nest);
	}
	/** Make new state by exiting innermost try/catch context. */
	State exitTry() {
	    ExceptionEntry tb[] = (ExceptionEntry[]) shrink(this.tryBlock);
	    BlockContext ns[] = (BlockContext[]) shrink(this.nest);
	    return new State(stack, lv, tb, monitorDepth, ns);
	}
	/** Make new state by entering a new try/catch context. */
	State enterTry(ExceptionEntry ee) {
	    ExceptionEntry tb[] = (ExceptionEntry[]) grow(this.tryBlock);
	    BlockContext ns[] = (BlockContext[]) grow(this.nest);
	    tb[0] = ee;
	    ns[0] = new BlockContext(BlockContext.TRY);
	    return new State(stack, lv, tb, monitorDepth, ns);
	}
	/** Make new state by clearing the stack and pushing a 
	    <code>Temp</code>. */
	State enterCatch(Temp top) {
	    Temp stk[] = new Temp[] { top };
	    BlockContext ns[] = (BlockContext[]) this.nest.clone();
	    ns[0] = new BlockContext(nest[0].exitBlock,
				     nest[0].exitTemp,
				     (Vector) nest[0].continuation.clone(),
				     BlockContext.CATCH);
	    return new State(stk, lv, tryBlock, monitorDepth, ns);
	}
	/** Make new state by entering a monitor block. */
	State enterMonitor() {
	    BlockContext ns[] = (BlockContext[]) grow(this.nest);
	    ns[0] = new BlockContext(BlockContext.MONITOR);
	    return new State(stack, lv, tryBlock, monitorDepth+1, ns);
	}
	/** Make new state by exiting a monitor block. */
	State exitMonitor() {
	    Util.assert(monitorDepth>0);
	    BlockContext ns[] = (BlockContext[]) shrink(this.nest);
	    return new State(stack, lv, tryBlock, monitorDepth-1, ns);
	}
	/** Make new state, as when entering a JSR/RET block. */
	State enterJSR() {
	    BlockContext ns[] = (BlockContext[]) grow(this.nest);
	    ns[0] = new BlockContext(BlockContext.JSR);
	    return new State(stack, lv, tryBlock, monitorDepth+1, ns);
	}
	/** Make new state, as when exiting a JSR/RET block. */
	State exitJSR() { return exitMonitor(); }

	/** Initialize state with temps corresponding to parameters. */
	State(Temp[] locals) {
	    this(new Temp[0], locals, new ExceptionEntry[0], 0, 
		 new BlockContext[0]);
	}
	/** Creates a new State object identical to this one. */
	public Object clone() {
	    return new State((Temp[]) stack.clone(), 
			     (Temp[]) lv.clone(), 
			     (ExceptionEntry[]) tryBlock.clone(),
			     monitorDepth,
			     (BlockContext[]) nest.clone());
	}
	/** Check to see if a given Instr is in all of the current try blocks.
	 */
	public boolean inAllTry(Instr in) {
	    for (int i=0; i<tryBlock.length; i++)
		if (!tryBlock[i].inTry(in))
		    return false;
	    return true;
	}

	// Utility functions... ///////////////////////////////

	/** Makes a new array by popping first 'n' elements off. */
	private static final Object[] shrink(Object[] src, int n) {
	    Util.assert(src.length>0);
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
	void put(InMerge in, PHI phi, int which_pred) 
	{ phimap.put(in, phi); predmap.put(in, new Integer(which_pred)); }
	PHI getPhi(InMerge in) 
	{ return (PHI) phimap.get(in); }
	int getPred(InMerge in) 
	{ return ((Integer) predmap.get(in)).intValue(); }
    }

    /** Return a <code>Quad</code> representation of the method code in
     *  <code>bytecode</code>. */
    static final Quad trans(harpoon.ClassFile.Bytecode.Code bytecode) {
	// set up initial state.
	String[] paramNames = bytecode.getMethod().getParameterNames();
	Temp[] params = new Temp[paramNames.length];
	for (int i=0; i<params.length; i++)
	    params[i] = new Temp((paramNames[i]==null)?"param":paramNames[i]);

	Temp[] locals = new Temp[bytecode.getMaxLocals()];
	if (!Modifier.isStatic(bytecode.getMethod().getModifiers())) {
	    locals[0] = new Temp("this");
	    System.arraycopy(params, 0, locals, 1, params.length);
	    for (int i=params.length+1; i<locals.length; i++)
		locals[i] = new Temp("lv");
	} else {
	    System.arraycopy(params, 0, locals, 0, params.length);
	    for (int i=params.length; i<locals.length; i++)
		locals[i] = new Temp("lv");
	}

	State s = new State(locals);

	Quad quads = new METHODHEADER(params);

	// translate using state.
	trans(s, bytecode.getTryBlocks(), (Instr) bytecode.getElements()[0], 
	      quads, 0);

	// return result.
	return quads;
    }

    /** Translate a block starting at <code>blockTop</code> using
     *  <code>initialState</code>. */
    static final void trans(State initialState, ExceptionEntry allTries[],
			    Instr blockTop, Quad header, int which_succ) {
	trans(new TransState(initialState, blockTop, 
				    header, which_succ), allTries);
    }
    /** Translate a block starting with a given <code>TransState</code>. */
    static final void trans(TransState ts0, ExceptionEntry allTries[]){
	Stack todo = new Stack(); todo.push(ts0);
	MergeMap mm = new MergeMap();

	while (!todo.empty()) {
	    TransState ts = (TransState) todo.pop();
	    // convenience abbreviations of TransState fields.
	    State s = ts.initialState;

	    // Are we entering a new TRY, MONITOR or JSR/RET block?
	    if ((countTry(ts.in, allTries) > s.tryBlock.length) ||
		(ts.in.getOpcode() == Op.MONITORENTER) ||
		(ts.in.getOpcode() == Op.JSR) ||
		(ts.in.getOpcode() == Op.JSR_W)) {
		boolean isMonitor = (ts.in.getOpcode() == Op.MONITORENTER);
		boolean isJSR = (ts.in.getOpcode() == Op.JSR ||
				 ts.in.getOpcode() == Op.JSR_W);
		Quad q; State ns;
		if (isJSR) { // JSR/RET
		    q = null;
		    ns = s.enterJSR();
		    trans(ns, allTries, ts.in.next()[0], 
			  ts.header, ts.which_succ);
		} else if (isMonitor) { // MONITOR
		    // Make header nodes for block.
		    Quad monitorBlock = new HEADER();
		    // Recursively generate monitor quads.
		    ns = s.enterMonitor().pop();
		    trans(ns, allTries, ts.in, monitorBlock, 0);
		    // Make and link MONITOR
		    q = new MONITOR(ts.in, s.stack[0], monitorBlock);
		} else { // TRY
		    // determine which try block we're entering
		    ExceptionEntry newTry = whichTry(ts.in, s, allTries);
		    Util.assert(newTry != null);
		    // Make header nodes for new TRY.
		    Quad tryBlock = new HEADER();
		    Quad catchBlock = new HEADER();
		    // Recursively generate tryBlock quads.
		    ns = s.enterTry(newTry);
		    trans(ns, allTries, ts.in, tryBlock, 0);
		    // Generate catchBlock
		    ns = ns.enterCatch(new Temp("catch"));
		    trans(ns, allTries, newTry.handler(), catchBlock, 0);
		    // make quad.
		    q = new TRY(ts.in, tryBlock, catchBlock, 
				newTry.caughtException(), ns.stack[0]);
		}
		// make PHI at exit of TRY/MONITOR/JSR
		ns.nest[0].exitBlock = 
		    new PHI(ts.in, new Temp[] { ns.nest[0].exitTemp },
		            ns.nest[0].continuation.size());
		// Link new TRY/MONITOR/JSR quad, if necessary.
		if (q==null)
		    q = ns.nest[0].exitBlock;
		else
		    Quad.addEdge(ts.header, ts.which_succ, q, 0);
		
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
		    Quad.addEdge(c0, 0, ns.nest[0].exitBlock, i);
		    ((PHI)ns.nest[0].exitBlock).src[0][i] = c0.dst;
		    
		    // different test for final val.
		    if (i==ns.nest[0].continuation.size()-1)
			break;

		    // if exitTemp==i then goto continuation[i]
		    Quad q0 = new CONST(tsi.in, new Temp(),
					new Integer(i), HClass.Int);
		    Quad q1 = new OPER(tsi.in, "icmpeq", new Temp(),
				       new Temp[] { ns.nest[0].exitTemp,
						    q0.def()[0] });
		    Quad q2 = new CJMP(tsi.in, q1.def()[0] );
		    Quad.addEdge(q, 0, q0, 0);
		    Quad.addEdge(q0,0, q1, 0);
		    Quad.addEdge(q1,0, q2, 0);
		    if (isJSR)
			ns = tsi.initialState.exitJSR();
		    else if (isMonitor)
			ns = tsi.initialState.exitMonitor();
		    else
			ns = tsi.initialState.exitTry();
		    todo.push(new TransState(ns, tsi.in, q2, 1));
		    q = q2;
		}
		// default branch.
		TransState tsi = 
		    (TransState) ns.nest[0].continuation.elementAt(i);
		if (isJSR)
		    ns = tsi.initialState.exitJSR();
		else if (isMonitor)
		    ns = tsi.initialState.exitMonitor();
		else
		    ns = tsi.initialState.exitTry();
		todo.push(new TransState(ns, tsi.in, q, 0));
		continue;
	    }
	    // Are we exiting a TRY, MONITOR or JSR block?
	    else if ((s.nest.length > 0) && 
		     (((s.nest[0].type == State.BlockContext.TRY) &&
		       (!s.tryBlock[0].inTry(ts.in))) ||
		      ((s.nest[0].type == State.BlockContext.MONITOR) &&
		       (ts.in.getOpcode() == Op.MONITOREXIT)) ||
		      ((s.nest[0].type == State.BlockContext.JSR) &&
		       (ts.in.getOpcode() == Op.RET)))) {
		s.nest[0].continuation.addElement(ts);
		// we'll fix up the dangling end later.
		continue;
	    }
	    // Are we exiting a CATCH block...
	    else if ((s.nest.length > 0) &&
		     (s.nest[0].type == State.BlockContext.CATCH) &&
		     (ts.in instanceof InMerge) &&
		     phiFromTry(s.tryBlock[0], ts.in)) {
		s.nest[0].continuation.addElement(ts);
		// we'll fix up the dangling end later.
		continue;
	    }
	    // None of the above.
	    else {
		TransState nts[] = transInstr(mm, ts);
		for (int i=0; i<nts.length; i++)
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
    static final TransState[] transInstr(MergeMap mm, TransState ts) {
	// Dispatch to correct specific function.
	if (ts.in instanceof InGen)    return transInGen(ts);
	if (ts.in instanceof InSwitch) return transInSwitch(ts);
	if (ts.in instanceof InCti)    return transInCti(ts);
	if (ts.in instanceof InMerge)  return transInMerge(mm, ts);
	throw new Error("Unknown Instr type.");
    }

    /** Translate an <code>InGen</code>. 
     *  @return a <Code>TransState[]</code> of length zero or one. */
    static final TransState[] transInGen(TransState ts) {
	InGen in = (InGen) ts.in;
	State s = ts.initialState;;
	State ns;
	Quad q;
	Quad last = null;

	switch(in.getOpcode()) {
	case Op.AALOAD:
	case Op.BALOAD:
	case Op.CALOAD:
	case Op.FALOAD:
	case Op.IALOAD:
	case Op.SALOAD:
	    ns = s.pop(2).push(new Temp());
	    q = new AGET(in, ns.stack[0], s.stack[1], s.stack[0]);
	    break;
	case Op.AASTORE:
	case Op.BASTORE:
	case Op.CASTORE:
	case Op.FASTORE:
	case Op.IASTORE:
	case Op.SASTORE:
	    ns = s.pop(3);
	    q = new ASET(in, s.stack[2], s.stack[1], s.stack[0]);
	    break;
	case Op.ACONST_NULL:
	    ns = s.push(new Temp("null"));
	    q = new CONST(in, ns.stack[0], null, HClass.Void);
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
		// Alternate implementation:
		//ns = s.push(new Temp());
		//q = new MOVE(in, ns.stack[0], 
		//	    s.lv[opd.getIndex()]);
		break;
	    }
	case Op.ANEWARRAY:
	    {
		OpClass opd = (OpClass) in.getOperand(0);
		HClass hc = HClass.forDescriptor("[" + 
						 opd.value().getDescriptor());
		ns = s.pop().push(new Temp());
		q = new ANEW(in, ns.stack[0], hc, new Temp[] { s.stack[0] });
		break;
	    }
	case Op.ARRAYLENGTH:
	    ns = s.pop().push(new Temp());
	    q = new ALENGTH(in, ns.stack[0], s.stack[0]);
	    break;
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
		ns = s.push(new Temp("const"));
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
		Temp Tnull = new Temp("null");
		Temp Tr0 = new Temp();
		Temp Tr1 = new Temp();
		Temp Tex = new Temp("checkcast");

		HClass HCex = HClass.forClass(ClassCastException.class);

		// make quads
		Quad q0 = new CONST(in, Tnull, null, HClass.Void);
		Quad q1 = new OPER(in, "acmp", Tr0, // equal is true
				   new Temp[] { Tobj, Tnull } ); 
		Quad q2 = new CJMP(in, Tr0);
		Quad q3 = new INSTANCEOF(in, Tr1, Tobj, opd.value());
		Quad q4 = new CJMP(in, Tr1);
		Quad q5 = new NEW(in, Tex, HCex);
		Quad q6 = new CALL(in, HCex.getConstructor(new HClass[0]),
				   Tex, new Temp[0]);
		Quad q7 = new THROW(in, Tex);
		Quad q8 = new PHI(in, new Temp[0], 2);
		// link quads.
		Quad.addEdge(q0, 0, q1, 0);
		Quad.addEdge(q1, 0, q2, 0);
		Quad.addEdge(q2, 0, q3, 0);
		Quad.addEdge(q2, 1, q8, 0);
		Quad.addEdge(q3, 0, q4, 0);
		Quad.addEdge(q4, 0, q5, 0);
		Quad.addEdge(q4, 1, q8, 1);
		Quad.addEdge(q5, 0, q6, 0);
		Quad.addEdge(q6, 0, q7, 0);
		// and setup the next state.
		ns = s;
		q = q0;
		last = q8;
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
	case Op.LDIV:
	case Op.LMUL:
	case Op.LOR:
	case Op.LREM:
	case Op.LSUB:
	case Op.LXOR:
	    ns = s.pop(4).push(null).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // dadd, ddiv or dmul
			 ns.stack[0], new Temp[] { s.stack[2], s.stack[0] });
	    break;
	case Op.DALOAD:
	case Op.LALOAD:
	    ns = s.pop(2).push(null).push(new Temp());
	    q = new AGET(in, ns.stack[0], s.stack[1], s.stack[0]);
	    break;
	case Op.DASTORE:
	case Op.LASTORE:
	    ns = s.pop(4);
	    q = new ASET(in, s.stack[3], s.stack[2], s.stack[0]);
	    break;
	case Op.DCMPG:
	case Op.DCMPL:
	case Op.LCMP:
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
		ns = s.push(null).push(new Temp("const"));
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
	case Op.IDIV:
	case Op.IMUL:
	case Op.IOR:
	case Op.IREM:
	case Op.ISHL:
	case Op.ISHR:
	case Op.ISUB:
	case Op.IUSHR:
	case Op.IXOR:
	    ns = s.pop(2).push(new Temp());
	    q = new OPER(in, Op.toString(in.getOpcode()), // fadd, fdiv, ...
			 ns.stack[0], new Temp[] {s.stack[1], s.stack[0]});
	    break;
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
		ns = s.push(new Temp("const"));
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
	    ns = s.pop(2).push(new Temp());
	    q = new OPER(in, "fneg", ns.stack[0], new Temp[] {s.stack[0]});
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
	case Op.GETSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType()))  // 64-bit value.
		ns = s.pop().push(null).push(new Temp());
	    else // 32-bit value.
		ns = s.pop().push(new Temp());
	    q = new GET(in, ns.stack[0], opd.value(), s.stack[0]);
	    break;
	    }
	case Op.IF_ICMPEQ:
	case Op.IF_ICMPNE:
	case Op.IF_ICMPLT:
	case Op.IF_ICMPGE:
	case Op.IF_ICMPGT:
	case Op.IF_ICMPLE:
	case Op.IFEQ:
	case Op.IFNE:
	case Op.IFLT:
	case Op.IFGE:
	case Op.IFGT:
	case Op.IFLE:
	    throw new Error("Ack!"); // FIXME

	case Op.IINC:
	    {
		OpLocalVariable opd0 = (OpLocalVariable) in.getOperand(0);
		OpConstant opd1 = (OpConstant) in.getOperand(1);
		Temp constant = new Temp("const");
		ns = s.assignLV(opd0.getIndex(),
				new Temp(s.lv[opd0.getIndex()]));
		q = new CONST(in, constant, opd1.getValue(), opd1.getType());
		Quad.addEdge(q,
			     new OPER(in, "iadd", ns.lv[opd0.getIndex()],
				      new Temp[] { s.lv[opd0.getIndex()], 
						       constant})
			     );
		last = q.next[0];
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
	    OpMethod opd = (OpMethod) in.getOperand(0);
	    HClass paramtypes[] = opd.value().getParameterTypes();
	    Temp param[] = new Temp[paramtypes.length];
	    int i,j;
	    for (i=param.length-1, j=0; i>=0; i--, j++) {
		param[i] = s.stack[j];
		if (isLongDouble(paramtypes[i])) j++;
	    }
	    if (opd.value().getReturnType()==HClass.Void) { // no return value.
		ns = s.pop(j+1);
		q = new CALL(in, opd.value(), s.stack[j], param);
	    } else if (!isLongDouble(opd.value().getReturnType())) {
		// 32-bit return value.
		ns = s.pop(j+1).push(new Temp());
		q = new CALL(in, opd.value(), s.stack[j], param, ns.stack[0]);
	    } else { // 64-bit return value.
		ns = s.pop(j+1).push(null).push(new Temp());
		q = new CALL(in, opd.value(), s.stack[j], param, ns.stack[0]);
	    }
	    }
	    break;
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
		q = new ANEW(in, ns.stack[0], opd0.value(), Tdims);
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
		q = new ANEW(in, ns.stack[0], arraytype, 
			     new Temp[] { s.stack[0] });
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
	case Op.PUTSTATIC:
	    {
	    OpField opd = (OpField) in.getOperand(0);
	    if (isLongDouble(opd.value().getType())) { // 64-bit value.
		ns = s.pop(3);
		q = new SET(in, opd.value(), s.stack[2], s.stack[0]);
	    }
	    else {
		ns = s.pop(2);
		q = new SET(in, opd.value(), s.stack[1], s.stack[0]);
	    }
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
	// make & return next translation state to hit.
	if (q!=null) {
	    // Link new quad if necessary.
	    Quad.addEdge(ts.header, ts.which_succ, q, 0);
	    return new TransState[] { 
		new TransState(ns, in.next()[0], last, 0) };
	} else {
	    return new TransState[] { 
		new TransState(ns, in.next()[0], ts.header, ts.which_succ) };
	}
    }
    /** 
     * Translate a single <Code>InMerge</code> using a <code>MergeMap</code>.
     */
    static final TransState[] transInMerge(MergeMap mm, TransState ts) {
	InMerge in = (InMerge) ts.in;
	State s = ts.initialState; // abbreviation.
	TransState[] result = new TransState[0]; // eventual result.

	// First, look up the InMerge in the MergeMap.
	PHI phi = mm.getPhi(in);
	if (phi==null) {
	    // Create new State.
	    State ns = s.merge();
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
	Quad q = new SWITCH(in, s.stack[0], keys);
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
    static final TransState[] transInCti(TransState ts) {
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
	    break;
	case Op.RETURN:
	    q = new RETURN(in);
	    r = new TransState[0];
	    break;
	case Op.ATHROW:
	    q = new THROW(in, s.stack[0]);
	    r = new TransState[0];
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
		Quad q2 = new CJMP(in, q.def()[0]);
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
		q = new CONST(in, new Temp("null"), null, HClass.Void);
		Quad q0 = new OPER(in, "acmpeq", new Temp(), 
				   new Temp[] { s.stack[0], q.def()[0] });
		Quad q1 = new CJMP(in, q0.def()[0]);
		Quad.addEdge(q,  0, q0, 0);
		Quad.addEdge(q0, 0, q1, 0);
		int iffalse=0, iftrue=1;
		if (in.getOpcode()==Op.IFNONNULL) { // invert things
		    iffalse=1; iftrue=0;
		}
		r = new TransState[] {
		    new TransState(ns, in.next()[0], q1, iffalse),
		    new TransState(ns, in.next()[1], q1, iftrue)
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

    // Miscellaneous utility functions. ///////////////////////////

    /** Determine if an HClass needs to be represented by one or two bytecode
     *  stack entries. */
    private static final boolean isLongDouble(HClass hc) {
	if (hc == HClass.Long || hc == HClass.Double)
	    return true;
	return false;
    }
    /** Determine if some entry to a MERGE node is from an Instr inside
     *  a given try. */
    private static final boolean phiFromTry(ExceptionEntry theTry,
					    Instr theMerge) {
	Instr pr[] = theMerge.prev();
	for (int i=0; i<pr.length; i++)
	    if (theTry.inTry(pr[i]))
		return true;
	return false;
    }
    /** Count the number of try blocks containing the given Instr. */
    private static final int countTry(Instr in, ExceptionEntry[] tb) {
	int n=0;
	for (int i=0; i<tb.length; i++)
	    if (tb[i].inTry(in))
		n++;
	return n;
    }
    /** Find a try block containing the given Instr which is not already
     *  present in the State's tryBlock list. */
    private static final ExceptionEntry whichTry(Instr in, State s,
						 ExceptionEntry[] allTries)
    {
	// determine which try block we're entering
	ExceptionEntry newTry = null;
	for (int i=0; i<allTries.length; i++) {
	    if (allTries[i].inTry(in)) {
		int j;
		for (j=0; j<s.tryBlock.length; j++)
		    if (s.tryBlock[j]==allTries[i])
			break;
		if (j==s.tryBlock.length) { // try not in current state.
		    newTry = allTries[i];
		    break;
		}
	    }
	}
	return newTry; // null if can't find.
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
}
