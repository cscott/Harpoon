// Method.java, created Mon Dec 28 01:31:03 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.PrintWriter;
import java.util.Enumeration;
/**
 * <code>Method</code> interprets method code given a static state.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Method.java,v 1.1.2.3 1999-01-03 03:01:44 cananian Exp $
 */
public final class Method extends HCLibrary {

    /** invoke a static main method with no static state. */
    public static final void run(PrintWriter prof, HCodeFactory hcf,
				 HClass cls, String[] args) {
	HMethod method=cls.getMethod("main", new HClass[]{ HCstringA });
	Util.assert(method.isStatic());

	StaticState ss = new StaticState(hcf, prof);
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
	    invoke(ss, method, new Object[] { params } );
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
	    ss=null;
	    System.gc();
	    System.runFinalization();
	}
    }

    /** invoke the specified method.  void methods return null. */
    static final Object invoke(StaticState ss, HMethod method, Object[] params)
	throws InterpretedThrowable {
	Util.assert(params.length == numParams(method));

	if (!ss.isLoaded(method.getDeclaringClass()))
	    ss.load(method.getDeclaringClass());

	long start_count = ss.getInstructionCount();
	try { // pop stack, end profiling, etc.
	    // easy to verify that every path through try executes
	    // pushStack *exactly once*.

	    NativeMethod nm = ss.findNative(method);
	    if (nm!=null) {
		ss.pushStack(new NativeStackFrame(method));
		//ss.incrementInstructionCount(); //native methods take 0 time
		return nm.invoke(ss, params);
	    }
	    // non-native, interpret.
	    HCode c = ss.hcf.convert(method);
	    if (c==null) {
		ss.pushStack(new NativeStackFrame(method)); // gonna pop it
		ObjectRef obj = ss.makeThrowable(HCunsatisfiedlinkErr,
						 "No definition for "+method);
		throw new InterpretedThrowable(obj, ss);
	    }
	    QuadStackFrame sf = new QuadStackFrame((Quad)c.getRootElement());
	    ss.pushStack(sf);

	    Interpreter i;
	    if (c instanceof QuadWithTry)
		i = new ImplicitI(ss, sf, params);
	    else if (c instanceof QuadNoSSA || c instanceof QuadSSA)
		i = new ExplicitI(ss, sf, params);
	    else throw new Error("What kinda code is this!?");
	    
	exec_loop:
	    while (!i.done)
		try {
		    sf.pc.visit(i);
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
			  (q.type()!=HCstring) ? v[i] :
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
		ObjectRef obj = (ObjectRef) params[0];
		hm = obj.type.getMethod(hm.getName(), hm.getDescriptor());
	    }
	    try {
		Object retval = toInternal(invoke(ss, hm, params));
		if (q.retval()!=null)
		    sf.update(q.retval(), retval);
		if (q.retex()!=null)
		    sf.update(q.retex(), null);
	    } catch (InterpretedThrowable it) {
		if (q.retex()==null) throw it; // concession to expediency.
		sf.update(q.retval(), null);
		sf.update(q.retex(), it.ex);
	    }
	    advance(0);
	}
	public void visit(CJMP q) {
	    Boolean b = (Boolean) sf.get(q.test());
	    if (b.booleanValue()) // true branch.
		visit((SIGMA)q, 1);
	    else
		visit((SIGMA)q, 0);
	}
	public void visit(COMPONENTOF q) {
	    ArrayRef arr = (ArrayRef) sf.get(q.arrayref());
	    if (arr.type.getComponentType().isPrimitive()) // must be true.
		sf.update(q.dst(), new Boolean(true));
	    else { // not a primitive array.  perform the test for real.
		ObjectRef obj = (ObjectRef) sf.get(q.objectref());
		if (obj==null ||
		    obj.type.isInstanceOf(arr.type.getComponentType()))
		    sf.update(q.dst(), new Boolean(true));
		else
		    sf.update(q.dst(), new Boolean(false));
	    }
	    advance(0);
	}
	public void visit(CONST q) {
	    if (q.type()!=HCstring)
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
	    if (q.objectref()==null) { // static
		sf.update(q.dst(), toInternal(ss.get(q.field())));
	    } else { // non-static
		ObjectRef obj = (ObjectRef) sf.get(q.objectref());
		sf.update(q.dst(), toInternal(obj.get(q.field())));
	    }
	    advance(0);
	}
	public void visit(HEADER q) {
	    advance(1); // towards METHOD.
	}
	public void visit(INSTANCEOF q) {
	    ObjectRef obj = (ObjectRef) sf.get(q.src());
	    if (obj==null || obj.type.isInstanceOf(q.hclass())) // true.
		sf.update(q.dst(), new Boolean(true));
	    else
		sf.update(q.dst(), new Boolean(false));
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
	    ObjectRef obj = (ObjectRef) sf.get(q.lock());
	    obj.lock();
	    advance(0);
	}
	public void visit(MONITOREXIT q) {
	    ObjectRef obj = (ObjectRef) sf.get(q.lock());
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
	    sf.update(q.dst(), q.evalValue(op));
	    advance(0);
	}
	public void visit(PHI q) {
	    // uses last pred info.
	    for (int i=0; i<q.numPhis(); i++)
		sf.update(q.dst(i), sf.get(q.src(i, last_pred)));
	    advance(0);
	}
	public void visit(RETURN q) {
	    Tret = q.retval();
	    done = true;
	    advance(0); // may as well.
	}
	public void visit(SET q) {
	    Object src = sf.get(q.src());
	    if (q.objectref()==null) { // static
		ss.update(q.field(), toExternal(src, q.field().getType()));
	    } else { // non-static
		ObjectRef obj = (ObjectRef) sf.get(q.objectref());
		obj.update(q.field(), toExternal(src, q.field().getType()) );
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
    }
    // Interpreter with *implicit* exception handling.
    static private class ImplicitI extends ExplicitI {
	ImplicitI(StaticState ss, QuadStackFrame sf, Object[] params) {
	    super(ss, sf, params);
	}
	
	void typeCheck(Temp src, HClass cls) throws InterpretedThrowable {
	    ObjectRef of = (ObjectRef) sf.get(src);
	    if (of==null || of.type.isInstanceOf(cls))
		return; // no problems.
	    ObjectRef obj = ss.makeThrowable(HCclasscastE, of.type.toString());
	    throw new InterpretedThrowable(obj, ss);
	}
	void componentCheck(Temp array, Temp src) throws InterpretedThrowable {
	    ArrayRef af = (ArrayRef) sf.get(array);
	    HClass afCT = af.type.getComponentType();
	    if (afCT.isPrimitive()) return; // statically typed.
	    ObjectRef of = (ObjectRef) sf.get(src);
	    if (of == null || of.type.isInstanceOf(afCT))
		return; // yay, no problemos.
	    ObjectRef obj = ss.makeThrowable(HCarraystoreE,
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
	    ObjectRef obj = ss.makeThrowable(HCarrayindexE, 
					     Integer.toString(index));
	    throw new InterpretedThrowable(obj, ss);
	}
	void nullCheck(Temp t) throws InterpretedThrowable {
	    if (sf.get(t)!=null) return; // all's well.
	    ObjectRef obj = ss.makeThrowable(HCnullpointerE);
	    throw new InterpretedThrowable(obj, ss);
	}
	void minusCheck(Temp t) throws InterpretedThrowable {
	    int i = ((Integer)sf.get(t)).intValue();
	    if (i >= 0) return; // a-ok.
	    ObjectRef obj = ss.makeThrowable(HCnegativearrayE,
					     Integer.toString(i));
	    throw new InterpretedThrowable(obj, ss);
	}
	void zeroCheck(Temp t) throws InterpretedThrowable {
	    long z = ((Number)sf.get(t)).longValue();
	    if (z != 0) return; // a-ok.
	    ObjectRef obj = ss.makeThrowable(HCarithmeticE);
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
			  (q.type()!=HCstring) ? v[i] :
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
	    super.visit(q);
	}
	public void visit(TYPECAST q) {
	    typeCheck(q.objectref(), q.hclass());
	    super.visit(q);
	}
    }
}
