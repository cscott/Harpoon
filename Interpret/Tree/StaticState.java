// StaticState.java, created Mon Dec 28 00:36:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Temp.Label;
import harpoon.Util.Tuple;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Stack;

/**
 * <code>StaticState</code> contains the (static) execution context.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StaticState.java,v 1.3.2.2 2002-03-14 01:58:08 cananian Exp $
 */
final class StaticState extends HCLibrary {
    
    /** mapping of classes to their static fields. */
    final private Hashtable classInfo = new Hashtable();// no unloading.
    final private Hashtable nonclassInfo = new Hashtable();   

    /** which linker to use. */
    Linker linker;
    /** which code representation to use. */
    HCodeFactory hcf;
    /** used to map fields & methods to labels */
    final InterpreterOffsetMap map; 
    
    // Class constructor 
    StaticState(Linker linker, HCodeFactory hcf, InterpreterOffsetMap map) { 
	this(linker, hcf, null, map);     //prof is null for no profiling.
    }

    StaticState(Linker linker, HCodeFactory hcf, PrintWriter prof,
		InterpreterOffsetMap map) {
	super(linker); this.linker = linker;
	// Only translate trees in canonical form 
	this.hcf = hcf; this.prof = prof; this.map = map;
	Support.registerNative(this);
    }

    private static class ClassHeader {
	FieldValueList fvl=null;
    }

    private void put(HClass cls, FieldValueList fvl) {
	((ClassHeader)classInfo.get(cls)).fvl = fvl;
    }


    /************************************************************
     *                                                          *
     *                     CLASS LOADING                        *
     *                                                          *
     ***********************************************************/

    boolean isLoaded(HClass cls) { return classInfo.get(cls)!=null; }

    void load(HClass cls) throws InterpretedThrowable {
	assert !isLoaded(cls);
	HClass sc = cls.getSuperclass();
	if (sc!=null && !isLoaded(sc)) load(sc); // load superclasses first.
	System.err.println("LOADING "+cls);

	// Map [HClass --> FieldValueList]
	//
	classInfo.put(cls, new ClassHeader());
	
	// Map [class pointer --> HClass]
	// 
	Label clsLabel = map.label(cls);
	map(clsLabel, cls);
	
	loadStaticFields(clsLabel, cls);
	loadNonStaticFields(clsLabel, cls);
	loadMethods(clsLabel, cls);
	if (!(cls.isInterface() || cls.isPrimitive())) 
	    loadDisplay(clsLabel, cls, map.displaySize());
	else  
	    loadDisplay(clsLabel, cls);
	
	if (!(cls.isPrimitive() || cls.isArray())) 
	    loadInterfaces(clsLabel, cls);
	
	// execute <clinit>() 
	HMethod hm = cls.getClassInitializer();
	if (hm!=null) Method.invoke(this, hm, new Object[0]);
	assert isLoaded(cls);
    }

    private void loadDisplay(Label clsLabel, HClass current, int size) { 
	for (int i=0; i<size; i++) { 
	    map(new ClazPointer(clsLabel, this, i), ConstPointer.NULL_POINTER);
	}
	loadDisplay(clsLabel, current);
    }

    private void loadDisplay(Label clsLabel, HClass current) {
	HClass sc;

	sc = getSuperclass(current);
	if (sc!=null) loadDisplay(clsLabel, sc);

	map(new ClazPointer(clsLabel, this, map.offset(current)),
	    new ConstPointer(map.label(current), this));

    }
    
    private HClass getSuperclass(HClass cls) { 
	if (cls.isArray()) { 
	    HClass obj  = linker.forName("java.lang.Object");
	    int    dims = HClassUtil.dims(cls);
	    HClass base = HClassUtil.baseClass(cls);
	    if (base.isPrimitive()) 
		return obj;
	    else if (base.getDescriptor().equals(obj.getDescriptor()))
		return HClassUtil.arrayClass(linker, obj, dims-1);
	    else 
		return HClassUtil.arrayClass(linker, base.getSuperclass(), dims);
	}
	else { 
	    return cls.getSuperclass();
	}
    }

