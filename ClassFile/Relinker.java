// Relinker.java, created Mon Dec 27 19:05:58 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * A <code>Relinker</code> object is a <code>Linker</code> where one
 * can globally replace references to a certain class with references
 * to another, different, class.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Relinker.java,v 1.3 2002-02-26 22:45:06 cananian Exp $
 */
public class Relinker extends Linker implements java.io.Serializable {
    protected final Linker linker;

    /** Creates a <code>Relinker</code>. */
    public Relinker(Linker linker) {
	this.linker = linker;
    }
    protected HClass forDescriptor0(String descriptor) {
	return new HClassProxy(this, linker.forDescriptor(descriptor));
    }
    protected HClass makeArray(HClass baseType, int dims) {
	// two cases: 1) base type's proxy is a mutable class generated
	// by this relinker, 2) base type's proxy is from linker.
	// proxy base array type in case 2; create our own HClassArray
	// for case 1.
	if (!baseType.isPrimitive()) {
	    HClass baseProxy = ((HClassProxy)baseType).proxy;
	    if (baseProxy.getLinker()==this) // must be our mutable.
		return new HClassProxy
		    (this, new HClassArray(this, baseProxy, dims));
	}
	// otherwise, proxy the linker's array.
	String desc = Util.repeatString("[",dims) + baseType.getDescriptor();
	return new HClassProxy(this, linker.forDescriptor(desc));
    }
    
    /** Creates a mutable class with the given name which is based on
     *  the given template class.  The name <b>need not</b> be unique.
     *  If a class with the given name already exists, all references
     *  to the existing class are changed to point to the new mutable
     *  class returned by this method. */
    public HClass createMutableClass(String name, HClass template) {
	Util.ASSERT(template.getLinker()==this);
	HClass newClass = new HClassSyn(this, name, template);
	HClass proxyClass = new HClassProxy(this, newClass);
	newClass.hasBeenModified=true;
	try {
	    HClass oldClass = forName(name); // get existing proxy class
	    if (oldClass.equals(template))
		newClass.hasBeenModified=false; // exact copy of oldClass
	    relink(oldClass, proxyClass);
	    return oldClass;
	} catch (NoSuchClassException e) { // brand spankin' new class
	    register(proxyClass);
	    return proxyClass;
	}
    }

    /** Globally replace all references to <code>oldClass</code> with
     *  references to <code>newClass</code>, which may or may not have
     *  the same name.  The following constraint must hold:<pre>
     *  oldClass.getLinker()==newClass.getLinker()==this
     *  </pre><p>
     *  <b>WARNING:</b> the <code>hasBeenModified()</code> method of
     *  <code>HClass</code>is not reliable after calling 
     *  <code>relink()</code> if <code>oldClass.getName()</code> is not the
     *  same as <code>newClass.getName()</code>.  The value returned
     *  by <code>HClass.hasBeenModified()</code> will not reflect changes
     *  due to the global replacement of <code>oldClass</code> with
     *  <code>newClass</code> done by this <code>relink()</code>.</p>
     */
    public void relink(HClass oldClass, HClass newClass) {
	// XXX this can cause some .equals() weirdness!
	Util.ASSERT(oldClass.getLinker()==this);
	Util.ASSERT(newClass.getLinker()==this);
	// we're going to leave the old mapping in, so that classes
	// loaded in the future still get the new class.  uncomment
	// out the next line if we decide to delete the old descriptor
	// mapping when we relink.
	//descCache.remove(oldClass.getDescriptor());
	((HClassProxy)oldClass).relink(((HClassProxy)newClass).proxy);
	descCache.put(oldClass.getDescriptor(), oldClass);
    }

