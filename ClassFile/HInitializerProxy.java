// HInitializerProxy.java, created Tue Jan 11 08:48:28 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HInitializerProxy</code> is a relinkable proxy for an
 * <code>HInitializer</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HInitializerProxy.java,v 1.1.4.3 2000-11-12 03:22:34 cananian Exp $
 * @see HInitializer
 */
class HInitializerProxy extends HMethodProxy implements HInitializer {
    /** Creates a <code>HInitializerProxy</code>. */
    HInitializerProxy(Relinker relinker, HInitializer proxy) {
        super(relinker, proxy);
    }
    void relink(HInitializer proxy) {
	super.relink(proxy);
    }
    public String toString() { return HInitializerImpl.toString(this); }
    public int hashCode() { return HInitializerImpl.hashCode(this); }
}
