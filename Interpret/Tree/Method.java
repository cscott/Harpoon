package harpoon.Interpret.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXP;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.OPER;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * <code>Method</code> converts <code>HMethods</code> into tree code, 
 * and interprets them. 
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Method.java,v 1.1.2.1 1999-03-27 22:05:09 duncan Exp $
 */
public final class Method extends HCLibrary {

    static final Integer TREE_NULL = new Integer(0);
    
    /** invoke a static main method with no static state. */
    public static final void run(PrintWriter prof, 
				 HCodeFactory hcf,
				 HClass cls, String[] args) {
	HMethod method; OffsetMap map; StaticState ss;
	harpoon.IR.Tree.Code tc;

	method=cls.getMethod("main", new HClass[]{ HCstringA });
	
	Util.assert(method.isStatic());
	Util.assert(hcf.getCodeName().equals("tree"));
	
	tc = (harpoon.IR.Tree.Code)hcf.convert(method);
	map=((Tree)tc.getRootElement()).getFactory().getFrame().getOffsetMap();
	ss = new StaticState(hcf, prof, (InterpreterOffsetMap)map);
	try {
	    HMethod HMinit = HCsystem.getMethod("initializeSystemClass","()V");
	    // set up static state.
	    ss.load(HCsystem);
	    invoke(ss, HMinit, new Object[0]);
	    // encapsulate params properly.
	    ArrayRef params=new ArrayRef(ss,HCstringA,new int[]{args.length});
	    for (int i=0; i<args.length; i++)
		params.update(i, ss.makeString(args[i]));
	    // run main() method.
	    ss.load(cls);
	    	    
	    invoke(ss, method, new Object[] { toNonNativeFormat(params) } );
	} catch (InterpretedThrowable it) {
	    String msg = it.ex.type.getName();
	    try {
		HMethod hm = it.ex.type.getMethod("toString",new HClass[0]);
		ObjectRef obj =(ObjectRef)invoke(ss, hm, new Object[]{it.ex});
		msg = ss.ref2str(obj);
	    } catch (InterpretedThrowable it0) { /* do nothing */ }
	    PrintWriter err = new PrintWriter(System.err, true);
	    err.println("Caught "+msg);
	    //StaticState.printStackTrace(err, it.stackTrace);
	    StaticState.printStackTrace(err, (String[]) it.ex.getClosure());
	} finally {
	    // try to force finalization of object classes
	    System.out.println("Try to force finalization...");
	    ss=null;
	    System.gc();
	    System.runFinalization();
	}
    }

    /** Returns the value obtained by converting <code>obj</code> into 
     *  native format.  The <code>type</code> parameter is the type
     *  of the <code>obj</code> parameter.  
     */
    static final Object toNativeFormat(Object obj, HClass type) { 
	Util.assert(!(obj instanceof ClazPointer));
	Util.assert(!(obj instanceof UndefinedPointer));
	Util.assert(type!=null);

	// Can we have a pointer to a pointer?
	if (obj instanceof ConstPointer) { // correct?
	    ConstPointer cptr = (ConstPointer)obj;
	    return toNativeFormat(cptr.getValue(), cptr.getType());
	}
	else if (obj instanceof FieldPointer) {
	    return (ObjectRef)((FieldPointer)obj).getBase();
	}
	else if (obj instanceof ArrayPointer) {
	    return (ArrayRef)((ArrayPointer)obj).getBase();
	}
	else if (type == HClass.Byte)
	    return new Byte((byte)((Integer)obj).intValue());
	else if (type == HClass.Short)
	    return new Short((short)((Integer)obj).intValue());
	else if (type == HClass.Char)
	    return new Character((char)((Integer)obj).intValue());
	else if (type == HClass.Boolean)
	    return new Boolean(((Integer)obj).intValue()!=0);
	else if ((!type.isPrimitive()) &&
		 (obj instanceof Integer) &&
		 (((Integer)obj).intValue()==0))
	    return null;
	else 
	    return obj;
    }