    /** Move <code>HMember</code> <code>hm</code> from its declaring
     *  class to some other class, <code>newDestination</code>.  This
     *  usually only makes sense if you're moving a member from a
     *  class to its superclass, or vice-versa --- but we're not
     *  enforcing this (full foot-shooting power granted).  The
     *  <code>newDestination</code> class must not already have a
     *  field named the same/method with the same signature as
     *  <code>hm</code>.  All references to the member in the old class
     *  will be re-directed to point to the member in the new class,
     *  and in fact, upon return the given <code>HMember</code>
     *  <code>hm</code> will refer to the new member.
     *  <b>WARNING</b>: make sure that any methods which refer to this
     *  member have been converted to Bytecode form at least *before*
     *  invoking <code>move()</code>; otherwise field resolution will fail
     *  when we are parsing the bytecode file (we don't keep any record
     *  of the 'old' or 'canonical' name of the moved member).
     */
    public void move(HMember hm, HClass newDestination)
	throws DuplicateMemberException {
	HClass oldDeclarer = hm.getDeclaringClass();
	Util.ASSERT(oldDeclarer.getLinker()==this);
	Util.ASSERT(newDestination.getLinker()==this);
	Util.ASSERT(hm instanceof HMemberProxy);
	// make sure both old and new classes are mutable.
	HClassMutator check;
	check = oldDeclarer.getMutator();	Util.ASSERT(check!=null);
	check = newDestination.getMutator();	Util.ASSERT(check!=null);
	// access mutator of proxied class directly.
	HClassMutator oldmut = ((HClassProxy)oldDeclarer).proxyMutator;
	HClassMutator newmut = ((HClassProxy)newDestination).proxyMutator;
	// add field/method to (proxied) new class
	HMember newm = (hm instanceof HField) ? (HMember)
	    newmut.addDeclaredField(hm.getName(), (HField)hm) :
	    (hm instanceof HConstructor) ? (HMember)
	    newmut.addConstructor((HConstructor) hm) :
	    (hm instanceof HInitializer) ? (HMember)
	    newmut.addClassInitializer() :
	    (hm instanceof HMethod) ? (HMember)
	    newmut.addDeclaredMethod(hm.getName(), (HMethod)hm) :
	    null/*should never happen!*/;
	Util.ASSERT(newm!=null, "not a field, method, or constructor");
	// store away original (old) field.
	HMember oldm = (hm instanceof HField) ? 
	    (HMember) ((HFieldProxy)hm).proxy :
	    (HMember) ((HMethodProxy)hm).proxy;
	// redirect old field proxy to this new field.
	int hashcheck = hm.hashCode();
	if (hm instanceof HField)
	    ((HFieldProxy)hm).relink((HField)newm);
	else if (hm instanceof HConstructor)
	    ((HConstructorProxy)hm).relink((HConstructor)newm);
	else if (hm instanceof HInitializer)
	    ((HInitializerProxy)hm).relink((HInitializer)newm);
	else if (hm instanceof HMethod)
	    ((HMethodProxy)hm).relink((HMethod)newm);
	else Util.ASSERT(false, "not a field, method, or constructor");
	Util.ASSERT(hashcheck==hm.hashCode());// hashcode shouldn't change.
	// now remove (non-proxied) old field.
	if (hm instanceof HField)
	    oldmut.removeDeclaredField((HField)oldm);
	else if (hm instanceof HConstructor)
	    oldmut.removeConstructor((HConstructor)oldm);
	else if (hm instanceof HInitializer)
	    oldmut.removeClassInitializer((HInitializer)oldm);
	else if (hm instanceof HMethod)
	    oldmut.removeDeclaredMethod((HMethod)oldm);
	// update the memberMap
	memberMap.remove(oldm);
	memberMap.put(newm, hm);
	// done!
	Util.ASSERT(hm.getDeclaringClass()==newDestination);
    }

    // stub to help in reloading proxies.
    HClassProxy load(String descriptor, HClass proxy) {
	if (descCache.containsKey(descriptor))
	    ((HClassProxy) descCache.get(descriptor)).relink(proxy);
	else
	    descCache.put(descriptor, new HClassProxy(this, proxy));
	return (HClassProxy) descCache.get(descriptor);
    }

    // Serializable
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, ClassNotFoundException {
	in.defaultReadObject();
	// create new memberMap
	// note that linker descCache is cleared magically on read, too.
	memberMap = new HashMap();
	// now restore the "odd" entries in the descCache.
	for (Iterator it=((List)in.readObject()).iterator(); it.hasNext(); ) {
	    String descK = (String) it.next();
	    String descV = (String) it.next();
	    relink(forDescriptor(descK),forDescriptor(descV));
	}
	// done.
    }
    private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException {
	out.defaultWriteObject();
	// write out list of relinked descriptors.
	List l = new ArrayList();
	for (Iterator it=descCache.keySet().iterator(); it.hasNext(); ) {
	    String descK = (String) it.next();
	    String descV = forDescriptor(descK).getDescriptor();
	    if (descK.equals(descV)) continue; // not an interesting entry.
	    l.add(descK); l.add(descV);
	}
	out.writeObject(l);
    }

