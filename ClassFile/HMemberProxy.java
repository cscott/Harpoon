// HMemberProxy.java, created Wed Jan 12 19:15:03 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;
/**
 * <code>HMemberProxy</code> is a relinkable proxy for an
 * <code>HMember</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMemberProxy.java,v 1.3.2.2 2002-03-10 08:01:57 cananian Exp $
 * @see HFieldProxy
 * @see HMethodProxy
 */
abstract class HMemberProxy implements HMember {
    Relinker relinker;
    boolean sameLinker;
    private HMember proxy;
    private final int hashcode;

    /** Creates a <code>HMemberProxy</code>. */
    HMemberProxy(Relinker relinker, HMember proxy, int hashcode) {
        this.relinker = relinker;
	// one hash code forever.
	this.hashcode = hashcode;
    }
    protected void relink(HMember proxy) {
	assert !(proxy instanceof HMemberProxy &&
		      ((HMemberProxy)proxy).relinker==relinker) : "should never proxy to a proxy of this same relinker.";
	this.proxy = proxy;
	this.sameLinker = (proxy==null || // keep us safe if the proxy==null
			   relinker == proxy.getDeclaringClass().getLinker());
    }
    // HMember interface
    public HClass getDeclaringClass() {return wrap(proxy.getDeclaringClass());}
    public String getDescriptor() { return proxy.getDescriptor(); }
    public String getName() { return proxy.getName(); }
    public int getModifiers() { return proxy.getModifiers(); }
    public boolean isSynthetic() { return proxy.isSynthetic(); }
    public final int hashCode() { return hashcode; }
    public abstract String toString();
    public abstract boolean equals(Object obj);

    // Comparable interface
    /** Compares two <code>HMember</code>s lexicographically; first by
     *  declaring class, then by name, and lastly by descriptor. */
    public int compareTo(HMember o) {
        return memberComparator.compare(this, o);
    }

    // keep member map up-to-date when hashcode changes.
    protected void flushMemberMap() { relinker.memberMap.remove(proxy); }
    protected void updateMemberMap() { relinker.memberMap.put(proxy, this); }

    // wrap/unwrap
    protected HClass wrap(HClass hc) {
	if (sameLinker && hc != proxy.getDeclaringClass()) return hc;
	return relinker.wrap(hc);
    }
    protected HClass unwrap(HClass hc){
	if (sameLinker && hc != getDeclaringClass()) return hc;
	return relinker.unwrap(hc);
    }
    // array wrap/unwrap.
    protected HClass[] wrap(HClass[] hc) {
	HClass[] result = new HClass[hc.length];
	for (int i=0; i<result.length; i++)
	    result[i] = wrap(hc[i]);
	return result;
    }
    protected HClass[] unwrap(HClass[] hc) {
	HClass[] result = new HClass[hc.length];
	for (int i=0; i<result.length; i++)
	    result[i] = unwrap(hc[i]);
	return result;
    }
}
