// HClassProxy.java, created Tue Jan 11 07:39:47 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.ArrayFactory;
import harpoon.Util.ReferenceUnique;
import harpoon.Util.Collections.UniqueVector;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.io.Serializable;
/**
 * <code>HClassProxy</code> serves as a proxy class for
 * <code>HClass</code> objects, allowing them to be swapped out &
 * "redefined" after creation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassProxy.java,v 1.1.4.9 2001-11-08 00:24:48 cananian Exp $
 */
class HClassProxy extends HClass implements HClassMutator, Serializable {
  Relinker relinker;
  HClass proxy;
  HClassMutator proxyMutator;
  // this boolean indicates whether the proxied class is from
  // the same linker as this HClassProxy, which only happens
  // when our linker create new HClassSyns
  boolean sameLinker;

  HClassProxy(Relinker l, HClass proxy) {
      super(l);
      this.relinker = l;
      relink(proxy);
  }
  void relink(HClass newproxy) {
    Util.assert(newproxy!=null);
    Util.assert(!(newproxy instanceof HClassProxy &&
		  ((HClassProxy)newproxy).relinker==relinker),
		"should never proxy to a proxy of this same relinker.");
    // first update all the fields and methods hanging around.
    if (proxy!=null) {
      HField[] hf = proxy.getDeclaredFields();
      for (int i=0; i<hf.length; i++)
	try {
	  HFieldProxy hfp = (HFieldProxy) relinker.memberMap.get(hf[i]);
	  if (hfp==null) continue;
	  hfp.flushMemberMap(); hfp.relink(null);
	  HField nhf = newproxy.getDeclaredField(hf[i].getName());
	  if (!nhf.getDescriptor().equals(hf[i].getDescriptor())) continue;
	  hfp.relink(nhf); hfp.updateMemberMap();
	} catch (NoSuchFieldError e) { /* skip */ }
      HMethod[] hm = proxy.getDeclaredMethods();
      for (int i=0; i<hm.length; i++)
	try {
	  HMethodProxy hmp = (HMethodProxy) relinker.memberMap.get(hm[i]);
	  if (hmp==null) continue;
	  hmp.flushMemberMap(); hmp.relink(null);
	  HMethod nhm = newproxy.getDeclaredMethod(hm[i].getName(),
						   hm[i].getDescriptor());
	  hmp.relink(nhm); hmp.updateMemberMap();
	} catch (NoSuchMethodError e) { /* skip */ }
    }
    // okay, now that the members are updated, let's update this guy.
    this.proxy = newproxy;
    this.proxyMutator = newproxy.getMutator();
    this.sameLinker = (relinker == newproxy.getLinker());
  }

  /**
   * Returns a mutator for this <code>HClass</code>, or <code>null</code>
   * if this object is immutable.
   */
  public HClassMutator getMutator() {
    if (proxyMutator==null) {
      relink(isArray()
	     ? (HClass) new HClassArraySyn(relinker,
					   ((HClassArray)proxy).baseType,
					   ((HClassArray)proxy).dims)
	     : (HClass) new HClassSyn(relinker, proxy.getName(), this));
      proxy.hasBeenModified = false; // exact copy of proxy.
    }
    return (proxyMutator==null) ? null : this;
  }

  // the following methods need no special handling:
  public boolean hasBeenModified() { return proxy.hasBeenModified(); }
  public String getName() { return proxy.getName(); }
  public String getPackage() { return proxy.getPackage(); }
  public String getDescriptor() { return proxy.getDescriptor(); }
  public String getSourceFile() { return proxy.getSourceFile(); }
  public int getModifiers() { return proxy.getModifiers(); }
  public boolean isArray() { return proxy.isArray(); }
  public boolean isInterface() { return proxy.isInterface(); }
  public boolean isPrimitive() { return proxy.isPrimitive(); }

