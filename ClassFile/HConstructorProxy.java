// HConstructorProxy.java, created Tue Jan 11 08:46:55 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HConstructorProxy</code> is a relinkable proxy for an
 * <code>HConstructor</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HConstructorProxy.java,v 1.1.4.4 2001-11-12 17:55:26 cananian Exp $
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
}
