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
import harpoon.IR.Tree.Edge;
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
import harpoon.IR.Tree.CanonicalTreeCode;
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
 * @version $Id: Method.java,v 1.1.2.4 1999-07-12 22:57:53 bdemsky Exp $
 */
public final class Method extends HCLibrary {
    static PrintWriter out = new java.io.PrintWriter(System.out);
    static final Integer TREE_NULL = new Integer(0);
    
    /** invoke a static main method with no static state. */
    public static final void run(PrintWriter prof, 
				 HCodeFactory hcf,
				 HClass cls, String[] args) {
	CanonicalTreeCode tc;  // The code of the method to interpret
	HMethod method;        // The method to interpret
	OffsetMap map;         // The offset map used by the tree interpreter
	StaticState ss;        // The interpreter's static state

	method=cls.getMethod("main", new HClass[]{ HCstringA });
	
	Util.assert(method.isStatic());
	Util.assert(hcf.getCodeName().equals("canonical-tree"));
	
	tc = (CanonicalTreeCode)hcf.convert(method);
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
	Util.assert(type!=null);

	Object  retval = null;
	Pointer ptr    = null;

	if (DEBUG) db("TONF: " + obj + ", " + type);

	try { ptr = (Pointer)obj; }
	catch (ClassCastException e) { 
	    // obj is not a pointer type
	    if (type == HClass.Byte)
		retval = new Byte((byte)((Integer)obj).intValue());
	    else if (type == HClass.Short)
		retval = new Short((short)((Integer)obj).intValue());
	    else if (type == HClass.Char)
		retval = new Character((char)((Integer)obj).intValue());
	    else if (type == HClass.Boolean)
		retval = new Boolean(((Integer)obj).intValue()!=0);
 	    else if (!type.isPrimitive())  {
		Util.assert(((Integer)obj).intValue()==0);
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
	Util.assert(!(obj instanceof UndefinedRef));

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
        Util.assert(params.length == numParams(method));

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
	    CanonicalTreeCode c = (CanonicalTreeCode)ss.hcf.convert(method);
	    Frame f = ((Tree)c.getRootElement()).getFactory().getFrame();

	    // failed to translate method into tree code
	    if (c==null) {
		ss.pushStack(new NativeStackFrame(method)); // gonna pop it
		ObjectRef obj = ss.makeThrowable(HCunsatisfiedlinkErr,
						 "No definition for "+method);
		throw new InterpretedThrowable(obj, ss);
	    }
	    if (Method.DEBUG) c.print(out);

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
	        sf.pc = ((SEQ)sf.pc).left;
	}

	void advance(int which_succ) {
	    Edge e = sf.pc.nextEdge(which_succ);
	    sf.pc = (Stm)e.to();
	    ss.incrementInstructionCount();
	}
    }

    // The Tree interpreter
    static private class TreeInterpreter extends Interpreter {
	TreeInterpreter(StaticState ss, TreeStackFrame sf) {
  	    super(ss, sf);
	}

	public void visit(Tree q) {
	    throw new Error("Hello? No defaults here.");
	}
	
	public void visit(BINOP e) { 
	    if (DEBUG) db("Visiting: " + e);

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
		else if (e.op==Bop.CMPGT) { 
		    if (left instanceof Pointer) { 
			Pointer leftPtr = (Pointer)left, rightPtr = (Pointer)right;
			Util.assert(leftPtr.getBase()==rightPtr.getBase());
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
	    e.test.visit(this);
	    boolean b = (((Integer)sf.get(e.test)).intValue()!=0)?true:false;
	    if (b) advance(0);
	    else advance(1);
	}
	
	public void visit(CONST e) { 
	    if (DEBUG) db("Visiting: " + e);
	    sf.update(e, e.value);
	}
	
	public void visit(EXP e)  { 
	    if (DEBUG) db("Visiting: " + e);
	    // Execute e for side effects
	    e.exp.visit(this);
	    advance(0);
	}

	/* Let Method.invoke() distinguish between native and
	 * non-native methods */
	public void visit(INVOCATION s) { 
	    if (DEBUG) db("Visiting: " + s);
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
	    advance(0);  
	}

	public void visit(JUMP e) { 
	    if (DEBUG) db("Visiting: " + e);
	    Util.assert(e.exp instanceof NAME);
	    advance(0);
	}
	  
	public void visit(LABEL s) { 
	    if (DEBUG) db("Visiting LABEL: " + s);
	    /* Nothing to do here, just advance the PC */ 
	    advance(0);
	}

	public void visit(MEM e) { 
	    if (DEBUG) db("Visiting: " + e);
	    e.exp.visit(this);
	    Pointer ptr;

	    try { 
	      if (DEBUG) db("Trying to derefence: " + e.exp);
	      // Can only dereference Pointer types
	      ptr = (Pointer)sf.get(e.exp);
	    }
	    catch (ClassCastException ex) { 
	      if (DEBUG) db("*** EXC should have been thrown: " + ex + 
				 ", "  + e.exp);
	      throw ex;
	    }

	    sf.update(e, ptr.getValue());
	}

        public void visit(MOVE s) {
	    if (DEBUG) db("Visiting: " + s);
	    s.src.visit(this);
	    Object srcValue = sf.get(s.src);

	    if (s.dst instanceof MEM) { 
	        MEM location = (MEM)s.dst;
		location.exp.visit(this);
		Pointer ptr = (Pointer)sf.get(location.exp);

		try {
		    ptr.updateValue(sf.get(s.src));
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
	    else if (s.dst instanceof TEMP) { 
		TEMP dst = (TEMP)s.dst;
		sf.update(dst.temp, sf.get(s.src));
		sf.update(dst, sf.get(s.src));  // maybe not necessary
	    }
	    else
		throw new Error("Bad type for destination in: " + s);

	    advance(0);
	}

	public void visit(NAME e) { 
	    if (DEBUG) db("Visiting: " + e);
	    if (e.label.toString().startsWith("STRING_CONST")) {
		sf.update(e, new StringPointer(ss, e.label));
	    }
	    else {
		sf.update(e, new ConstPointer(e.label, ss));
	    }
	}

        public void visit(RETURN q) {
	    if (DEBUG) db("Visiting: " + q);
	    q.retval.visit(this);
	    Tret = sf.get(q.retval);
	    done = true;
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
	    if (DEBUG) db("Visiting: " + e);
	    e.operand.visit(this);

	    Object operand  = sf.get(e.operand);

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
	    if (i.func instanceof NAME)
		return
		    ((NAME)i.func).label.toString().equals("RUNTIME_MALLOC");
	    else return false; 
	}
    }
}