    /** Returns the value obtained by converting <code>obj</code> into 
     *  non-native format. 
     */
    static final Object toNonNativeFormat(Object obj) { 
	Util.assert(!(obj instanceof UndefinedRef));

	//System.err.println("ToNNF: " + obj.getClass() + ", " + obj);
	if (obj ==null) 
	    return TREE_NULL;
	else if (obj instanceof ObjectRef)
	    return new FieldPointer((ObjectRef)obj, 0);
	else if (obj instanceof ArrayRef) 
	    return new ArrayPointer((ArrayRef)obj, 0);
	else if (obj instanceof Byte ||
		 obj instanceof Short)
	    return new Integer(((Number)obj).intValue());
	else if (obj instanceof Character)
	    return new Integer((int)((Character)obj).charValue());
	else if (obj instanceof Boolean)
	    return new Integer(((Boolean)obj).booleanValue()?1:0);
	else 
	    return obj;
    }

    /** invoke the specified method.  void methods return null. */
    static final Object invoke(StaticState ss, HMethod method, Object[] params)
	throws InterpretedThrowable {
        Util.assert(params.length == numParams(method));
	
	if (!ss.isLoaded(method.getDeclaringClass())) {
	    // Do a dummy push in case an error occurs when loading the class
	    ss.pushStack(new NativeStackFrame(method)); 
	    ss.load(method.getDeclaringClass());
	    ss.popStack();  // don't want this stack frame
	}

	long start_count = ss.getInstructionCount();
	try { 
	    // pop stack, end profiling, etc.
	    // easy to verify that every path through try executes
	    // pushStack *exactly once*.
	  
	    NativeMethod nm = ss.findNative(method);
	    if (nm!=null) {
		ss.pushStack(new NativeStackFrame(method));
		//ss.incrementInstructionCount(); //native methods take 0 time
		Object rval = nm.invoke(ss, params);
		return rval;
	    }
	    // non-native, interpret.
	    TreeCode c = (TreeCode)ss.hcf.convert(method);
	    Frame f = ((Tree)c.getRootElement()).getFactory().getFrame();

	    // failed to translate method into tree code
	    if (c==null) {
		ss.pushStack(new NativeStackFrame(method)); // gonna pop it
		ObjectRef obj = ss.makeThrowable(HCunsatisfiedlinkErr,
						 "No definition for "+method);
		throw new InterpretedThrowable(obj, ss);
	    }

	    // push new stack frame
	    TreeStackFrame sf = new TreeStackFrame((Stm)c.getRootElement());
	    ss.pushStack(sf);

	    for (int i=0; i<params.length; i++) {
		sf.update(f.getGeneralRegisters()[i], 
			  toNonNativeFormat(params[i]));
	    }

	    Interpreter i = new TreeInterpreter(ss, sf);
	    
	    // Run interpreter on the generated tree code
	    while (!i.done) { sf.pc.visit(i); }

	    // We've finished, see if an exception was thrown
	    if (i.Texc!=TREE_NULL) {
		System.err.println("THROWING " +
				   ((ObjectRef)i.Texc).type +
				   " at " + sf.pc.getSourceFile() + ":" +
				   sf.pc.getLineNumber());
		throw new InterpretedThrowable((ObjectRef)i.Texc, ss);
	    }
	    
	    if (method.getReturnType()==HClass.Void) return null;
	    else {
		// Convert to native format, and return
	        return toNativeFormat(i.Tret, method.getReturnType());
	    }
	}
	finally { 
	    // pop stack & profile *always.*
	    //System.out.println("Finished: " + method.getName());
	    ss.popStack();
	    long end_count = ss.getInstructionCount();
	    ss.profile(method, start_count, end_count);
	}
    }

    private static int numParams(HMethod m) {
	return m.getParameterTypes().length + (m.isStatic()?0:1);
    }

