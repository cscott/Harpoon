// StaticState.java, created Mon Dec 28 00:36:44 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Util;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Stack;
/**
 * <code>StaticState</code> contains the (static) execution context.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StaticState.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
final class StaticState  {
    /** which code representation to use. */
    /*final*/ HCodeFactory hcf;
    StaticState(HCodeFactory hcf) {
	this.hcf = hcf;
	Support.registerNative(this);
    }
    // ----------------------------
    /** mapping of classes to their static fields. */
    final private Hashtable classInfo = new Hashtable();// no unloading.
    private static class ClassHeader {
	FieldValueList fvl=null;
    }
    private FieldValueList get(HClass cls) {
	return ((ClassHeader)classInfo.get(cls)).fvl;
    }
    private void put(HClass cls, FieldValueList fvl) {
	((ClassHeader)classInfo.get(cls)).fvl = fvl;
    }
    // PUBLIC:
    boolean isLoaded(HClass cls) { return classInfo.get(cls)!=null; }
    void load(HClass cls) {
	try {
	    Util.assert(!isLoaded(cls));
	    System.err.println("LOADING "+cls);
	    classInfo.put(cls, new ClassHeader());
	    HField[] fl = cls.getDeclaredFields();
	    for (int i=0; i<fl.length; i++)
		if (fl[i].isStatic())
		    update(fl[i], ObjectRef.defaultValue(fl[i]));
	    // execute static initializer.
	    HMethod hm = cls.getClassInitializer();
	    if (hm!=null) Method.invoke(this, hm, new Object[0]);
	    Util.assert(isLoaded(cls));
	} catch (InterpretedThrowable it) {
	    throw new Error("Could not load "+cls);
	}
    }

    Object get(HField sf) {
	Util.assert(sf.isStatic());
	HClass cls = sf.getDeclaringClass();
	if (!isLoaded(cls)) load(cls);
	return FieldValueList.get(get(cls), sf);
    }
    void update(HField sf, Object value) {
	Util.assert(sf.isStatic());
	HClass cls = sf.getDeclaringClass();
	if (!isLoaded(cls)) load(cls);
	put(cls, FieldValueList.update(get(cls), sf, value));
    }
    //--------------------------------------

    /** Call Stack: */
    final private Stack callStack = new Stack();
    private StackFrame stack(int i) {
	return (StackFrame) callStack.elementAt(callStack.size()-i-1);
    }
    void pushStack(StackFrame sf) { callStack.push(sf); }
    void popStack() { callStack.pop(); }
    void printStackTrace(PrintWriter pw) {
	printStackTrace(pw, stackTrace());
    }
    void printStackTrace(java.io.PrintStream ps) {
	printStackTrace(new PrintWriter(ps, true));
    }
    void printStackTrace() { printStackTrace(System.err); }
	
    String[] stackTrace() {
	String[] st = new String[callStack.size()];
	for (int i=0; i<st.length; i++)
	    st[i] = stack(i).getMethod() + 
		"("+stack(i).getSourceFile()+":"+stack(i).getLineNumber()+")";
	return st;
    }
    static void printStackTrace(PrintWriter pw, String[] st) {
	for (int i=0; i<st.length; i++)
	    pw.println("-   at " + st[i]);
    }
    // -------------------------
    // intern() table for strings.
    final private Hashtable internTable = new Hashtable();
    final ObjectRef intern(ObjectRef src) {
	String s = ref2str(src);
	ObjectRef obj = (ObjectRef) internTable.get(s);
	if (obj==null) { internTable.put(s, src); obj = src; }
	return obj;
    }
    final String ref2str(ObjectRef str) {
	HField HFvalue = Support.HCstring.getField("value");
	HField HFoffset= Support.HCstring.getField("offset");
	HField HFcount = Support.HCstring.getField("count");

	ArrayRef value = (ArrayRef)str.get(HFvalue);
	int offset = ((Integer)str.get(HFoffset)).intValue();
	int count = ((Integer)str.get(HFcount)).intValue();

	char[] ca = new char[count];
	for (int i=0; i<ca.length; i++)
	    ca[i] = ((Character)value.get(i+offset)).charValue();
	return new String(ca);
    }
    final ObjectRef makeString(String s)
	throws InterpretedThrowable {
	ObjectRef obj = new ObjectRef(this, Support.HCstring);
	ArrayRef ca=new ArrayRef(this, Support.HCcharA, new int[]{s.length()});
	for (int i=0; i<s.length(); i++)
	    ca.update(i, new Character(s.charAt(i)));
	HMethod hm =
	    Support.HCstring.getConstructor(new HClass[] { Support.HCcharA });
	Method.invoke(this, hm, new Object[] { obj, ca } );
	return obj;
    }
    final ObjectRef makeStringIntern(String s) 
	throws InterpretedThrowable {
	ObjectRef obj = (ObjectRef) internTable.get(s);
	if (obj!=null) return obj;
	obj = makeString(s);
	internTable.put(s, obj);
	return obj;
    }
    // --------------------------------------------------------
    // NATIVE METHOD SUPPORT:
    private final Hashtable nativeRegistry = new Hashtable();
    private final Hashtable nativeClosure = new Hashtable();
    final void register(NativeMethod nm) {
	nativeRegistry.put(nm.getMethod(), nm);
    }
    final NativeMethod findNative(HMethod hm) {
	return (NativeMethod) nativeRegistry.get(hm);
    }
    final Object getNativeClosure(HMethod hm) {
	return nativeClosure.get(hm.getDeclaringClass());
    }
    final void putNativeClosure(HClass hc, Object cl) {
	nativeClosure.put(hc, cl);
    }
}