  // the following methods require use to unwrap the parameters and
  // wrap up the return values.
  public HField getDeclaredField(String name) throws NoSuchFieldError {
    return wrap(proxy.getDeclaredField(name));
  }
  public HField[] getDeclaredFields() {
    return wrap(proxy.getDeclaredFields());
  }
  public HMethod getDeclaredMethod(String name, HClass parameterTypes[])
    throws NoSuchMethodError {
    return wrap(proxy.getDeclaredMethod(name, unwrap(parameterTypes)));
  }
  public HMethod getDeclaredMethod(String name, String descriptor)
    throws NoSuchMethodError {
    return wrap(proxy.getDeclaredMethod(name, descriptor));
  }
  public HMethod[] getDeclaredMethods() {
    return wrap(proxy.getDeclaredMethods());
  }
  public HConstructor getConstructor(HClass parameterTypes[])
    throws NoSuchMethodError {
    return wrap(proxy.getConstructor(unwrap(parameterTypes)));
  }
  public HConstructor[] getConstructors() {
    return wrap(proxy.getConstructors());
  }
  public HInitializer getClassInitializer() {
    return wrap(proxy.getClassInitializer());
  }
  public HClass getSuperclass() {
    return wrap(proxy.getSuperclass());
  }
  public HClass[] getInterfaces() {
    return wrap(proxy.getInterfaces());
  }
  public HClass getComponentType() {
    return wrap(proxy.getComponentType());
  }
  public boolean equals(Object o) {
    return (o instanceof HClass &&
	    ((HClass)o).getDescriptor().equals(getDescriptor()));
  }
  // HClassMutator interface
  public HField addDeclaredField(String name, HClass type)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredField(name, unwrap(type)));
  }
  public HField addDeclaredField(String name, String descriptor)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredField(name, descriptor));
  }
  public HField addDeclaredField(String name, HField template)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredField(name, template));
  }
  public void removeDeclaredField(HField f) throws NoSuchMemberException {
    HFieldProxy fp = (HFieldProxy) f;
    fp.flushMemberMap();
    proxyMutator.removeDeclaredField(fp.proxy);
  }
  public HInitializer addClassInitializer() throws DuplicateMemberException {
    return wrap(proxyMutator.addClassInitializer());
  }
  public void removeClassInitializer(HInitializer m)
    throws NoSuchMemberException {
    HInitializerProxy ip = (HInitializerProxy) m;
    ip.flushMemberMap();
    proxyMutator.removeClassInitializer((HInitializer)ip.proxy);
  }
  public HConstructor addConstructor(String descriptor)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addConstructor(descriptor));
  }
  public HConstructor addConstructor(HClass[] paramTypes)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addConstructor(unwrap(paramTypes)));
  }
  public HConstructor addConstructor(HConstructor template)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addConstructor(template));
  }
  public void removeConstructor(HConstructor c) throws NoSuchMemberException {
    HConstructorProxy cp = (HConstructorProxy) c;
    cp.flushMemberMap();
    proxyMutator.removeConstructor((HConstructor)cp.proxy);
  }
  public HMethod addDeclaredMethod(String name, String descriptor)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredMethod(name, descriptor));
  }
  public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				   HClass returnType)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredMethod(name, unwrap(paramTypes),
					       unwrap(returnType)));
  }
  public HMethod addDeclaredMethod(String name, HMethod template)
    throws DuplicateMemberException {
    return wrap(proxyMutator.addDeclaredMethod(name, template));
  }
  public void removeDeclaredMethod(HMethod m) throws NoSuchMemberException {
    HMethodProxy mp = (HMethodProxy) m;
    mp.flushMemberMap();
    proxyMutator.removeDeclaredMethod(mp.proxy);
  }
  public void addInterface(HClass in) {
    proxyMutator.addInterface(unwrap(in));
  }
  public void removeInterface(HClass in) throws NoSuchClassException {
    proxyMutator.removeInterface(unwrap(in));
  }
  public void removeAllInterfaces() { proxyMutator.removeAllInterfaces(); }
  public void addModifiers(int m) { proxyMutator.addModifiers(m); }
  public void setModifiers(int m) { proxyMutator.setModifiers(m); }
  public void removeModifiers(int m) { proxyMutator.removeModifiers(m); }
  public void setSuperclass(HClass sc) {
    proxyMutator.setSuperclass(unwrap(sc));
  }
  public void setSourceFile(String sourcefilename) {
    proxyMutator.setSourceFile(sourcefilename);
  }

  // Serializable interface.
  // we have to work around the fact that the base HClass is not Serializable.
  public Object writeReplace() { return new HClassProxyStub(this); }
  private static final class HClassProxyStub implements java.io.Serializable {
    private Relinker relinker;
    private HClass proxy;
    HClassProxyStub(HClassProxy hcp) {
      this.relinker = hcp.relinker;
      this.proxy = hcp.proxy;
    }
    public Object readResolve() {
      // leverage relinker during reconstruct.  this makes sure all our
      // mappings are consistent with the descCache.
      return relinker.load(proxy.getDescriptor(), proxy);
    }
  }

  // wrap/unwrap methods.
  private HClass wrap(HClass hc) {
    //if (sameLinker && hc != proxy) return hc; else return relinker.wrap(hc);
    return relinker.wrap(hc); // it can never hurt to wrap the class.
  }
  private HClass unwrap(HClass hc) {
    if (sameLinker) return hc; else return relinker.unwrap(hc);
  }
  private HField wrap(HField hf) { return relinker.wrap(hf); }
  private HMethod wrap(HMethod hm) { return relinker.wrap(hm); }
  private HConstructor wrap(HConstructor hc) { return relinker.wrap(hc); }
  private HInitializer wrap(HInitializer hi) { return relinker.wrap(hi); }

  // array wrap/unwrap
  private HField[] wrap(HField hf[]) { return relinker.wrap(hf); }
  private HMethod[] wrap(HMethod hm[]) { return relinker.wrap(hm); }
  private HConstructor[] wrap(HConstructor hc[]) {return relinker.wrap(hc);}

  private HClass[] wrap(HClass[] hc) {
    HClass[] result = new HClass[hc.length];
    for (int i=0; i<result.length; i++)
      result[i] = wrap(hc[i]);
    return result;
  }
  private HClass[] unwrap(HClass[] hc) {
    HClass[] result = new HClass[hc.length];
    for (int i=0; i<result.length; i++)
      result[i] = unwrap(hc[i]);
    return result;
  }
}

// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:
