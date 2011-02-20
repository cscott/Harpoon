// Method.java, created Sat Mar 27 17:05:09 1999 by duncan
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
//import harpoon.IR.Tree.Edge;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.OPER;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.UNOP;
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
 * @version $Id: Method.java,v 1.4 2002-04-10 03:06:00 cananian Exp $
 */
public final class Method extends Debug {
    static PrintWriter out = new java.io.PrintWriter(System.out);
    static final Integer TREE_NULL = new Integer(0);

    /** invoke a static main method with no static state. */
    public static final void run(PrintWriter prof, 
				 HCodeFactory hcf,
				 HClass cls, String[] args) {
	Code        tc;       // The code of the method to interpret
	HMethod     method;   // The method to interpret
	OffsetMap   map;      // The offset map used by the tree interpreter
	StaticState ss;       // The interpreter's static state

	Linker linker = cls.getLinker();
	method=cls.getMethod("main", new HClass[] { linker.forDescriptor("[Ljava/lang/String;") });
	
	assert method.isStatic();
	assert hcf.getCodeName().equals("canonical-tree") ||
		    hcf.getCodeName().equals("optimized-tree") : "Bad factory codename: " + hcf.getCodeName();
	
	tc = (Code)hcf.convert(method);
	map=((DefaultFrame)((Tree)tc.getRootElement()).getFactory().getFrame()).getOffsetMap();
	ss = new StaticState(linker, hcf, prof, (InterpreterOffsetMap)map);
	try {
	    HMethod HMinit = ss.HCsystem.getMethod("initializeSystemClass","()V");
	    // set up static state.
	    ss.load(ss.HCsystem);
	    invoke(ss, HMinit, new Object[0]);
	    // encapsulate params properly.
	    ArrayRef params=new ArrayRef(ss,ss.HCstringA,new int[]{args.length});
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
	} 
	finally {
	    // try to force finalization of object classes
	    if (DEBUG) db("Try to force finalization...");
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
	Object  retval = null;
	Pointer ptr    = null;

	if (DEBUG) db("TONF: " + obj + ", " + type);

	try { ptr = (Pointer)obj; }
	catch (ClassCastException e) { 
	    // obj is not a pointer type.  In that case, we _must_ have
	    // specified its type through the "type" parameter.
	    assert type!=null;
	    if (type == HClass.Byte)
		retval = new Byte((byte)((Integer)obj).intValue());
	    else if (type == HClass.Short)
		retval = new Short((short)((Integer)obj).intValue());
	    else if (type == HClass.Char)
		retval = new Character((char)((Integer)obj).intValue());
	    else if (type == HClass.Boolean)
		retval = new Boolean(((Integer)obj).intValue()!=0);
 	    else if (!type.isPrimitive())  {
		assert ((Integer)obj).intValue()==0;
		retval = null;
	    }
	    else 
		retval = obj;

 	    return retval;
	}
	    
	// obj must be a Pointer
	switch (ptr.kind()) { 
	case Pointer.ARRAY_PTR:
	    retval = ((ArrayPointer)obj).getBase();  // --> ArrayRef
	    break;
	case Pointer.CONST_PTR:
	    ConstPointer cptr = (ConstPointer)obj; 
	    retval = toNativeFormat(cptr.getValue(), cptr.getType());
	    break;
	case Pointer.FIELD_PTR:
	    retval = ((FieldPointer)obj).getBase();  // --> ObjectRef
	    break;
	default: 
	    // Cannot convert any other type of pointer
	    throw new Error("Can't convert " + ptr.getClass().toString() +
		 	    " to native format");
	}

	if (DEBUG) db("   --> " + retval);
 	return retval;
    } 
   
    /** Returns the value obtained by converting <code>obj</code> into 
     *  non-native format. 
     */
    static final Object toNonNativeFormat(Object obj) { 
	assert !(obj instanceof UndefinedRef);

	Object result;

	if (DEBUG) db("TONNF: " + obj);

	if (obj ==null) 
	    result = TREE_NULL;
	else if (obj instanceof ObjectRef)
	    result = new FieldPointer((ObjectRef)obj, 0);
	else if (obj instanceof ArrayRef) 
	    result = new ArrayPointer((ArrayRef)obj, 0);
	else if (obj instanceof Byte ||
		 obj instanceof Short)
	    result = new Integer(((Number)obj).intValue());
	else if (obj instanceof Character)
	    result = new Integer((int)((Character)obj).charValue());
	else if (obj instanceof Boolean)
	    result = new Integer(((Boolean)obj).booleanValue()?1:0);
	else 
	    result = obj;

	if (DEBUG) db("   ---> " + result);

	return result;
    }

    /** invoke the specified method.  void methods return null. */
    static final Object invoke(StaticState ss, HMethod method, Object[] params)
	throws InterpretedThrowable {
        assert params.length == numParams(method);

	if (DEBUG) db("Invoking method: " + method);

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
		if (DEBUG) db("Returning: " + rval);
		return rval;
	    }
	    // non-native, interpret.
	    Code c = (Code)ss.hcf.convert(method);
	    Frame f = ((Tree)c.getRootElement()).getFactory().getFrame();

	    // failed to translate method into tree code
	    if (c==null) {
		ss.pushStack(new NativeStackFrame(method)); // gonna pop it
		ObjectRef obj = ss.makeThrowable(ss.HCunsatisfiedlinkErr,
						 "No definition for "+method);
		throw new InterpretedThrowable(obj, ss);
	    }
	    if (Method.DEBUG) c.print(out);

	    // push new stack frame
	    TreeStackFrame sf = new TreeStackFrame((Stm)c.getRootElement());
	    ss.pushStack(sf);

	    Interpreter i = new TreeInterpreter(ss, sf, params);
	    
	    // Run interpreter on the generated tree code
	    while (!i.done) { sf.pc.accept(i); }

	    // We've finished, see if an exception was thrown
	    if (i.Texc!=TREE_NULL) {
		System.err.println("THROWING " +
				   ((ObjectRef)i.Texc).type +
				   " at " + sf.pc.getSourceFile() + ":" +
				   sf.pc.getLineNumber());
		throw new InterpretedThrowable((ObjectRef)i.Texc, ss);
	    }
	    
	    if (method.getReturnType()==HClass.Void) {
	        if (DEBUG) db("Returning from VOID func");
		return null;
	    }
	    else {
		if (DEBUG) db("Returning: " + i.Tret);
		// Convert to native format, and return
	        return toNativeFormat(i.Tret, method.getReturnType());
	    }
	}
	finally { 
	    // pop stack & profile *always.*
	    //if (DEBUG) db("Finished: " + method.getName());
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
	Object Tret = TREE_NULL;
	Object Texc = TREE_NULL;
	boolean done = false;
        private Derivation derivation;
        private TypeMap typeMap;

	Interpreter(StaticState ss, TreeStackFrame sf) {
	    this.ss = ss; this.sf = sf; 
	    while (sf.pc instanceof SEQ)
	        sf.pc = ((SEQ)sf.pc).getLeft();
	}

	void advance(int which_succ) {
	    // FIXME:  this needs to use the grapher interface. 
	    //Edge e = sf.pc.nextEdge(which_succ);
	    //sf.pc = (Stm)e.to();
	    ss.incrementInstructionCount();
	}
    }

