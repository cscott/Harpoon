// HClassArraySyn.java, created Fri Oct 20 17:34:37 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * <code>HClassArraySyn</code> is a simple subclass of
 * <code>HClassArray</code> which allows you to add methods
 * (not fields, class initializers, or constructors) to
 * an array type.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassArraySyn.java,v 1.1.2.6 2001-01-18 22:53:34 cananian Exp $
 */
class HClassArraySyn extends HClassArray implements HClassMutator {
    final List declaredMethods = new ArrayList(4);
    
    /** Creates a <code>HClassArraySyn</code>. */
    HClassArraySyn(Linker linker, HClass baseType, int dims) {
	this(linker, baseType, dims, true/*init w/ clone method*/);
    }
    private HClassArraySyn(Linker linker, HClass baseType, int dims,
			   boolean init) {
        super(linker, baseType, dims);
	if (init) // we skip this step when restoring from serialized form.
	    declaredMethods.add // even clone method is mutable.
		(new HMethodSyn(this, cloneMethod.getName(), cloneMethod));
    }
    /** Allow mutation. */
    public HClassMutator getMutator() { return this; }
    public HMethod[] getDeclaredMethods() {
	return (HMethod[])
	    declaredMethods.toArray(new HMethod[declaredMethods.size()]);
    }

    // Allowed mutations.  -----------------------------------------
    public HMethod addDeclaredMethod(String name, String descriptor) {
	Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
	return addDeclaredMethod0(new HMethodSyn(this, name, descriptor));
    }
    public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				     HClass returnType) {
	Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
	Util.assert(checkLinker(returnType));
	for (int i=0; i<paramTypes.length; i++)
	    Util.assert(checkLinker(paramTypes[i]));
	return addDeclaredMethod0(new HMethodSyn(this, name, paramTypes,
						 returnType));
    }
    public HMethod addDeclaredMethod(String name, HMethod template) {
	Util.assert(!name.equals("<init>") && !name.equals("<clinit>"));
	return addDeclaredMethod0(new HMethodSyn(this, name, template));
    }
    /** single implementation. */
    private HMethod addDeclaredMethod0(HMethodSyn hm) {
	Util.assert(hm.getDeclaringClass()==this);
	if (declaredMethods.contains(hm))
	    throw new DuplicateMemberException("Method "+hm+" in "+this);
	declaredMethods.add(hm);
	hasBeenModified=true; // flag the modification
	return hm;
    }
    public void removeDeclaredMethod(HMethod m) throws NoSuchMethodError {
	if (declaredMethods.remove(m)) {
	    hasBeenModified=true; // flag the modification
	    return;
	}
	throw new NoSuchMemberException("Method "+m+" in "+this);
    }

    // Unallowed mutations.  ---------------------------------------
    public HField addDeclaredField(String name, HClass type)
	throws DuplicateMemberException {
	throw new Error("Adding fields to arrays is not allowed.");
    }
    public HField addDeclaredField(String name, String descriptor)
	throws DuplicateMemberException {
	throw new Error("Adding fields to arrays is not allowed.");
    }
    public HField addDeclaredField(String name, HField template)
	throws DuplicateMemberException {
	throw new Error("Adding fields to arrays is not allowed.");
    }
    public void removeDeclaredField(HField f) throws NoSuchMemberException {
	if (f==lengthField)
	    throw new Error("Removing the length fields from an array is not "+
			    "allowed.");
	else throw new NoSuchMemberException(f.toString());
    }
    public HInitializer addClassInitializer() throws DuplicateMemberException {
	throw new Error("Adding a class initializer to an array is not "+
			"allowed.");
    }
    public void removeClassInitializer(HInitializer m)
	throws NoSuchMemberException {
	throw new NoSuchMemberException(m.toString());
    }
    public HConstructor addConstructor(String descriptor)
	throws DuplicateMemberException {
	throw new Error("Adding a constructor to an array is not allowed.");
    }
    public HConstructor addConstructor(HClass[] paramTypes)
	throws DuplicateMemberException {
	throw new Error("Adding a constructor to an array is not allowed.");
    }
    public HConstructor addConstructor(HConstructor template)
	throws DuplicateMemberException {
	throw new Error("Adding a constructor to an array is not allowed.");
    }
    public void removeConstructor(HConstructor c)
	throws NoSuchMemberException {
	throw new NoSuchMemberException(c.toString());
    }
    public void addInterface(HClass in) {
	throw new Error("Not allowed to add interfaces to an array.");
    }
    public void removeInterface(HClass in)
	throws NoSuchClassException {
	throw new Error("Not allowed to remove interfaces from an array.");
    }
    public void removeAllInterfaces() {
	throw new Error("Not allowed to remove interfaces from an array.");
    }
    public void addModifiers(int m) {
	throw new Error("Not allowed to change the modifiers of an array.");
    }
    public void setModifiers(int m) {
	throw new Error("Not allowed to change the modifiers of an array.");
    }
    public void removeModifiers(int m) {
	throw new Error("Not allowed to change the modifiers of an array.");
    }
    public void setSuperclass(HClass sc) {
	throw new Error("Not allowed to change the superclass of an array.");
    }
    public void setSourceFile(String sourcefilename) {
	throw new Error("Not allowed to reset the source file of an array.");
    }

    //----------------------------------------------------------
    // assertion helper.
    private boolean checkLinker(HClass hc) {
	return hc.isPrimitive() || hc.getLinker()==getLinker();
    }
    //----------------------------------------------------------

    /** Serializable interface. */
    public Object writeReplace() {
	return new Stub(this);
    }
    private static final class Stub implements java.io.Serializable {
	private final Linker linker;
	private final HClass baseType;
	private final int dims;
	private final List declaredMethods;
	private final boolean modified;
	Stub(HClassArraySyn c) { // store salient information.
	    this.linker = c.getLinker();
	    this.baseType = c.baseType;
	    this.dims = c.dims;
	    this.modified = c.hasBeenModified;
	    this.declaredMethods = new ArrayList(c.declaredMethods.size());
	    for (Iterator it=c.declaredMethods.iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		this.declaredMethods.add(new HClassSyn.MethodStub(hm));
	    }
	}
	public Object readResolve() {
	    HClassArraySyn c=new HClassArraySyn(linker, baseType, dims, false);
	    for (Iterator it=declaredMethods.iterator(); it.hasNext(); ) {
		HMethod hm = ((HClassSyn.MethodStub)it.next()).reconstruct(c);
		c.declaredMethods.add(hm);
	    }
	    c.hasBeenModified = this.modified;
	    return c;
	}
    }
}
