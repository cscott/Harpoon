// HConstructorProxy.java, created Tue Jan 11 08:46:55 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HConstructorProxy</code> is a relinkable proxy for an
 * <code>HConstructor</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructorProxy.java,v 1.1.4.3 2000-11-12 03:22:34 cananian Exp $
 * @see HConstructor
 */
class HConstructorProxy extends HMethodProxy implements HConstructor {
    /** Creates a <code>HConstructorProxy</code>. */
    HConstructorProxy(Relinker relinker, HConstructor proxy) {
        super(relinker, proxy);
    }
    void relink(HConstructor proxy) {
	super.relink(proxy);
    }
    public String toString() { return HConstructorImpl.toString(this); }
    public int hashCode() { return HConstructorImpl.hashCode(this); }
}
