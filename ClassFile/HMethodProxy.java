// HMethodProxy.java, created Tue Jan 11 08:34:57 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HMethodProxy</code> is a relinkable proxy for an
 * <code>HMethod</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodProxy.java,v 1.1.4.6 2001-11-14 23:14:41 cananian Exp $
 * @see HMethod
 */
class HMethodProxy extends HMemberProxy
    implements HMethod, HMethodMutator, java.io.Serializable {
    HMethod proxy;
    HMethodMutator proxyMutator;
    
    /** Creates a <code>HMethodProxy</code>. */
    HMethodProxy(Relinker relinker, HMethod proxy) {
	super(relinker, proxy, proxy.hashCode());
	relink(proxy);
    }
    void relink(HMethod proxy) {
	super.relink(proxy);
	this.proxy = proxy;
	this.proxyMutator = (proxy==null) ? null : proxy.getMutator();
    }

    public HMethodMutator getMutator() {
	if (proxyMutator==null)
	    ((HClassProxy)getDeclaringClass()).getMutator();
	return (proxyMutator==null) ? null : this;
    }
    // HMethod interface
    public HClass getReturnType() { return wrap(proxy.getReturnType()); }
    public HClass[] getParameterTypes() {
	return wrap(proxy.getParameterTypes());
    }
    public String[] getParameterNames() { return proxy.getParameterNames(); }
    public HClass[] getExceptionTypes() {
	return wrap(proxy.getExceptionTypes());
    }
    public boolean isInterfaceMethod() { return proxy.isInterfaceMethod(); }
    public boolean isStatic() { return proxy.isStatic(); }
    public String toString() { return HMethodImpl.toString(this); }
    public boolean equals(Object obj) { return HMethodImpl.equals(this, obj); }
    // HMethodMutator interface
    // BE CAREFUL TO KEEP TRACK OF PROXY'S HASHCODE CHANGES.
    // flushMemberMap() and updateMemberMap() need to be called whenever the
    // underlying proxy's hashcode changes.  Things that may cause a
    // change: changing declaring class, name, or descriptor.
    public void addModifiers(int m) { proxyMutator.addModifiers(m); }
    public void setModifiers(int m) { proxyMutator.setModifiers(m); }
    public void removeModifiers(int m) { proxyMutator.removeModifiers(m); }
    public void setReturnType(HClass type) {
	flushMemberMap();
	try { proxyMutator.setReturnType(unwrap(type)); }
	finally { updateMemberMap(); }
    }
    public void setParameterTypes(HClass[] parameterTypes) {
	flushMemberMap();
	try { proxyMutator.setParameterTypes(unwrap(parameterTypes)); }
	finally { updateMemberMap(); }
    }
    public void setParameterType(int which, HClass type) {
	flushMemberMap();
	try { proxyMutator.setParameterType(which, unwrap(type)); }
	finally { updateMemberMap(); }
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
    /** Serializable interface. */
    public Object writeReplace() { return new HMethodImpl.HMethodStub(this); }
}