    private void loadInterfaces(Label clsLabel, HClass cls) {
	// Make interface list
	HClass[] interfaces = cls.getInterfaces();
	InterfaceList iList = new InterfaceList(interfaces.length+1);
	for (int i=0; i<interfaces.length; i++) {
	    iList.addInterface
		(new ConstPointer(map.label(interfaces[i]), this), i);
	    HMethod[] hm = interfaces[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
		map(new ClazPointer(clsLabel, this, map.offset(hm[j])), 
		    hm[j]);
	    }
	}

	// NULL-terminate the interface list
	iList.addInterface(ConstPointer.NULL_POINTER, interfaces.length);
	
	//
	map(new ClazPointer(clsLabel, this, map.interfaceListOffset(cls)),
	    new InterfaceListPointer(iList, 0));
    }

 
    private void loadMethods(Label clsLabel, HClass current) {
	HClass sc = current.getSuperclass();

	if (sc!=null) loadMethods(clsLabel, sc);

	HMethod[] mt = current.getDeclaredMethods();
	for (int i=0; i<mt.length; i++) {
	    // Attempt to map a label directly to the method
	    // (even for non-static methods)
	    map(map.label(mt[i]), mt[i]);
	    
	    if (!mt[i].isStatic()) {
		map(new ClazPointer(clsLabel, this, map.offset(mt[i])), 
		    mt[i]);
	    }
	}
    }

    private void loadStaticFields(Label clsLabel, HClass current) {
	HField[] fl = current.getDeclaredFields();
	for (int i=0; i<fl.length; i++) {		    
	    if (fl[i].isStatic()) {
		map(map.label(fl[i]), fl[i]);
		update(fl[i], Ref.defaultValue(fl[i]));	
	    }
	}
    }

    private void loadNonStaticFields(Label clsLabel, HClass current) {
	HClass sc = current.getSuperclass();

	if (sc!=null) loadNonStaticFields(clsLabel, sc);
	
	HField[] fl = current.getDeclaredFields();
	for (int i=0; i<fl.length; i++) {
	    if (!fl[i].isStatic()) {
		map(current, map.offset(fl[i]), fl[i]);
	    }
	}
    }

    /************************************************************
     *                                                          *
     *             STATIC DATA ACCESS METHODS                   *
     * (Methods to access an _interpreted_ class's static data) *
     *                                                          *
     ***********************************************************/


    HField getField(ConstPointer ptr) {
	return (HField)classInfo.get(ptr.getBase());
    }
	
    /** Returns the non-static field pointed to by ptr */
    HField getField(FieldPointer ptr) {
	HField result = null;
	HClass type = ((ObjectRef)ptr.getBase()).type; 
	Long offset = new Long(ptr.getOffset());

	for (; type!=null; type = type.getSuperclass()) {
	    result = (HField)classInfo.get
		(new Tuple(new Object[] { type, offset }));
	    if (result != null) return result;
	}

	throw new Error("Couldn't find field: " + ptr);
    }

    /** Returns an <code>HClass</code> representing the type pointed
     *  to by label */
    HClass getHClass(Label label) { 
	if (classInfo.containsKey(label)) {  // the class has been loaded
	    return (HClass)classInfo.get(label);
	}
	else { // the class has not been loaded.  
	    HClass hc = (HClass)map.decodeLabel(label); 
	    load(hc); 
	    assert classInfo.containsKey(label) : label.toString();
	    return (HClass)classInfo.get(label);
	}
    }

    /** Returns the data pointed to by ptr */
    Object getValue(ClazPointer ptr) { 
	assert classInfo.containsKey(ptr) : ptr.toString();
	return classInfo.get(ptr); 
    }
    
    /** Returns the HMethod with the specified label */
    Object getValue(ConstPointer ptr) { 
	// Do null-pointer check
	if (ptr==ConstPointer.NULL_POINTER) { 
	    return new Integer(0);
	}
	else { 
	    if (!classInfo.containsKey(ptr.getBase())) {
		classInfo.put(ptr.getBase(), 
			      map.decodeLabel((Label)ptr.getBase()));
	    }
	    
	    assert classInfo.containsKey(ptr.getBase());
	    if (classInfo.get(ptr.getBase()) instanceof HField) {
		return get((HField)classInfo.get(ptr.getBase())); 
	    }
	    else {
		return classInfo.get(ptr.getBase());
	    }
	}
    }

