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
 * @version $Id: HMemberProxy.java,v 1.1.4.6 2000-10-22 08:42:42 cananian Exp $
 * @see HFieldProxy
 * @see HMethodProxy
 */
abstract class HMemberProxy implements HMember {
    Relinker relinker;
    boolean sameLinker;
    private HMember proxy;
    private final int hashcode;

    /** Creates a <code>HMemberProxy</code>. */
    HMemberProxy(Relinker relinker, HMember proxy) {
        this.relinker = relinker;
	// one hash code forever.
	hashcode = proxy.getDeclaringClass().hashCode() ^
	    proxy.getName().hashCode() ^ proxy.getDescriptor().hashCode();
    }
    protected void relink(HMember proxy) {
	Util.assert(!(proxy instanceof HMemberProxy &&
		      ((HMemberProxy)proxy).relinker==relinker),
		    "should never proxy to a proxy of this same relinker.");
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
    public int hashCode() { return hashcode; }
    public String toString() { return proxy.toString(); }
    public boolean equals(Object obj) {
	if (obj instanceof HMemberProxy)
	    return proxy.equals(((HMemberProxy)obj).proxy);
	Util.assert(false);// this is usually a bug.
	return false;
    }
    // Comparable interface
    /** Compares two <code>HMember</code>s lexicographically; first by
     *  declaring class, then by name, and lastly by descriptor. */
    public int compareTo(Object o) {
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