    // The Tree interpreter
    static private class TreeInterpreter extends Interpreter {
	private Object[] params;
	TreeInterpreter(StaticState ss, TreeStackFrame sf, Object[] params) {
  	    super(ss, sf);
	    this.params = params;
	}

	public void visit(Tree q) {
	    throw new Error("Hello? No defaults here.");
	}
	
	public void visit(BINOP e) { 
	    if (DEBUG) db("Visiting: " + e);

	    e.getLeft().accept(this);
	    e.getRight().accept(this);
	    
	    Object left  = sf.get(e.getLeft());
	    Object right = sf.get(e.getRight());

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
		else if (e.op==Bop.CMPGT) { 
		    if (left instanceof Pointer) { 
			Pointer leftPtr = (Pointer)left, rightPtr = (Pointer)right;
			assert leftPtr.getBase()==rightPtr.getBase();
			sf.update(e, 
				  (leftPtr.getOffset()>rightPtr.getOffset())?
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
			assert !(right instanceof Pointer);
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
		    if (DEBUG) db("*** ILLEGAL BINOP: " + 
				       e + ", " + left + ", " + right + 
				       " IN METHOD " + sf.getMethod());
		    throw new Error
			("Illegal opcode for Pointer object: " + e.op);
		}
	    }
	}
	
	public void visit(CJUMP e) {
	    if (DEBUG) db("Visiting: " + e);
	    e.getTest().accept(this);
	    boolean b = (((Integer)sf.get(e.getTest())).intValue()!=0)?true:false;
	    if (b) advance(0);
	    else advance(1);
	}
	
	public void visit(CONST e) { 
	    if (DEBUG) db("Visiting: " + e);
	    sf.update(e, e.value==null?(Number)new Integer(0):e.value);
	}

	public void visit(DATUM e) { 
	    throw new Error("Should not have encountered a DATUM node");
	}
	
	public void visit(EXPR e)  { 
	    if (DEBUG) db("Visiting: " + e);
	    // Execute e for side effects
	    e.getExp().accept(this);
	    advance(0);
	}

	public void visit(NATIVECALL s) { 
	    assert isAllocation(s);  // Only native call we know about

	    // Can't resolve ptr type yet
	    UndefinedPointer ptr=new UndefinedPointer(new UndefinedRef(ss), 0);
	    sf.update(((TEMP)s.getRetval()).temp, ptr);
	    advance(0);
	}

	/* Let Method.invoke() distinguish between native and
	 * non-native methods */
	public void visit(CALL s) { 
	    if (DEBUG) db("Visiting: " + s);

	    // FIXME: may want to allow other expressions than TEMPs
	    assert s.getRetval().kind()==TreeKind.TEMP;
	    assert s.getRetex().kind()==TreeKind.TEMP;
	    assert s.getHandler().kind()==TreeKind.NAME;

	    s.getFunc().accept(this);

	    // Dereference function ptr
	    HMethod method = 
		(HMethod)(((Pointer)sf.get(s.getFunc())).getValue());
	    ExpList  params    = s.getArgs(); 
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
	    else { paramTypes = pTypesTmp; }

	    oParams = new Object[paramTypes.length];
	    for (int i=0; params!=null; i++) {
		// Convert all parameters to native format, and store
		// in an array of objects
		params.head.accept(this);
		oParams[i] = 
		    toNativeFormat(sf.get(params.head), paramTypes[i]);
		params = params.tail;
	    }
		
	    try {
		Object retval = invoke(ss, method, oParams);
		retval = retval==null?null:toNonNativeFormat(retval);
		sf.update(s.getRetval(), retval);
		sf.update(((TEMP)s.getRetval()).temp, retval);
		advance(0);  // Advance PC to normal branch
	    }
	    catch (InterpretedThrowable it) {
		// ignore handler param
		sf.update(s.getRetex(), new FieldPointer(it.ex, 0));
		sf.update(((TEMP)s.getRetex()).temp, sf.get(s.getRetex()));
		advance(1);  // Advance PC to exceptional branch
	    }
	}

	public void visit(JUMP e) { 
	    if (DEBUG) db("Visiting: " + e);
	    assert e.getExp() instanceof NAME;
	    advance(0);
	}
	  
	public void visit(LABEL s) { 
	    if (DEBUG) db("Visiting LABEL: " + s);
	    /* Nothing to do here, just advance the PC */ 
	    advance(0);
	}

	public void visit(MEM e) { 
	    if (DEBUG) db("Visiting: " + e);
	    e.getExp().accept(this);
	    Pointer ptr;

	    if (DEBUG) db("Trying to derefence: " + e.getExp());
	    // Can only dereference Pointer types
	    ptr = (Pointer)sf.get(e.getExp());

	    sf.update(e, ptr.getValue());
	}

	public void visit(METHOD s) { 
	    if (DEBUG) db("Visiting: " + s);

	    TEMP[] tParams = s.getParams();
	    for (int i=1; i<tParams.length; i++) {
		sf.update(tParams[i].temp, toNonNativeFormat(params[i-1]));
	    }
	    advance(0);
	}


        public void visit(MOVE s) {
	    if (DEBUG) db("Visiting: " + s);
	    s.getSrc().accept(this);
	    Object srcValue = sf.get(s.getSrc());

	    if (s.getDst() instanceof MEM) { 
	        MEM location = (MEM)s.getDst();
		location.getExp().accept(this);
		Pointer ptr = (Pointer)sf.get(location.getExp());

		try {
		    ptr.updateValue(sf.get(s.getSrc()));
		}
		catch (PointerTypeChangedException e) {
		    if (DEBUG) db("PTYPE resolved: " + ptr + " --> " + e.ptr);
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
	    else if (s.getDst() instanceof TEMP) { 
		TEMP dst = (TEMP)s.getDst();
		sf.update(dst.temp, sf.get(s.getSrc()));
		sf.update(dst, sf.get(s.getSrc()));  // maybe not necessary
	    }
	    else
		throw new Error("Bad type for destination in: " + s);

	    advance(0);
	}

	public void visit(NAME e) { 
	    if (DEBUG) db("Visiting: " + e);

	    // Still pretty ugly, but more reliable than before
	    InterpreterOffsetMap map = 
		(InterpreterOffsetMap)
		((DefaultFrame)e.getFactory().getFrame())
		.getOffsetMap();
	    if (map.stringConstantMap().containsValue(e.label)) { 
		sf.update(e, new StringPointer(ss, e.label));
	    }
	    else {
		sf.update(e, new ConstPointer(e.label, ss));
	    }
	}

        public void visit(RETURN q) {
	    if (DEBUG) db("Visiting: " + q);
	    q.getRetval().accept(this);
	    Tret = sf.get(q.getRetval());
	    done = true;
	}

	public void visit(SEGMENT s) { 
	    advance(0);  // Don't use SEGMENT nodes in the interpreter
	}

        public void visit(SEQ e) { 
	    throw new Error("Should not be visiting SEQ nodes");
	}


        public void visit(TEMP e) {
	    if (DEBUG) db("Visiting: " + e);
	    Object tmpValue = sf.get(e.temp);
	    if (tmpValue != null) 
		sf.update(e, tmpValue);
	}
	    
        public void visit(THROW e) { 
	    if (DEBUG) db("Visiting: " + e);
	    HClass type;
	    Pointer exc = (Pointer)sf.get(e.getRetex());
	    
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

	    e.getRetex().accept(this);
	    Texc = toNativeFormat(sf.get(e.getRetex()), type);
	    done = true;
	}        
	
	public void visit(UNOP e) { 
	    if (DEBUG) db("Visiting: " + e);
	    e.getOperand().accept(this);

	    Object operand  = sf.get(e.getOperand());

	    if (operand instanceof Pointer) {
	      if (operand instanceof UndefinedPointer) {
		// dont convert undefinedpointers
		sf.update(e, e);
	      }
	      else {
		System.err.println("WARNING: dangerous pointer op!");
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
	    if (i.getFunc() instanceof NAME)
		return
		    ((NAME)i.getFunc()).label.toString().equals("RUNTIME_MALLOC");
	    else return false; 
	}
    }
}
