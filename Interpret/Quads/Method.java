// Method.java, created Mon Dec 28 01:31:03 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMember;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.DEBUG;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.LABEL;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.HandlerSet;
import harpoon.IR.Quads.Qop;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.PrintWriter;
import java.util.Enumeration;
/**
 * <code>Method</code> interprets method code in quad form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Method.java,v 1.1.2.23 2001-09-26 16:02:31 cananian Exp $
 */
public final class Method {

    /** Write a start-up static state to disk. */
    public static final void makeStartup(Linker linker, HCodeFactory hcf,
					 java.io.OutputStream os)
	throws java.io.IOException {
	StaticState ss = new StaticState(linker, hcf);
	try {
	    HMethod HMinit =
		ss.HCsystem.getMethod("initializeSystemClass", "()V");
	    // set up static state.
	    ss.load(ss.HCsystem);
	    invoke(ss, HMinit, new Object[0]);
	    System.err.println("Writing.");
	    java.io.ObjectOutputStream oos=new java.io.ObjectOutputStream(os);
	    oos.writeObject(ss);
	    oos.close();
	} catch (InterpretedThrowable it) { prettyPrint(ss, it); }
    }

    /** invoke a static main method, using a static state loaded from disk. */
    public static final void run(PrintWriter prof, HCodeFactory hcf,
				 HClass cls, String[] args,
				 java.io.InputStream is)
	throws java.io.IOException {
	StaticState ss;
	try {
	    System.err.println("Reading.");
	    java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);
	    ss = (StaticState) ois.readObject();
	    ss.prof = prof;
	    ss.hcf = hcf;
	    ois.close();
	    Util.assert(cls.getLinker()==ss.linker,
			"Saved static state uses incompatible linker");
	} catch (ClassNotFoundException e) {
	    throw new java.io.IOException(e.toString());
	} try {
	    run(ss, cls, args);
	} catch (InterpretedThrowable it) { prettyPrint(ss, it); }
	// if profiling, force gc and finalization.
	if (ss.prof!=null) {
	    ss=null; System.gc(); System.runFinalization();
	}
    }

    /** invoke a static main method with no static state. */
    public static final void run(PrintWriter prof, HCodeFactory hcf,
				 HClass cls, String[] args) {
	StaticState ss = new StaticState(cls.getLinker(), hcf, prof);
	try {
	    HMethod HMinit =
		ss.HCsystem.getMethod("initializeSystemClass", "()V");
	    // set up static state.
	    ss.load(ss.HCsystem);
	    invoke(ss, HMinit, new Object[0]);
	    run(ss, cls, args);
	} catch (InterpretedThrowable it) { prettyPrint(ss, it); }
	// if profiling, force gc and finalization.
	if (ss.prof!=null) {
	    ss=null; System.gc(); System.runFinalization();
	}
    }

    private static final void run(StaticState ss, HClass cls, String[] args)
	throws InterpretedThrowable {
	HMethod method=cls.getMethod("main", new HClass[]{ ss.HCstringA });
	Util.assert(method.isStatic());
	// encapsulate params properly.
	ArrayRef params=new ArrayRef(ss,ss.HCstringA,new int[]{args.length});
	for (int i=0; i<args.length; i++)
	    params.update(i, ss.makeString(args[i]));
	// run main() method.
	ss.load(cls);
	invoke(ss, method, new Object[] { params } );
	// if profiling, force gc and finalization.
	if (ss.prof!=null) {
	    System.gc(); System.runFinalization();
	}
    }
    private static void prettyPrint(StaticState ss, InterpretedThrowable it) {
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

    /** invoke the specified method.  void methods return null. */
    static final Object invoke(StaticState ss, HMethod method, Object[] params)
	throws InterpretedThrowable {
	Util.assert(params.length == numParams(method));

	if (!ss.isLoaded(method.getDeclaringClass()))
	    ss.load(method.getDeclaringClass());

	long start_count = ss.getInstructionCount();
	try { // pop stack, end profiling, etc.
	    // easy to verify that every path through try leaves
	    // *exactly one* frame on the stack.
	    ss.pushStack(new NativeStackFrame(method));

	    NativeMethod nm = ss.findNative(method);
	    if (nm!=null) {
		//ss.incrementInstructionCount(); //native methods take 0 time
		return nm.invoke(ss, params);
	    }
	    // non-native, interpret.
	    HCode c = ss.hcf.convert(method);
	    if (c==null) {
		ObjectRef obj = ss.makeThrowable(ss.HCunsatisfiedlinkErr,
						 "No definition for "+method);
		throw new InterpretedThrowable(obj, ss);
	    }
	    QuadStackFrame sf = new QuadStackFrame((Quad)c.getRootElement());
	    ss.popStack(); // get rid of native stack frame
	    ss.pushStack(sf); // and replace with QuadStackFrame.

	    Interpreter i;
	    if (c instanceof QuadWithTry)
		i = new ImplicitI(ss, sf, params);
	    else if (c instanceof QuadNoSSA || c instanceof QuadSSA ||
		     c instanceof QuadRSSx || c instanceof QuadSSI)
		i = new ExplicitI(ss, sf, params);
	    else throw new Error("What kinda code is this!?");
	    
	exec_loop:
	    while (!i.done)
		try {
		    sf.pc.accept(i);
		} catch (InterpretedThrowable it) {
		    // check HANDLERs
		    for (Enumeration e=HandlerSet.elements(sf.pc.handlers());
			 e.hasMoreElements(); ) {
			HANDLER h = (HANDLER) e.nextElement();
			if (h.isCaught(it.ex.type)) {
			    i.advance(h, it.ex);
			    continue exec_loop;
			}
		    }
		    // no handler caught it; rethrow.
		    System.err.println("RETHROWING "+it.ex.type+" at "+
				       sf.pc.getSourceFile() + ":" +
				       sf.pc.getLineNumber());
		    throw it;
		}
	
	    // Return or throw.
	    if (i.Texc!=null) {
		Util.assert(sf.get(i.Texc)!=null, "Undefined throwable");
		System.err.println("THROWING " +
				   ((ObjectRef)sf.get(i.Texc)).type +
				   " at " + sf.pc.getSourceFile() + ":" +
				   sf.pc.getLineNumber());
		throw new InterpretedThrowable((ObjectRef)sf.get(i.Texc), ss);
	    }
	    if (method.getReturnType()==HClass.Void) return null;
	    return Interpreter.toExternal(sf.get(i.Tret),
					  method.getReturnType());
	} finally { // pop stack & profile *always.*
	    ss.popStack();
	    long end_count = ss.getInstructionCount();
	    ss.profile(method, start_count, end_count);
	}
    }
    private static int numParams(HMethod m) {
	return m.getParameterTypes().length + (m.isStatic()?0:1);
    }

    // interpreter superclass.
    static private abstract class Interpreter extends QuadVisitor {
	final StaticState ss;
	final QuadStackFrame sf;
	final Object[] params;
	Temp Tret = null;
	Temp Texc = null;
	boolean done = false;
	Interpreter(StaticState ss, QuadStackFrame sf, Object[] params) { 
	    this.ss = ss; this.sf = sf; this.params = params;
	}
	// advance to a handler
	void advance(HANDLER h, ObjectRef ex) {
	    Edge e = h.nextEdge(0);
	    sf.pc = (Quad) e.to();
	    last_pred = e.which_pred();
	    sf.update(h.exceptionTemp(), ex);
	    System.err.println("HANDLING "+ex.type+" at "+
			       sf.pc.getSourceFile() + ":" +
			       sf.pc.getLineNumber());
	    ss.incrementInstructionCount();
	}
	// advance to a successor
	void advance(int which_succ) {
	    Edge e = sf.pc.nextEdge(which_succ);
	    sf.pc = (Quad) e.to();
	    last_pred = e.which_pred();
	    ss.incrementInstructionCount();
	}
	int last_pred = 0;
	//------------------------------------------
	final static Object toInternal(Object external) {
	    if (external instanceof Byte ||
		external instanceof Short)
		return new Integer(((Number)external).intValue());
	    if (external instanceof Character)
		return new Integer((int)((Character)external).charValue());
	    if (external instanceof Boolean)
		return new Integer(((Boolean)external).booleanValue()?1:0);
	    return external;
	}
	final static Object toExternal(Object internal, HClass type) {
	    if (type == HClass.Byte)
		return new Byte((byte)((Integer)internal).intValue());
	    if (type == HClass.Short)
		return new Short((short)((Integer)internal).intValue());
	    if (type == HClass.Char)
		return new Character((char)((Integer)internal).intValue());
	    if (type == HClass.Boolean)
		return new Boolean(((Integer)internal).intValue()!=0);
	    return internal;
	}
    }
    // Interpreter with explicit exception handling.
    static private class ExplicitI extends Interpreter {
	ExplicitI(StaticState ss, QuadStackFrame sf, Object[] params) {
	    super(ss, sf, params);
	}

	/** Check access permissions on member, accessed from current frame.
	 *  The <code>objtype</code> parameter is <code>null</code> if the
	 *  access is to a static member. */
	void scopeCheck(HMember hm, HClass objtype) 
	    throws InterpretedThrowable {
	    int m = hm.getModifiers();
	    HClass from = sf.getMethod().getDeclaringClass();
	    HClass to = hm.getDeclaringClass();
	    if (Modifier.isProtected(m)) { // protected
		// from JVM ref on invokevirtual: if method is protected, then
		// it must be either a member of the current class or a member
		// of a superclass of the current class, and the class of
		// objectref must be either the current class or a subclass of
		// the current class.
		if (to.isSuperclassOf(from) && 
		    (objtype==null || from.isSuperclassOf(objtype)))
		    return; // all clear.
		// classes in the same package as some superclass can call
		// protected stuff, too. yuck.
		for (HClass sc=to; sc!=null; sc=sc.getSuperclass()) {
		    if (from.getPackage().equals(sc.getPackage()))
			return; // all clear.
		}
	    } else if (Modifier.isPrivate(m)) { // private
		// check for private class called from outside its class.
		if (to.equals(from) &&
		    (objtype==null || from.isSuperclassOf(objtype)))
		    return; // all clear.
	    } else if (Modifier.isPublic(m)) { // public
		return; // always safe.
	    } else { // package scope
		// check for package scope called from outside its package
		if (from.getPackage().equals(to.getPackage()))
		    return; // all clear.
	    }
	    // aha! an illegal access.
	    String msg=((objtype==null)?"":(objtype.getName()+": ")) +
		hm.toString();
	    ObjectRef eor = ss.makeThrowable(ss.HCillegalaccessErr, msg);
	    throw new InterpretedThrowable(eor, ss);
	}

	public void visit(Quad q) {
	    throw new Error("Hello? No defaults here.");
	}

	public void visit(AGET q) {
	    ArrayRef af = (ArrayRef) sf.get(q.objectref());
	    Integer ind = (Integer) sf.get(q.index());
	    sf.update(q.dst(), toInternal(af.get(ind.intValue())));
	    advance(0);
	}
	public void visit(ALENGTH q) {
	    ArrayRef af = (ArrayRef) sf.get(q.objectref());
	    sf.update(q.dst(), new Integer(af.length()));
	    advance(0);
	}
	public void visit(ANEW q) {
	    int[] dims = new int[q.dimsLength()];
	    for (int i=0; i<dims.length; i++)
		dims[i] = ((Integer)sf.get(q.dims(i))).intValue();
	    ArrayRef af = new ArrayRef(ss, q.hclass(), dims);
	    sf.update(q.dst(), af);
	    advance(0);
	}
	public void visit(ARRAYINIT q) {
	    ArrayRef af = (ArrayRef) sf.get(q.objectref());
	    Object[] v = q.value();
	    for (int i=0; i<v.length; i++)
		af.update(q.offset()+i,
			  (q.type()!=ss.HCstring) ? v[i] :
			  ss.makeStringIntern((String)v[i]));
	    advance(0);
	}
	public void visit(ASET q) {
	    ArrayRef af = (ArrayRef) sf.get(q.objectref());
	    Integer ind = (Integer) sf.get(q.index());
	    af.update(ind.intValue(),
		      toExternal(sf.get(q.src()), af.type.getComponentType()));
	    advance(0);
	}
	public void visit(CALL q) {
	    Object[] params = new Object[q.paramsLength()];
	    for (int i=0; i<params.length; i++)
		params[i] = toExternal(sf.get(q.params(i)),q.paramType(i));
	    HMethod hm = q.method();
	    if (!q.isStatic() && q.isVirtual()) { // do virtual dispatch
		Ref obj = (Ref) params[0];
		try {
		    hm = obj.type.getMethod(hm.getName(), hm.getDescriptor());
		} catch (NoSuchMethodError ex) {
		    // duplicate java error message
		    String msg = obj.type.getName()+": method "+
			hm.getName() + hm.getDescriptor() +" not found";
		    ObjectRef eor = ss.makeThrowable(ss.HCnosuchmethodErr,msg);
		    throw new InterpretedThrowable(eor, ss);
		}
		scopeCheck(hm, obj.type); // check virtual access perms
	    } else
		scopeCheck(hm, null); // check static access perms

	    try {
		Object retval = toInternal(invoke(ss, hm, params));
		if (q.retval()!=null) sf.update(q.retval(), retval);
		if (q.retex()!=null && q.retval()!=q.retex())
		    sf.undefine(q.retex()); // debugging support: "define both"
		visit((SIGMA)q, 0); // normal execution along 0 branch
	    } catch (InterpretedThrowable it) {
		if (q.retval()!=null && q.retval()!=q.retex())
		    sf.undefine(q.retval());// debugging support: "define both"
		if (q.retex()!=null)  sf.update(q.retex(), it.ex);
		else throw it; // concession to expediency.
		visit((SIGMA)q, 1); // exeception; proceed along 1 branch
	    }
	}
	public void visit(CJMP q) {
	    Integer b = (Integer) sf.get(q.test());
	    if (b.intValue()!=0) // true branch.
		visit((SIGMA)q, 1);
	    else
		visit((SIGMA)q, 0);
	}
	public void visit(COMPONENTOF q) {
	    ArrayRef arr = (ArrayRef) sf.get(q.arrayref());
	    Util.assert(!arr.type.getComponentType().isPrimitive());
	    Ref obj = (Ref) sf.get(q.objectref());
	    Util.assert(obj!=null);
	    if (obj.type.isInstanceOf(arr.type.getComponentType()))
		sf.update(q.dst(), new Integer(1));
	    else
		sf.update(q.dst(), new Integer(0));
	    advance(0);
	}
	public void visit(CONST q) {
	    if (q.type()!=ss.HCstring)
		sf.update(q.dst(), toInternal(q.value()));
	    else {
		ObjectRef obj=ss.makeStringIntern((String)q.value());
		sf.update(q.dst(), obj);
	    }
	    advance(0);
	}
	public void visit(DEBUG q) {
	    System.err.println(q.str());
	    advance(0);
	}
	public void visit(FOOTER q) {
	    throw new Error("Didn't stop!");
	}
	public void visit(GET q) {
	    HField hf = q.field();
	    if (q.objectref()==null) { // static
		scopeCheck(hf, null); // check static access perms
		sf.update(q.dst(), toInternal(ss.get(hf)));
	    } else { // non-static
		Ref obj = (Ref) sf.get(q.objectref());//arrays have fields too
		scopeCheck(hf, obj.type); // check virtual access perms.
		sf.update(q.dst(), toInternal(obj.get(hf)));
	    }
	    advance(0);
	}
	public void visit(HEADER q) {
	    advance(1); // towards METHOD.
	}
	public void visit(INSTANCEOF q) {
	    Ref obj = (Ref) sf.get(q.src());
	    Util.assert(obj!=null);// (null instanceof ...) not allowed
	    if (obj.type.isInstanceOf(q.hclass())) // true.
		sf.update(q.dst(), new Integer(1));
	    else
		sf.update(q.dst(), new Integer(0));
	    advance(0);
	}
	public void visit(LABEL q) {
	    visit((PHI)q);
	}
	public void visit(HANDLER q) {
	    throw new Error("HANDLERs cannot be directly executed!");
	}
	public void visit(METHOD q) {
	    for (int i=0; i<q.paramsLength(); i++)
		sf.update(q.params(i), toInternal(this.params[i]));
	    advance(0); // towards code, not handlers.
	}
	public void visit(MONITORENTER q) {
	    Ref obj = (Ref) sf.get(q.lock());
	    obj.lock();
	    advance(0);
	}
	public void visit(MONITOREXIT q) {
	    Ref obj = (Ref) sf.get(q.lock());
	    obj.unlock();
	    advance(0);
	}	    
	public void visit(MOVE q) {
	    sf.update(q.dst(), sf.get(q.src()));
	    advance(0);
	}
	public void visit(NEW q) {
	    ObjectRef obj = new ObjectRef(ss, q.hclass());
	    sf.update(q.dst(), obj);
	    advance(0);
	}
	public void visit(NOP q) {
	    advance(0);
	}
	public void visit(OPER q) {
	    Object op[] = new Object[q.operandsLength()];
	    for (int i=0; i<op.length; i++)
		op[i] = sf.get(q.operands(i));
	    sf.update(q.dst(), toInternal(q.evalValue(op)));
	    advance(0);
	}
	public void visit(PHI q) {
	    // uses last pred info.
	    // Careful not to destroy sources until we've read them all!
	    // (dst and sources may conflict)
	    Object[] srcval = new Object[q.numPhis()];
	    for (int i=0; i<q.numPhis(); i++)
		srcval[i] = sf.get(q.src(i, last_pred));
	    for (int i=0; i<q.numPhis(); i++)
		sf.update(q.dst(i), srcval[i]);
	    advance(0);
	}
	public void visit(RETURN q) {
	    Tret = q.retval();
	    done = true;
	    advance(0); // may as well.
	}
	public void visit(SET q) {
	    HField hf = q.field();
	    Object src = sf.get(q.src());
	    if (q.objectref()==null) { // static
		scopeCheck(hf, null); // check static access perms.
		ss.update(hf, toExternal(src, hf.getType()));
	    } else { // non-static
		ObjectRef obj = (ObjectRef) sf.get(q.objectref());
		scopeCheck(hf, obj.type); // check virtual access perms
		obj.update(hf, toExternal(src, hf.getType()) );
	    }
	    advance(0);
	}
	public void visit(SIGMA q, int which_succ) {
	    for (int i=0; i<q.numSigmas(); i++)
		sf.update(q.dst(i, which_succ), sf.get(q.src(i)));
	    advance(which_succ);
	}
	public void visit(SWITCH q) {
	    Integer ind = (Integer) sf.get(q.index());
	    int match;
	    for (match=0; match<q.keysLength(); match++)
		if (ind.intValue() == q.keys(match)) break;
	    visit((SIGMA)q, match);
	}
	public void visit(THROW q) {
	    Texc = q.throwable();
	    done = true;
	    // don't advance: we want to preserve the stack state
	    // for line number info.
	}
	public void visit(TYPECAST q) { // typecast is nop in explicit form.
	    advance(0);
	}
	public void visit(TYPESWITCH q) {
	    Ref ind = (Ref) sf.get(q.index());
	    Util.assert(ind!=null); // null index not allowed.
	    int match;
	    for (match=0; match<q.keysLength(); match++)
		if (ind.type.isInstanceOf(q.keys(match))) break;
	    Util.assert(match < q.arity(),
			"no-default TYPESWITCH has no match");
	    visit((SIGMA)q, match);
	}
    }
    // Interpreter with *implicit* exception handling.
    static private class ImplicitI extends ExplicitI {
	ImplicitI(StaticState ss, QuadStackFrame sf, Object[] params) {
	    super(ss, sf, params);
	}
	
	void typeCheck(Temp src, HClass cls) throws InterpretedThrowable {
	    Ref of = (Ref) sf.get(src);
	    if (of==null || of.type.isInstanceOf(cls))
		return; // no problems.
	    String msg = // of.type.toString(); // for orthodoxy.
		"[is: "+of.type.toString()+"] "+ // for debugging
		"[supposed to be: "+cls.toString()+"]"; // for debugging
	    ObjectRef obj = ss.makeThrowable(ss.HCclasscastE, msg);
	    throw new InterpretedThrowable(obj, ss);
	}
	void componentCheck(Temp array, Temp src) throws InterpretedThrowable {
	    ArrayRef af = (ArrayRef) sf.get(array);
	    HClass afCT = af.type.getComponentType();
	    if (afCT.isPrimitive()) return; // statically typed.
	    Ref of = (Ref) sf.get(src);
	    if (of == null || of.type.isInstanceOf(afCT))
		return; // yay, no problemos.
	    ObjectRef obj = ss.makeThrowable(ss.HCarraystoreE,
					     of.type.toString() + " -> " +
					     af.type.toString());
	    throw new InterpretedThrowable(obj, ss);
	}
	void boundsCheck(Temp array, Temp index) throws InterpretedThrowable {
	    boundsCheck(array, ((Integer)sf.get(index)).intValue());
	}
	void boundsCheck(Temp array, int index) throws InterpretedThrowable {
	    ArrayRef af = (ArrayRef) sf.get(array);
	    if (0 <= index && index < af.length()) return; // a-ok
	    ObjectRef obj = ss.makeThrowable(ss.HCarrayindexE, 
					     Integer.toString(index));
	    throw new InterpretedThrowable(obj, ss);
	}
	void nullCheck(Temp t) throws InterpretedThrowable {
	    if (sf.get(t)!=null) return; // all's well.
	    ObjectRef obj = ss.makeThrowable(ss.HCnullpointerE);
	    throw new InterpretedThrowable(obj, ss);
	}
	void minusCheck(Temp t) throws InterpretedThrowable {
	    int i = ((Integer)sf.get(t)).intValue();
	    if (i >= 0) return; // a-ok.
	    ObjectRef obj = ss.makeThrowable(ss.HCnegativearrayE,
					     Integer.toString(i));
	    throw new InterpretedThrowable(obj, ss);
	}
	void zeroCheck(Temp t) throws InterpretedThrowable {
	    long z = ((Number)sf.get(t)).longValue();
	    if (z != 0) return; // a-ok.
	    ObjectRef obj = ss.makeThrowable(ss.HCarithmeticE);
	    throw new InterpretedThrowable(obj, ss);
	}

	public void visit(AGET q) {
	    nullCheck(q.objectref());
	    boundsCheck(q.objectref(), q.index());
	    super.visit(q);
	}
	public void visit(ALENGTH q) {
	    nullCheck(q.objectref());
	    super.visit(q);
	}
	public void visit(ANEW q) {
	    for (int i=0; i<q.dimsLength(); i++)
		minusCheck(q.dims(i));
	    super.visit(q);
	}
	public void visit(ARRAYINIT q) {
	    nullCheck(q.objectref());
	    ArrayRef af = (ArrayRef) sf.get(q.objectref());
	    Object[] v = q.value();
	    for (int i=0; i<v.length; i++) {
		boundsCheck(q.objectref(), q.offset() + i);
		af.update(q.offset()+i,
			  (q.type()!=ss.HCstring) ? v[i] :
			  ss.makeStringIntern((String)v[i]));
	    }
	    advance(0);
	}
	public void visit(ASET q) {
	    nullCheck(q.objectref());
	    boundsCheck(q.objectref(), q.index());
	    componentCheck(q.objectref(), q.src());
	    super.visit(q);
	}
	public void visit(CALL q) {
	    if (!q.isStatic()) nullCheck(q.params(0));
	    super.visit(q);
	}
	public void visit(COMPONENTOF q) {
	    // object may be null in implicit case.
	    Ref obj = (Ref) sf.get(q.objectref());
	    if (obj==null) {
		sf.update(q.dst(), new Integer(1));
		advance(0);
	    } else super.visit(q);
	}
	public void visit(INSTANCEOF q) {
	    // object may be null in implicit case.
	    Ref obj = (Ref) sf.get(q.src());
	    if (obj==null) {
		sf.update(q.dst(), new Integer(0));
		advance(0);
	    } else super.visit(q);
	}
	public void visit(GET q) {
	    if (!q.isStatic()) nullCheck(q.objectref());
	    super.visit(q);
	}
	public void visit(MONITORENTER q) {
	    nullCheck(q.lock());
	    super.visit(q);
	}
	public void visit(MONITOREXIT q) {
	    nullCheck(q.lock());
	    super.visit(q);
	}
	public void visit(OPER q) {
	    switch (q.opcode()) {
	    case Qop.LDIV: case Qop.LREM: case Qop.IDIV: case Qop.IREM:
		zeroCheck(q.operands(1));
	    default: break;
	    }
	    super.visit(q);
	}
	public void visit(SET q) {
	    if (!q.isStatic()) nullCheck(q.objectref());
	    super.visit(q);
	}
	public void visit(THROW q) {
	    nullCheck(q.throwable());
	    ObjectRef obj = (ObjectRef) sf.get(q.throwable());
	    throw new InterpretedThrowable(obj, ss); // transfer to handler.
	}
	public void visit(TYPECAST q) {
	    typeCheck(q.objectref(), q.hclass());
	    super.visit(q);
	}
	public void visit(TYPESWITCH q) {
	    // object may be null in implicit case
	    Ref obj = (Ref) sf.get(q.index());
	    if (obj==null) {
		Util.assert(q.hasDefault());
		advance(q.keysLength()); // take default branch
	    } else super.visit(q);
	}
    }
}