    // Interpreter superclass.  Not strictly necessary, but it saved
    // work to reuse as much of Scott's code as possible. 
    static private abstract class Interpreter extends TreeVisitor {
	final StaticState ss;
	final TreeStackFrame sf;
	static Object END = new Object();
	Object Tret = TREE_NULL;
	Object Texc = TREE_NULL;
	boolean done = false;
	Hashtable labels2nodes = new Hashtable();
        private Stack nodesToVisit = new Stack();
        private Derivation derivation;
        private TypeMap typeMap;
	
	Interpreter(StaticState ss, TreeStackFrame sf) {
	    this.ss = ss; this.sf = sf; 
	}

	// map labels to nodes in the TreeCode just once.  
	// recalculating these would get really expensive for large pieces
	// of code
	void mapLabels() {
	    for (Enumeration e = sf.pc.getFactory().getParent().getElementsE();
		 e.hasMoreElements();) {
	        Object next = e.nextElement();
		if (next instanceof SEQ) { 
		    SEQ seq = (SEQ)next;
		    if (seq.left instanceof LABEL) {
			Label l = ((LABEL)seq.left).label;
			labels2nodes.put(l, seq.right);
		    }
		    if (seq.right instanceof LABEL) {
			Label l = ((LABEL)seq.right).label;
			labels2nodes.put(l, END);
		    }
		}
	    }
	}
	
	// advance the pc to the instruction following the specified label
        void advance(Label label) { 
	    // We should always be able to find the label
	    Util.assert(labels2nodes.containsKey(label));

	    ss.incrementInstructionCount();
	    Object stm = labels2nodes.get(label);

	    if (stm==END) done = true;
	    else {
		sf.pc = (Stm)stm;
		nodesToVisit.removeAllElements();
	    }
	}
      
        void advance() {
	    // No more nodes, we're done
	    if (nodesToVisit.isEmpty()) { done = true; }
	    else {
		sf.pc = (Stm)nodesToVisit.pop();
		ss.incrementInstructionCount();
	    }
	}

        void advance(Stm stm) {
	    if (stm==null) { advance(); }
	    else {
		sf.pc = stm;
		ss.incrementInstructionCount();
	    }
	}
  
        void advance(Stm left, Stm right) { 
	    if (left==null) { advance(right); }
	    else if (right==null) { advance(left); }
	    else {
		sf.pc = left;
		nodesToVisit.push(right);
		ss.incrementInstructionCount();
	    }
	}
    }

    // The Tree interpreter
    static private class TreeInterpreter extends Interpreter {
	TreeInterpreter(StaticState ss, TreeStackFrame sf) {
  	    super(ss, sf);
	    mapLabels();
	}

	// Used to avoid recalculating labels when evaluating ESEQs
	private TreeInterpreter(StaticState ss, TreeStackFrame sf, 
				Hashtable labels) {
	    super(ss, sf);
	    labels2nodes = labels;
	}

	public void visit(Tree q) {
	    throw new Error("Hello? No defaults here.");
	}
	
	public void visit(BINOP e) { 
	    e.left.visit(this);
	    e.right.visit(this);
	    
	    Object left  = sf.get(e.left);
	    Object right = sf.get(e.right);

	    // Case 1: neither operand is a Pointer object
	    if ((!(left instanceof Pointer)) &&
		(!(right instanceof Pointer))) {
		sf.update(e, BINOP.evalValue(e.getFactory(),
					     e.op, e.optype, 
					     left, right));


	    }
	    // Case 2: one or both operands is a Pointer object
	    else {
		if (e.op==Bop.CMPEQ) {
		    if (left instanceof Pointer) { 
			sf.update(e, 
				  ((Pointer)left).equals(right)?
				  new Integer(1):
				  new Integer(0));
		    }
		    else {
			sf.update(e, new Integer(0));
		    }
		}
		else if (e.op==Bop.ADD) {
		    Pointer ptr; Object offset;

		    if (left instanceof Pointer) {
			// Both of them cannot be base pointers
			Util.assert(!(right instanceof Pointer));
			ptr = (Pointer)left;
			offset = right;
		    }
		    else { 
			ptr = (Pointer)right;
			offset = left;
		    }
		    
		    if (Type.isDoubleWord(e.getFactory(), Type.POINTER)) 
			sf.update(e, ptr.add(((Long)offset).longValue()));
		    else
			sf.update(e, ptr.add((long)((Integer)offset).intValue()));
		}
		else {
		    throw new Error
			("Illegal opcode for Pointer object: " + e.op);
		}
	    }
	}
	
	public void visit(CJUMP e) {
	    e.test.visit(this);
	    boolean b = (((Integer)sf.get(e.test)).intValue()==1)?true:false;
	    if (b) 
		advance(e.iftrue);
	    else
		advance(e.iffalse);
	}
	
	public void visit(CONST e) { 
	    sf.update(e, e.value);
	}
	
	public void visit(ESEQ e) { 
	    // Create an interpreter to evaluate this eseq
	    TreeInterpreter i = new TreeInterpreter(ss, sf, labels2nodes);

	    sf.pc = e.stm; // set PC to be the stm of this ESEQ

	    while (!i.done) { sf.pc.visit(i); }

	    // Return or throw.
	    if (i.Texc!=TREE_NULL) {
		Texc = i.Texc;
		done = true;
	    }
	    else if (i.Tret!=TREE_NULL) {
		Tret = i.Tret;
		done = true;
	    }
	    else {
		// Now evaluate the expression of the ESEQ
	        e.exp.visit(this);
		sf.update(e, sf.get(e.exp));
	    }
	}

	public void visit(EXP e)  { 
	    // Execute e for side effects
	    e.exp.visit(this);
	    advance();
	}

	/* Let Method.invoke() distinguish between native and
	 * non-native methods */
	public void visit(INVOCATION s) { 
	    if (isAllocation(s)) {
		// Can't resolve ptr type yet
		UndefinedPointer ptr = 
		    new UndefinedPointer(new UndefinedRef(ss), 0);
		sf.update(s.retval, ptr);
		sf.update(s.retex, TREE_NULL);
		sf.update(((TEMP)s.retval).temp, ptr);
		sf.update(((TEMP)s.retex).temp, TREE_NULL);
	    }
	    else {
		// FIX: may want to allow other expressions than TEMPs
		// in future
	        Util.assert((s.retval instanceof TEMP) &&
			    (s.retex  instanceof TEMP));

		// Dereference function ptr
		HMethod method = 
		    (HMethod)(((Pointer)sf.get(s.func)).getValue());
		ExpList  params    = s.args; 
		Object[] oParams;
		HClass[] paramTypes;
		HClass[] pTypesTmp = method.getParameterTypes();
		    
		if (!method.isStatic()) {
		    // Add the extra "this" parameter
		    paramTypes    = new HClass[pTypesTmp.length+1];
		    paramTypes[0] = method.getDeclaringClass();
		    System.arraycopy(pTypesTmp, 0, 
				     paramTypes, 1, pTypesTmp.length);
		}
		else {
		    paramTypes = pTypesTmp;
		}

		oParams = new Object[paramTypes.length];
		for (int i=0; params!=null; i++) {
		    // Convert all parameters to native format, and store
		    // in an array of objects
		    params.head.visit(this);
		    oParams[i] = 
			toNativeFormat(sf.get(params.head), paramTypes[i]);
		    params = params.tail;
		}
		
		try {
		    Object retval = invoke(ss, method, oParams);
		    retval = retval==null?null:toNonNativeFormat(retval);
		    sf.update(s.retval, retval);
		    sf.update(s.retex, null);
		    
		    // Will s.retval always be a TEMP?
		    sf.update(((TEMP)s.retval).temp, retval);
		    sf.update(((TEMP)s.retex).temp, TREE_NULL);
		}
		catch (InterpretedThrowable it) {
		    sf.update(s.retval, null);
		    sf.update(s.retex, it); 

		    // Will s.retval always be a TEMP?
		    sf.update(((TEMP)s.retval).temp, TREE_NULL);
		    sf.update(((TEMP)s.retex).temp, 
			      new FieldPointer(it.ex, 0));
		}
	    }
	    // Advance the PC
	    advance();  
	}

	public void visit(JUMP e) { 
	    Util.assert(e.exp instanceof NAME);
	    advance(((NAME)e.exp).label);
	}
	  
	public void visit(LABEL s) { 
	    /* Nothing to do here, just advance the PC */ 
	    advance();
	}

	public void visit(MEM e) { 
	    e.exp.visit(this);
	    
	    // Can only dereference Pointer types
	    Pointer ptr = (Pointer)sf.get(e.exp);
	    sf.update(e, ptr.getValue());
	}

        public void visit(MOVE q) {
	    q.src.visit(this);
	    Object srcValue = sf.get(q.src);

	    if (q.dst instanceof MEM) { 
	        MEM location = (MEM)q.dst;
		location.exp.visit(this);
		Pointer ptr = (Pointer)sf.get(location.exp);

		try {
		    ptr.updateValue(sf.get(q.src));
		}
		catch (PointerTypeChangedException e) {
		    // The type of ptr has been resolved.  Update the
		    // stack frame accordingly. 
		    ptr = ptr.add(-ptr.getOffset());
		    sf.replace(ptr, e.ptr);
		    sf.replace(ptr.add(-1), e.ptr.add(-1));
		    sf.replace(ptr.add(-2), e.ptr.add(-2));
		    if (e.ptr instanceof ArrayPointer)
			sf.replace(ptr.add(-3), e.ptr.add(-3));
		}
	    }
	    else if (q.dst instanceof TEMP) { 
		TEMP dst = (TEMP)q.dst;
		sf.update(dst.temp, sf.get(q.src));
		sf.update(dst, sf.get(q.src));  // maybe not necessary
	    }
	    else
		throw new Error("Bad type for destination in: " + q);

	    advance();
	}

	public void visit(NAME e) { 
	    if (e.label.toString().startsWith("STRING_CONST")) {
		sf.update(e, new StringPointer(ss, e.label));
	    }
	    else {
		sf.update(e, new ConstPointer(e.label, ss));
	    }
	}

        public void visit(RETURN q) {
	    q.retval.visit(this);
	    Tret = sf.get(q.retval);
	    done = true;
	}

        public void visit(SEQ e) { 
	    advance(e.left, e.right);
	}


        public void visit(TEMP e) {
	    Object tmpValue = sf.get(e.temp);
	    if (tmpValue != null) 
		sf.update(e, tmpValue);
	}
	    
        public void visit(THROW e) { 
	    HClass type;
	    Pointer exc = (Pointer)sf.get(e.retex);
	    
	    // Make getType() abstract method in Pointer?
	    if (exc instanceof FieldPointer) {
		type = ((ObjectRef)exc.getBase()).type;
	    }
	    else if (exc instanceof ConstPointer) {
		type = ((ConstPointer)exc).getType();
	    }
	    else {
		throw new Error("BAD pointer type for throw: " + exc);
	    }

	    e.retex.visit(this);
	    Texc = toNativeFormat(sf.get(e.retex), type);
	    done = true;
	}        
	
	public void visit(UNOP e) { 
	    e.operand.visit(this);

	    Object operand  = sf.get(e.operand);

	    if (operand instanceof Pointer) {
	      if (operand instanceof UndefinedPointer) {
		// dont convert undefinedpointers
		sf.update(e, e);
	      }
	      else {
		sf.update(e, UNOP.evalValue(e.getFactory(), 
					    e.op, e.optype, 
					    ((Pointer)operand).getValue()));
	      }
	      
	    }
	    else {
		sf.update(e, UNOP.evalValue(e.getFactory(),
					    e.op, e.optype, 
					    operand));
	    }
	}
	    
	// Hack to determine if "i" is an allocation.  See 
	// InterpreterAllocationStrategy.
	private static boolean isAllocation(INVOCATION i) {
	    if (i.func instanceof NAME)
		return
		    ((NAME)i.func).label.toString().equals("RUNTIME_MALLOC");
	    else return false; 
	}
    }
}
