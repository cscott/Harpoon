// HMethodProxy.java, created Tue Jan 11 08:34:57 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HMethodProxy</code> is a relinkable proxy for an
 * <code>HMethod</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodProxy.java,v 1.1.2.3 2000-01-11 21:53:48 cananian Exp $
 * @see HMethod
 */
class HMethodProxy implements HMethod, HMethodMutator {
    Relinker relinker;
    HMethod proxy;
    HMethodMutator proxyMutator;
    
    /** Creates a <code>HMethodProxy</code>. */
    HMethodProxy(Relinker relinker, HMethod proxy) {
        this.relinker = relinker;
	relink(proxy);
    }
    void relink(HMethod proxy) {
	this.proxy = proxy;
	this.proxyMutator = (proxy==null) ? null : proxy.getMutator();
    }

    public HMethodMutator getMutator() {
	if (proxyMutator==null)
	    ((HClassProxy)getDeclaringClass()).getMutator();
	return (proxyMutator==null) ? null : this;
    }
    // HMethod interface
    public HClass getDeclaringClass() {return wrap(proxy.getDeclaringClass());}
    public String getName() { return proxy.getName(); }
    public int getModifiers() { return proxy.getModifiers(); }
    public HClass getReturnType() { return wrap(proxy.getReturnType()); }
    public String getDescriptor() { return proxy.getDescriptor(); }
    public HClass[] getParameterTypes() {
	return wrap(proxy.getParameterTypes());
    }
    public String[] getParameterNames() { return proxy.getParameterNames(); }
    public HClass[] getExceptionTypes() {
	return wrap(proxy.getExceptionTypes());
    }
    public boolean isSynthetic() { return proxy.isSynthetic(); }
    public boolean isInterfaceMethod() { return proxy.isInterfaceMethod(); }
    public boolean isStatic() { return proxy.isStatic(); }
    public boolean equals(Object obj) {
	if (obj instanceof HMethodProxy)
	    return proxy.equals(((HMethodProxy)obj).proxy);
	return proxy.equals(obj);
    }
    public int hashCode() { return proxy.hashCode(); }
    public String toString() { return proxy.toString(); }
    // HMethodMutator interface
    public void addModifiers(int m) { proxyMutator.addModifiers(m); }
    public void setModifiers(int m) { proxyMutator.setModifiers(m); }
    public void removeModifiers(int m) { proxyMutator.removeModifiers(m); }
    public void setReturnType(HClass type) {
	flushMemberMap();
	proxyMutator.setReturnType(unwrap(type));
	updateMemberMap();
    }
    public void setParameterTypes(HClass[] parameterTypes) {
	flushMemberMap();
	proxyMutator.setParameterTypes(unwrap(parameterTypes));
	updateMemberMap();
    }
    public void setParameterType(int which, HClass type) {
	flushMemberMap();
	proxyMutator.setParameterType(which, unwrap(type));
	updateMemberMap();
    }
    public void setParameterNames(String[] parameterNames) {
	proxyMutator.setParameterNames(parameterNames);
    }
    public void setParameterName(int which, String name) {
	proxyMutator.setParameterName(which, name);
    }
    public void addExceptionType(HClass exceptionType) {
	proxyMutator.addExceptionType(unwrap(exceptionType));
    }
    public void setExceptionTypes(HClass[] exceptionTypes) {
	proxyMutator.setExceptionTypes(unwrap(exceptionTypes));
    }
    public void removeExceptionType(HClass exceptionType) {
	proxyMutator.removeExceptionType(unwrap(exceptionType));
    }
    public void setSynthetic(boolean isSynthetic) {
	proxyMutator.setSynthetic(isSynthetic);
    }
    // keep member map up-to-date when hashcode changes.
    void flushMemberMap() { relinker.memberMap.remove(proxy); }
    void updateMemberMap() { relinker.memberMap.put(proxy, this); }

    // wrap/unwrap
    private HClass wrap(HClass hc) { return relinker.wrap(hc); }
    private HClass unwrap(HClass hc) { return relinker.unwrap(hc); }
    private HClass[] wrap(HClass hc[]) { return relinker.wrap(hc); }
    private HClass[] unwrap(HClass[] hc) { return relinker.unwrap(hc); }
}
