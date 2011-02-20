// StaticState.java, created Mon Dec 28 00:36:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
/**
 * <code>StaticState</code> contains the (static) execution context.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StaticState.java,v 1.4 2002-04-10 03:05:50 cananian Exp $
 */
final class StaticState extends HCLibrary implements java.io.Serializable {
    /** which linker to use. */
    /*final*/ Linker linker;
    /** which code representation to use. */
    /*final*/ HCodeFactory hcf;
    StaticState(Linker linker, HCodeFactory hcf) { this(linker, hcf, null); }
    //prof is null for no profiling.
    StaticState(Linker linker, HCodeFactory hcf, PrintWriter prof) {
	super(linker); this.linker = linker;
	this.hcf = hcf; this.prof = prof;
	Support.registerNative(this);
    }
    private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException {
	out.writeObject(this.linker);
	out.defaultWriteObject();
    }
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, ClassNotFoundException {
	this.nativeRegistry = new HashMap();
	this.nativeClosure = new HashMap();
	this.internTable = new HashMap();
	this.linker = (Linker) in.readObject();
	Support.registerNative(this);
	in.defaultReadObject();
    }
    // ----------------------------
    /** mapping of classes to their static fields. */
    final private Map classInfo = new HashMap();// no unloading.
    private static class ClassHeader implements java.io.Serializable {
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
    void load(HClass cls) throws InterpretedThrowable {
	assert !isLoaded(cls);
	HClass sc = cls.getSuperclass();
	if (sc!=null && !isLoaded(sc)) load(sc); // load superclasses first.
	if (TRACE)
	System.err.println("LOADING "+cls);
	classInfo.put(cls, new ClassHeader());
	HField[] fl = cls.getDeclaredFields();
	for (int i=0; i<fl.length; i++)
	    if (fl[i].isStatic())
		update(fl[i], Ref.defaultValue(fl[i]));
	// execute static initializer.
	HMethod hm = cls.getClassInitializer();
	if (hm!=null) Method.invoke(this, hm, new Object[0]);
	assert isLoaded(cls);
    }

    Object get(HField sf) {
	assert sf.isStatic();
	HClass cls = sf.getDeclaringClass();
	if (!isLoaded(cls)) load(cls);
	return FieldValueList.get(get(cls), sf);
    }
    void update(HField sf, Object value) {
	assert sf.isStatic();
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
    HMethod getCaller() { return stack(1).getMethod(); }
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
	    st[i] =
		stack(i).getMethod().getDeclaringClass().getName() + "." +
		stack(i).getMethod().getName() +
		"("+stack(i).getSourceFile()+":"+stack(i).getLineNumber()+")";
	return st;
    }
    static void printStackTrace(PrintWriter pw, String[] st) {
	for (int i=0; i<st.length; i++)
	    pw.println("-   at " + st[i]);
    }
    // -------------------------
    // intern() table for strings.
    private transient Map internTable = new HashMap();
    final ObjectRef intern(ObjectRef src) {
	return makeStringIntern(ref2str(src));
    }
    final String ref2str(ObjectRef str) {
	HField HFvalue = HCstring.getField("value");
	HField HFoffset= HCstring.getField("offset");
	HField HFcount = HCstring.getField("count");

	ArrayRef value = (ArrayRef)str.get(HFvalue);
	int offset = ((Integer)str.get(HFoffset)).intValue();
	int count = ((Integer)str.get(HFcount)).intValue();

	char[] ca = new char[count];
	for (int i=0; i<ca.length; i++)
	    ca[i] = ((Character)value.get(i+offset)).charValue();
	return new String(ca);
    }
    final ObjectRef makeString(String s) {
	return makeString(s, new ObjectRef(this, HCstring));
    }
    private final ObjectRef makeString(String s, ObjectRef obj)
	throws InterpretedThrowable {
	ArrayRef ca=new ArrayRef(this, HCcharA, new int[]{s.length()});
	for (int i=0; i<s.length(); i++)
	    ca.update(i, new Character(s.charAt(i)));
	HMethod hm = HCstring.getConstructor(new HClass[] { HCcharA });
	Method.invoke(this, hm, new Object[] { obj, ca } );
	return obj;
    }
    final ObjectRef makeStringIntern(final String s) 
	throws InterpretedThrowable {
	ObjectRef obj = (ObjectRef) internTable.get(s);
	if (obj!=null) return obj;
	// special subclass of ObjectRef that will intern itself on input.
	obj = makeString(s, new ObjectRef(this, HCstring) {
	    public Object readResolve() {
		if (!internTable.containsKey(s)) internTable.put(s, this);
		return internTable.get(s);
	    }
	});
	internTable.put(s, obj);
	return obj;
    }
    final ObjectRef makeThrowable(HClass HCex) 
	throws InterpretedThrowable {
	ObjectRef obj = new ObjectRef(this, HCex);
	Method.invoke(this, HCex.getConstructor(new HClass[0]),
		      new Object[] { obj } );
	return obj;
    }
    final ObjectRef makeThrowable(HClass HCex, String msg)
	throws InterpretedThrowable {
	ObjectRef obj = new ObjectRef(this, HCex);
	Method.invoke(this, HCex.getConstructor(new HClass[] { HCstring }),
		      new Object[] { obj, makeString(msg) } );
	return obj;
    }
    // --------------------------------------------------------
    // NATIVE METHOD SUPPORT:
    private transient Map nativeRegistry = new HashMap();
    private Map nativeClosure = new HashMap();
    /** provide an implementation for a native method. */
    final void register(NativeMethod nm) {
	assert Modifier.isNative(nm.getMethod().getModifiers()) : "NativeMethod implementation for non-native "+
		    nm.getMethod();
	register0(nm);
    }
    /** override an implementation for a non-native method. */
    final void registerOverride(NativeMethod nm) {
	assert !Modifier.isNative(nm.getMethod().getModifiers()) : "NativeMethod override for genuinely native "+
		    nm.getMethod();
	register0(nm);
    }
    private final void register0(NativeMethod nm) {
	nativeRegistry.put(nm.getMethod(), nm);
    }
    final NativeMethod findNative(HMethod hm) {
	if (hm.getDeclaringClass().isArray() && hm.getName().equals("clone"))
	    hm = HCobject.getMethod("clone", new HClass[0]);
	return (NativeMethod) nativeRegistry.get(hm);
    }
    final Object getNativeClosure(HClass hc) {
	return nativeClosure.get(hc);
    }
    final void putNativeClosure(HClass hc, Object cl) {
	nativeClosure.put(hc, cl);
    }
    // --------------------------------------------------------
    // PROFILING SUPPORT.
    public transient PrintWriter prof;
    private long count; // instruction count.
    final synchronized void incrementInstructionCount() { count++; }
    final synchronized long getInstructionCount() { return count; }
    // profile time spent in a method.
    final synchronized void profile(HMethod method, long start, long end) {
	if (prof==null) return;
	else prof.println("M "+
			  method.getDeclaringClass().getName()+" "+
			  method.getName()+" "+method.getDescriptor()+" "+
			  start+" "+end);
    }
    // profile lifetime of an object instance
    final synchronized void profile(HClass cls, long start, long end,
				    long size) {
	if (prof==null) return;
	else prof.println("N "+cls.getDescriptor()+" "+start+" "+end+" "+size);
    }
    // --------------------------------------------------------
    // DEBUGGING INFORMATION.
    public boolean TRACE = true;
}