    // WRAP/UNWRAP CODE
    transient Map memberMap = new HashMap();

    HClass wrap(HClass hc) {
	if (hc==null || hc.isPrimitive()) return hc;
	return forDescriptor(hc.getDescriptor());
    }
    HClass unwrap(HClass hc) {
	if (hc==null || hc.isPrimitive()) return hc;
	/* If we "promote" the original class to an HClassSyn (to modify it)
	 * we must still 'unwrap' it to the unmodified version from the
	 * original linker, or reference-equality tests against it will fail.
	 * Example: searching for method foo(Object o) of class A after we've
	 * added a new field to Object.  Class A's method descriptor will
	 * still reference the "old" Object class. */
	if (((HClassProxy)hc).sameLinker)
	    return linker.forDescriptor(hc.getDescriptor());
	return ((HClassProxy)hc).proxy;
    }
    HField wrap(HField hf) {
	if (hf==null) return null;
	Util.ASSERT(!(hf instanceof HFieldProxy &&
		      ((HFieldProxy)hf).relinker==this),
		    "should never try to proxy a proxy of this same relinker");
	HField result = (HField) memberMap.get(hf);
	if (result==null) {
	    result = new HFieldProxy(this, hf);
	    Util.ASSERT(result.getDeclaringClass().getLinker()==this);
	    memberMap.put(hf, result);
	}
	return result;
    }
    HMethod wrap(HMethod hm) {
	if (hm==null) return null;
	Util.ASSERT(!(hm instanceof HMethodProxy &&
		      ((HMethodProxy)hm).relinker==this),
		    "should never try to proxy a proxy of this same relinker");
	if (hm instanceof HInitializer) return wrap((HInitializer)hm);
	if (hm instanceof HConstructor) return wrap((HConstructor)hm);
	HMethod result = (HMethodProxy) memberMap.get(hm);
	if (result==null) {
	    result = new HMethodProxy(this, hm);
	    Util.ASSERT(result.getDeclaringClass().getLinker()==this);
	    memberMap.put(hm, result);
	}
	return result;
    }
    HConstructor wrap(HConstructor hc) {
	if (hc==null) return null;
	HConstructor result = (HConstructorProxy) memberMap.get(hc);
	if (result==null) {
	    Util.ASSERT(!hc.getDeclaringClass().isArray());
	    result = new HConstructorProxy(this, hc);
	    memberMap.put(hc, result);
	}
	return result;
    }
    HInitializer wrap(HInitializer hi) {
	if (hi==null) return null;
	HInitializer result = (HInitializerProxy) memberMap.get(hi);
	if (result==null) {
	    Util.ASSERT(!hi.getDeclaringClass().isArray());
	    result = new HInitializerProxy(this, hi);
	    memberMap.put(hi, result);
	}
	return result;
    }    
    // array wrap/unwrap
    HClass[] wrap(HClass hc[]) {
	HClass[] result = new HClass[hc.length];
	for (int i=0; i<result.length; i++)
	    result[i] = wrap(hc[i]);
	return result;
    }
    HClass[] unwrap(HClass[] hc) {
	HClass[] result = new HClass[hc.length];
	for (int i=0; i<result.length; i++)
	    result[i] = unwrap(hc[i]);
	return result;
    }
    HField[] wrap(HField hf[]) {
	HField[] result = new HField[hf.length];
	for (int i=0; i<result.length; i++)
	    result[i] = wrap(hf[i]);
	return result;
    }
    HMethod[] wrap(HMethod hm[]) {
	HMethod[] result = new HMethod[hm.length];
	for (int i=0; i<result.length; i++)
	    result[i] = wrap(hm[i]);
	return result;
    }
    HConstructor[] wrap(HConstructor hc[]) {
	HConstructor[] result = new HConstructor[hc.length];
	for (int i=0; i<result.length; i++)
	    result[i] = wrap(hc[i]);
	return result;
    }
}