    /** Updates the static field which label points at */
    void updateFieldValue(ConstPointer ptr, Object value) {
	update((HField)classInfo.get(ptr.getBase()), value);
    }

    /************************************************************
     *                                                          *
     *      UTILITY METHODS TO AID IN STATIC DATA ACCESS        *
     *                                                          *
     ***********************************************************/

    // Returns the value of the static field "sf"
    // Throws an error if "sf" is not static
    Object get(HField sf) {
	assert sf.isStatic();
	HClass cls = sf.getDeclaringClass();
	if (!isLoaded(cls)) load(cls);
	return FieldValueList.get(get(cls), sf);
    }

    // Returns the FieldValueList associated with "cls"
    private FieldValueList get(HClass cls) {
	assert isLoaded(cls);
	return ((ClassHeader)classInfo.get(cls)).fvl;
    }

    // Updates the value of the static field "sf" to be "value".
    // Throws an error if "sf" is not static
    void update(HField sf, Object value) {
	assert sf.isStatic();
	HClass cls = sf.getDeclaringClass();
	if (!isLoaded(cls)) load(cls);
	put(cls, FieldValueList.update(get(cls), sf, value));
    }

    /************************************************************
     *                                                          *
     *                      CALL STACK                          *
     *                                                          *
     ***********************************************************/

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

    /************************************************************
     *                                                          *
     *                        STRINGS                           *
     *                                                          *
     ***********************************************************/

    final private Hashtable internTable = new Hashtable();

    final ObjectRef intern(ObjectRef src) {
	String s = ref2str(src);
	ObjectRef obj = (ObjectRef) internTable.get(s);
	if (obj==null) { internTable.put(s, src); obj = src; }
	return obj;
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

    final ObjectRef makeString(String s)
	throws InterpretedThrowable {
        
	ObjectRef obj = new ObjectRef(this, HCstring);
	ArrayRef ca=new ArrayRef(this, HCcharA, new int[]{s.length()});
	for (int i=0; i<s.length(); i++)
	    ca.update(i, new Character(s.charAt(i)));
	HMethod hm = HCstring.getConstructor(new HClass[] { HCcharA });
	Method.invoke(this, hm, new Object[] { obj, ca });

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

    /************************************************************
     *                                                          *
     *                    NATIVE METHODS                        *
     *                                                          *
     ***********************************************************/

    private final Hashtable nativeRegistry = new Hashtable();
    private final Hashtable nativeClosure = new Hashtable();
    final void register(NativeMethod nm) {
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

    /************************************************************
     *                                                          *
     *                   PROFILING SUPPORT                      *
     *                                                          *
     ***********************************************************/

    public final PrintWriter prof;
    private long count; // instruction count.
    final synchronized void incrementInstructionCount() { 
        count++; 
    }
    final synchronized long getInstructionCount() { 
        return count; 
    }
    // profile time spent in a method.
    final synchronized void profile(HMethod method, long start, long end) {
	if (prof==null) return;
	else prof.println("M "+
			  method.getDeclaringClass().getName()+" "+
			  method.getName()+" "+method.getDescriptor()+" "+
			  start+" "+end);
    }
    // profile lifetime of an object instance
    final synchronized void profile(HClass cls, long start, long end) {
	if (prof==null) return;
	else prof.println("N "+cls.getDescriptor()+" "+start+" "+end);
    }

    private void map(HClass hclass, long offset, HField field) {
	classInfo.put
	    (new Tuple(new Object[] { hclass, new Long(offset) }), field);
    }
    
    private void map(Label label, HClass hclass) {
	classInfo.put(label, hclass);
    }

    private void map(Label label, HField field) {
	classInfo.put(label, field);
    }

    private void map(Label label, HMethod method) {
	classInfo.put(label, method);
    }
    
    private void map(ClazPointer ptr, Object object) {
	classInfo.put(ptr, object);
    }
    
    
}



